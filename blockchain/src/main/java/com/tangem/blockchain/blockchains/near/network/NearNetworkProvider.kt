package com.tangem.blockchain.blockchains.near.network

import com.tangem.blockchain.extensions.Result

/**
 * @author Anton Zhilenkov on 01.08.2023.
 */
interface NearNetworkProvider {

    val host: String

    suspend fun getAccount(address: String): Result<ViewAccountResult>

    suspend fun getGas(blockHeight: Long): Result<GasPriceResult>

    suspend fun getTransactionStatus(txHash: String, senderAccountId: String): Result<TransactionStatusResult>

    suspend fun sendTransaction(signedTxBase64: String): Result<SendTransactionAsyncResult>
}