package com.tangem.blockchain.blockchains.tron

import android.util.Log
import com.google.common.primitives.Ints
import com.tangem.blockchain.blockchains.tron.network.TronAccountInfo
import com.tangem.blockchain.blockchains.tron.network.TronNetworkService
import com.tangem.blockchain.common.*
import com.tangem.blockchain.common.transaction.Fee
import com.tangem.blockchain.common.transaction.TransactionFee
import com.tangem.blockchain.common.transaction.TransactionSendResult
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.bigIntegerValue
import com.tangem.blockchain.extensions.decodeBase58
import com.tangem.blockchain.transactionhistory.TransactionHistoryProvider
import com.tangem.common.CompletionResult
import com.tangem.common.extensions.calculateSha256
import com.tangem.common.extensions.toHexString
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal

internal class TronWalletManager(
    wallet: Wallet,
    transactionHistoryProvider: TransactionHistoryProvider,
    private val transactionBuilder: TronTransactionBuilder,
    private val networkService: TronNetworkService,
) : WalletManager(wallet, transactionHistoryProvider = transactionHistoryProvider), TransactionSender, Approver {

    override val currentHost: String = networkService.host

    private val dummySigner = DummySigner()

    override suspend fun updateInternal() {
        val transactionIds = wallet.recentTransactions
            .filter { it.status == TransactionStatus.Unconfirmed }
            .mapNotNull { it.hash }

        when (
            val response =
                networkService.getAccountInfo(wallet.address, cardTokens, transactionIds)
        ) {
            is Result.Success -> updateWallet(response.data)
            is Result.Failure -> updateError(response.error)
        }
    }

    private fun updateWallet(response: TronAccountInfo) {
        Log.d(this::class.java.simpleName, "Balance is ${response.balance}")

        wallet.changeAmountValue(AmountType.Coin, response.balance)
        response.tokenBalances.forEach { wallet.addTokenValue(it.value, it.key) }

        wallet.recentTransactions.forEach {
            if (response.confirmedTransactionIds.contains(it.hash)) {
                it.status = TransactionStatus.Confirmed
            }
        }
    }

    private fun updateError(error: BlockchainError) {
        Log.e(this::class.java.simpleName, error.customMessage)
        if (error is BlockchainSdkError) throw error
    }

    override suspend fun send(
        transactionData: TransactionData,
        signer: TransactionSigner,
    ): Result<TransactionSendResult> {
        transactionData.requireUncompiled()

        val signResult = signTransactionData(
            amount = transactionData.amount,
            source = wallet.address,
            destination = transactionData.destinationAddress,
            signer = signer,
            publicKey = wallet.publicKey,
            extras = transactionData.extras as? TronTransactionExtras,
        )
        return when (signResult) {
            is Result.Failure -> Result.Failure(signResult.error)
            is Result.Success -> {
                when (val sendResult = networkService.broadcastHex(signResult.data)) {
                    is Result.Failure -> Result.Failure(sendResult.error)
                    is Result.Success -> {
                        val hash = sendResult.data.txid
                        transactionData.hash = hash
                        wallet.addOutgoingTransaction(transactionData.updateHash(hash = hash))
                        Result.Success(TransactionSendResult(hash))
                    }
                }
            }
        }
    }

    @Suppress("MagicNumber")
    override suspend fun getFee(amount: Amount, destination: String): Result<TransactionFee> {
        val blockchain = wallet.blockchain
        return coroutineScope {
            val destinationExistsDef = async { networkService.checkIfAccountExists(destination) }
            val resourceDef = async { networkService.getAccountResource(wallet.address) }
            val transactionDataDef = async {
                signTransactionData(
                    amount = amount,
                    source = wallet.address,
                    destination = destination,
                    signer = dummySigner,
                    publicKey = dummySigner.publicKey,
                    extras = null,
                )
            }

            if (!destinationExistsDef.await() && amount.type == AmountType.Coin) {
                return@coroutineScope Result.Success(
                    TransactionFee.Single(
                        Fee.Common(
                            amount = Amount(
                                BigDecimal.valueOf(1.1),
                                blockchain,
                            ),
                        ),
                    ),
                )
            }

            val energyFee = when (val energyFeeResult = getEnergyFee(amount, destination)) {
                is Result.Failure -> return@coroutineScope Result.Failure(energyFeeResult.error)
                is Result.Success -> energyFeeResult.data
            }
            val resource = when (val resourceResult = resourceDef.await()) {
                is Result.Failure -> return@coroutineScope Result.Failure(resourceResult.error)
                is Result.Success -> resourceResult.data
            }
            val transactionData = when (val transactionDataResult = transactionDataDef.await()) {
                is Result.Failure -> return@coroutineScope Result.Failure(transactionDataResult.error)
                is Result.Success -> transactionDataResult.data
            }

            val sunPerBandwidthPoint = 1000
            val additionalDataSize = 64
            val remainingBandwidthInSun = (resource.freeNetLimit - (resource.freeNetUsed ?: 0)) * sunPerBandwidthPoint
            val transactionSizeFee = sunPerBandwidthPoint * (transactionData.size + additionalDataSize)
            val consumedBandwidthFee = if (transactionSizeFee <= remainingBandwidthInSun) 0 else transactionSizeFee
            val totalFee = consumedBandwidthFee + energyFee

            val value = BigDecimal(totalFee).movePointLeft(blockchain.decimals())
            Result.Success(TransactionFee.Single(Fee.Common(Amount(value, blockchain))))
        }
    }

    @Suppress("LongParameterList")
    private suspend fun signTransactionData(
        amount: Amount,
        source: String,
        destination: String,
        signer: TransactionSigner,
        publicKey: Wallet.PublicKey,
        extras: TronTransactionExtras?,
    ): Result<ByteArray> {
        return when (val result = networkService.getNowBlock()) {
            is Result.Failure -> {
                Result.Failure(result.error)
            }

            is Result.Success -> {
                val transactionToSign = transactionBuilder.buildForSign(
                    amount = amount,
                    source = source,
                    destination = destination,
                    block = result.data,
                    extras = extras,
                )
                when (
                    val signResult =
                        sign(transactionToSign.encode().calculateSha256(), signer, publicKey)
                ) {
                    is Result.Failure -> Result.Failure(signResult.error)
                    is Result.Success -> {
                        val transactionToSend = transactionBuilder.buildForSend(
                            rawData = transactionToSign,
                            signature = signResult.data,
                        )
                        Result.Success(transactionToSend.encode())
                    }
                }
            }
        }
    }

    private suspend fun sign(
        transactionToSign: ByteArray,
        signer: TransactionSigner,
        publicKey: Wallet.PublicKey,
    ): Result<ByteArray> {
        return when (val result = signer.sign(transactionToSign, publicKey)) {
            is CompletionResult.Success -> {
                val unmarshalledSignature = if (publicKey == dummySigner.publicKey) {
                    result.data + ByteArray(1)
                } else {
                    UnmarshalHelper().unmarshalSignatureEVMLegacy(result.data, transactionToSign, publicKey)
                }
                Result.Success(unmarshalledSignature)
            }

            is CompletionResult.Failure -> {
                Result.fromTangemSdkError(result.error)
            }
        }
    }

    private suspend fun getEnergyFee(amount: Amount, destination: String): Result<Int> {
        val token = when (amount.type) {
            AmountType.Coin -> return Result.Success(0)
            is AmountType.Token -> amount.type.token
            else -> return Result.Failure(BlockchainSdkError.FailedToLoadFee)
        }
        val addressData = destination.decodeBase58(checked = true)
            ?.padLeft(TRON_BYTE_ARRAY_PADDING_SIZE)
            ?: byteArrayOf()

        val amountData = amount.bigIntegerValue()
            ?.toByteArray()
            ?.padLeft(TRON_BYTE_ARRAY_PADDING_SIZE)
            ?: return Result.Failure(BlockchainSdkError.FailedToLoadFee)

        val parameter = (addressData + amountData).toHexString()

        return coroutineScope {
            val energyUseDef = async {
                networkService.getMaxEnergyUse(
                    address = wallet.address,
                    contractAddress = token.contractAddress,
                    parameter = parameter,
                )
            }
            val chainParametersDef = async { networkService.getChainParameters() }

            val energyUse = when (val energyUseResult = energyUseDef.await()) {
                is Result.Failure -> return@coroutineScope Result.Failure(energyUseResult.error)
                is Result.Success -> energyUseResult.data
            }
            val chainParameters = when (val chainParametersResult = chainParametersDef.await()) {
                is Result.Failure -> return@coroutineScope Result.Failure(chainParametersResult.error)
                is Result.Success -> chainParametersResult.data
            }

            // Contract's energy fee changes every maintenance period (6 hours) and since we don't know what period
            // the transaction is going to be executed in we increase the fee just in case by 20%
            val sunPerEnergyUnit = chainParameters.sunPerEnergyUnit
            val energyFee = (energyUse * sunPerEnergyUnit).toDouble()
            val dynamicEnergyIncreaseFactor =
                chainParameters.dynamicIncreaseFactor.toDouble() / ENERGY_FACTOR_PRECISION
            val conservativeEnergyFee = (energyFee * (1 + dynamicEnergyIncreaseFactor)).toInt()

            Result.Success(conservativeEnergyFee)
        }
    }

    /**
     * Creates new ByteArray with given [length] and copy content of initial ByteArray content to new one.
     */
    private fun ByteArray.padLeft(length: Int): ByteArray {
        val paddingSize = Ints.max(length - this.size, 0)
        return ByteArray(paddingSize) + this
    }

    override suspend fun getAllowance(spenderAddress: String, token: Token): Result<BigDecimal> {
        return networkService.getAllowance(wallet.address, token, spenderAddress)
    }

    override fun getApproveData(spenderAddress: String, value: Amount?): String {
        return createTrc20ApproveDataHex(spenderAddress, value)
    }

    private companion object {
        /**
         * Value taken from [TIP-491](https://github.com/tronprotocol/tips/issues/491)
         */
        const val ENERGY_FACTOR_PRECISION = 10_000
    }
}
