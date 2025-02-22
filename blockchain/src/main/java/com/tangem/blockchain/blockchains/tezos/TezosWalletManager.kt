package com.tangem.blockchain.blockchains.tezos

import android.util.Log
import com.tangem.blockchain.blockchains.tezos.TezosAddressService.Companion.calculateTezosChecksum
import com.tangem.blockchain.blockchains.tezos.network.TezosInfoResponse
import com.tangem.blockchain.blockchains.tezos.network.TezosNetworkProvider
import com.tangem.blockchain.blockchains.tezos.network.TezosTransactionData
import com.tangem.blockchain.common.*
import com.tangem.blockchain.common.transaction.Fee
import com.tangem.blockchain.common.transaction.TransactionFee
import com.tangem.blockchain.common.transaction.TransactionSendResult
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.SimpleResult
import com.tangem.blockchain.extensions.successOr
import com.tangem.blockchain.extensions.toCanonicalECDSASignature
import com.tangem.common.CompletionResult
import com.tangem.common.card.EllipticCurve
import com.tangem.common.extensions.hexToBytes
import com.tangem.common.extensions.isZero
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Utils
import java.math.BigDecimal
import java.util.EnumSet

class TezosWalletManager(
    wallet: Wallet,
    private val transactionBuilder: TezosTransactionBuilder,
    private val networkProvider: TezosNetworkProvider,
    private val curve: EllipticCurve,
) : WalletManager(wallet), TransactionSender {

    override val currentHost: String
        get() = networkProvider.baseUrl

    private val blockchain = wallet.blockchain
    private var publicKeyRevealed: Boolean? = null

    override suspend fun updateInternal() {
        when (val response = networkProvider.getInfo(wallet.address)) {
            is Result.Success -> updateWallet(response.data)
            is Result.Failure -> updateError(response.error)
        }
    }

    private fun updateWallet(response: TezosInfoResponse) {
        Log.d(this::class.java.simpleName, "Balance is ${response.balance}")
        if (response.balance != wallet.amounts[AmountType.Coin]?.value) {
            // assume outgoing transaction has been finalized if balance has changed
            wallet.recentTransactions.clear()
        }
        wallet.changeAmountValue(AmountType.Coin, response.balance)
        transactionBuilder.counter = response.counter
    }

    private fun updateError(error: BlockchainError) {
        Log.e(this::class.java.simpleName, error.customMessage)
        if (error is BlockchainSdkError) throw error
    }

    override suspend fun send(
        transactionData: TransactionData,
        signer: TransactionSigner,
    ): Result<TransactionSendResult> {
        if (publicKeyRevealed == null) {
            // in case of publicKeyRevealed is null, we should try request it
            val publicKeyRevealedUpdated = networkProvider.isPublicKeyRevealed(wallet.address)
            publicKeyRevealed = publicKeyRevealedUpdated.successOr {
                return Result.Failure(BlockchainSdkError.CustomError("publicKeyRevealed is null"))
            }
        }

        val contents =
            when (val response = transactionBuilder.buildContents(transactionData, publicKeyRevealed!!)) {
                is Result.Failure -> return Result.Failure(response.error)
                is Result.Success -> response.data
            }
        val header =
            when (val response = networkProvider.getHeader()) {
                is Result.Failure -> return Result.Failure(response.error)
                is Result.Success -> response.data
            }
        val forgedContents = transactionBuilder.forgeContents(header.hash, contents)
        // potential security vulnerability, transaction should be forged locally
//                when (val response = networkProvider.forgeContents(header.hash, contents)) {
//                    is Result.Failure -> return Result.Failure(response.error)
//                    is Result.Success -> response.data
//                }
        val dataToSign = transactionBuilder.buildToSign(forgedContents)

        val signature = when (val signerResponse = signer.sign(dataToSign, wallet.publicKey)) {
            is CompletionResult.Failure -> return Result.fromTangemSdkError(signerResponse.error)
            is CompletionResult.Success -> signerResponse.data
        }
        val canonicalSignature = canonicalizeSignature(signature)

        return when (
            val response = networkProvider.checkTransaction(
                TezosTransactionData(header, contents, encodeSignature(canonicalSignature)),
            )
        ) {
            is SimpleResult.Failure -> Result.Failure(response.error)
            is SimpleResult.Success -> {
                val transactionToSend = transactionBuilder.buildToSend(signature, forgedContents)
                when (val sendResult = networkProvider.sendTransaction(transactionToSend)) {
                    is SimpleResult.Failure -> Result.Failure(sendResult.error)
                    SimpleResult.Success -> {
                        transactionData.hash = transactionToSend
                        wallet.addOutgoingTransaction(transactionData)
                        Result.Success(TransactionSendResult(transactionToSend))
                    }
                }
            }
        }
    }

    override suspend fun getFee(amount: Amount, destination: String): Result<TransactionFee> {
        var fee: BigDecimal = BigDecimal.valueOf(TezosConstants.TRANSACTION_FEE)
        var error: Result.Failure? = null

        coroutineScope {
            val publicKeyRevealedDeferred =
                async { networkProvider.isPublicKeyRevealed(wallet.address) }
            val destinationInfoDeferred = async { networkProvider.getInfo(destination) }

            when (val result = publicKeyRevealedDeferred.await()) {
                is Result.Failure -> error = result
                is Result.Success -> {
                    publicKeyRevealed = result.data
                    if (!publicKeyRevealed!!) {
                        fee += BigDecimal.valueOf(TezosConstants.REVEAL_FEE)
                    }
                }
            }
            when (val result = destinationInfoDeferred.await()) {
                is Result.Failure -> error = result
                is Result.Success -> {
                    if (result.data.balance.isZero()) {
                        fee += BigDecimal.valueOf(TezosConstants.ALLOCATION_FEE)
                    }
                }
            }
        }

        return if (error == null) {
            Result.Success(TransactionFee.Single(Fee.Common(Amount(fee, blockchain))))
        } else {
            error!!
        }
    }

    override suspend fun estimateFee(amount: Amount, destination: String): Result<TransactionFee> {
        // we should update publicKeyRevealed on every fee estimation
        val publicKeyRevealedUpdated = networkProvider.isPublicKeyRevealed(wallet.address)
        publicKeyRevealed = publicKeyRevealedUpdated.successOr { null }
        val defaultFee = BigDecimal.valueOf(TezosConstants.TRANSACTION_FEE)
        return Result.Success(TransactionFee.Single(Fee.Common(Amount(defaultFee, blockchain))))
    }

    @Deprecated("Will be removed in the future. Use TransactionValidator instead")
    override fun validateTransaction(amount: Amount, fee: Amount?): EnumSet<TransactionError> {
        val errors = super.validateTransaction(amount, fee)
        val total = fee?.value?.add(amount.value) ?: amount.value
        if (wallet.amounts[AmountType.Coin]!!.value == total) {
            errors.add(TransactionError.TezosSendAll)
        }
        return errors
    }

    @Suppress("MagicNumber")
    private fun canonicalizeSignature(signature: ByteArray): ByteArray {
        return when (curve) {
            EllipticCurve.Ed25519, EllipticCurve.Ed25519Slip0010 -> signature
            EllipticCurve.Secp256k1 -> {
                val canonicalECDSASignature = signature.toCanonicalECDSASignature()
                // bigIntegerToBytes cuts leading zero if present
                Utils.bigIntegerToBytes(canonicalECDSASignature.r, 32) +
                    Utils.bigIntegerToBytes(canonicalECDSASignature.s, 32)
            }
            else -> throw java.lang.Exception("This curve ($curve) is not supported")
        }
    }

    private fun encodeSignature(signature: ByteArray): String {
        val prefix = TezosConstants.getSignaturePrefix(curve).hexToBytes()
        val prefixedSignature = prefix + signature
        val checksum = prefixedSignature.calculateTezosChecksum()

        return Base58.encode(prefixedSignature + checksum)
    }
}
