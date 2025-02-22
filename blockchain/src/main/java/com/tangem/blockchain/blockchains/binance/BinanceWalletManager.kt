package com.tangem.blockchain.blockchains.binance

import android.util.Log
import com.tangem.blockchain.blockchains.binance.network.BinanceInfoResponse
import com.tangem.blockchain.blockchains.binance.network.BinanceNetworkProvider
import com.tangem.blockchain.common.*
import com.tangem.blockchain.common.transaction.Fee
import com.tangem.blockchain.common.transaction.TransactionFee
import com.tangem.blockchain.common.transaction.TransactionSendResult
import com.tangem.blockchain.extensions.Result
import com.tangem.common.CompletionResult

class BinanceWalletManager(
    wallet: Wallet,
    private val transactionBuilder: BinanceTransactionBuilder,
    private val networkProvider: BinanceNetworkProvider,
) : WalletManager(wallet), TransactionSender {

    private val blockchain = wallet.blockchain

    override val currentHost: String get() = networkProvider.baseUrl

    override suspend fun updateInternal() {
        when (val result = networkProvider.getInfo(wallet.address)) {
            is Result.Success -> updateWallet(result.data)
            is Result.Failure -> updateError(result.error)
        }
    }

    private fun updateWallet(response: BinanceInfoResponse) {
        val coinBalance = response.balances[blockchain.currency] ?: 0.toBigDecimal()
        Log.d(this::class.java.simpleName, "Balance is $coinBalance")
        wallet.setCoinValue(coinBalance)

        cardTokens.forEach {
            val tokenBalance = response.balances[it.contractAddress] ?: 0.toBigDecimal()
            wallet.addTokenValue(tokenBalance, it)
        }

        transactionBuilder.accountNumber = response.accountNumber
        transactionBuilder.sequence = response.sequence
    }

    private fun updateError(error: BlockchainError) {
        Log.e(this::class.java.simpleName, error.customMessage)
        if (error is BlockchainSdkError) throw error
    }

    override suspend fun send(
        transactionData: TransactionData,
        signer: TransactionSigner,
    ): Result<TransactionSendResult> {
        return when (val buildTransactionResult = transactionBuilder.buildToSign(transactionData)) {
            is Result.Failure -> buildTransactionResult
            is Result.Success -> {
                when (val signerResponse = signer.sign(buildTransactionResult.data, wallet.publicKey)) {
                    is CompletionResult.Success -> {
                        val transactionToSend = transactionBuilder.buildToSend(signerResponse.data)
                        when (val result = networkProvider.sendTransaction(transactionToSend)) {
                            is Result.Success -> {
                                transactionData.hash = result.data
                                wallet.addOutgoingTransaction(transactionData)
                                Result.Success(TransactionSendResult(result.data))
                            }
                            is Result.Failure -> return result
                        }
                    }
                    is CompletionResult.Failure -> Result.fromTangemSdkError(signerResponse.error)
                }
            }
        }
    }

    override suspend fun getFee(amount: Amount, destination: String): Result<TransactionFee> {
        return when (val result = networkProvider.getFee()) {
            is Result.Success -> Result.Success(TransactionFee.Single(Fee.Common(Amount(result.data, blockchain))))
            is Result.Failure -> result
        }
    }
}
