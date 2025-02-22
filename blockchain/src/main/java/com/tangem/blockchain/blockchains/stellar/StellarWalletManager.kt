package com.tangem.blockchain.blockchains.stellar

import android.util.Log
import com.tangem.blockchain.common.*
import com.tangem.blockchain.common.transaction.Fee
import com.tangem.blockchain.common.transaction.TransactionFee
import com.tangem.blockchain.common.transaction.TransactionSendResult
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.SimpleResult
import com.tangem.blockchain.extensions.successOr
import com.tangem.common.CompletionResult
import com.tangem.common.extensions.toHexString
import java.math.BigDecimal

class StellarWalletManager(
    wallet: Wallet,
    private val transactionBuilder: StellarTransactionBuilder,
    private val networkProvider: StellarNetworkProvider,
) : WalletManager(wallet), TransactionSender, SignatureCountValidator, ReserveAmountProvider {

    override val currentHost: String
        get() = networkProvider.baseUrl

    private val blockchain = wallet.blockchain

    private var baseFee = BASE_FEE
    private var baseReserve = BASE_RESERVE
    private var sequence = 0L

    override suspend fun updateInternal() {
        when (val result = networkProvider.getInfo(wallet.address)) {
            is Result.Failure -> updateError(result.error)
            is Result.Success -> updateWallet(result.data)
        }
    }

    private fun updateWallet(data: StellarResponse) { // TODO: rework reserve
        val reserve = data.baseReserve * (2 + data.subEntryCount).toBigDecimal()
        wallet.setCoinValue(data.coinBalance - reserve)
        wallet.setReserveValue(reserve)
        sequence = data.sequence
        baseFee = data.baseFee
        baseReserve = data.baseReserve
        transactionBuilder.minReserve = data.baseReserve * 2.toBigDecimal()

        cardTokens.forEach { token ->
            val tokenBalance = data.tokenBalances
                .find { it.symbol == token.symbol && it.issuer == token.contractAddress }?.balance
                ?: 0.toBigDecimal()
            wallet.addTokenValue(tokenBalance, token)
        }
        // only if no token(s) specified on manager creation or stored on card
        if (cardTokens.isEmpty()) updateUnplannedTokens(data.tokenBalances)

        updateRecentTransactions(data.recentTransactions)
    }

    private fun updateUnplannedTokens(balances: Set<StellarAssetBalance>) {
        balances.forEach {
            val token = Token(it.symbol, it.issuer, blockchain.decimals())
            wallet.addTokenValue(it.balance, token)
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
        val hash = transactionBuilder.buildToSign(transactionData, sequence).successOr {
            return Result.Failure(it.error)
        }

        return when (val signerResponse = signer.sign(hash, wallet.publicKey)) {
            is CompletionResult.Success -> {
                val transactionToSend = transactionBuilder.buildToSend(signerResponse.data)
                when (val sendResult = networkProvider.sendTransaction(transactionToSend)) {
                    is SimpleResult.Failure -> Result.Failure(sendResult.error)
                    SimpleResult.Success -> {
                        val txHash = transactionBuilder.getTransactionHash().toHexString()
                        transactionData.hash = txHash
                        wallet.addOutgoingTransaction(transactionData)
                        Result.Success(TransactionSendResult(txHash))
                    }
                }
            }
            is CompletionResult.Failure -> Result.fromTangemSdkError(signerResponse.error)
        }
    }

    override suspend fun getFee(amount: Amount, destination: String): Result<TransactionFee> {
        val feeStats = networkProvider.getFeeStats().successOr { return it }

        val maxChargedFee = feeStats.feeCharged.max.toBigDecimal().movePointLeft(blockchain.decimals())
        val minChargedFee = feeStats.feeCharged.min.toBigDecimal().movePointLeft(blockchain.decimals())
        val averageChargedFee = (maxChargedFee - minChargedFee).divide(2.toBigDecimal()) + minChargedFee

        return Result.Success(
            TransactionFee.Choosable(
                minimum = Fee.Common(Amount(minChargedFee, blockchain)),
                normal = Fee.Common(Amount(averageChargedFee, blockchain)),
                priority = Fee.Common(Amount(maxChargedFee, blockchain)),
            ),
        )
    }

    override suspend fun validateSignatureCount(signedHashes: Int): SimpleResult {
        return when (val result = networkProvider.getSignatureCount(wallet.address)) {
            is Result.Success -> if (result.data == signedHashes) {
                SimpleResult.Success
            } else {
                SimpleResult.Failure(BlockchainSdkError.SignatureCountNotMatched)
            }
            is Result.Failure -> SimpleResult.Failure(result.error)
        }
    }

    override fun getReserveAmount(): BigDecimal = transactionBuilder.minReserve

    override suspend fun isAccountFunded(destinationAddress: String): Boolean {
        return when (val response = networkProvider.checkTargetAccount(destinationAddress, null)) {
            is Result.Success -> response.data.accountCreated
            is Result.Failure -> false
        }
    }

    companion object {
        val BASE_FEE = 0.00001.toBigDecimal()
        val BASE_RESERVE = 0.5.toBigDecimal()
    }
}
