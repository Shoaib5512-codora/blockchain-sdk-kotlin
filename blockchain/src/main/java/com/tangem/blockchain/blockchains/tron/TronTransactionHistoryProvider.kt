package com.tangem.blockchain.blockchains.tron

import com.tangem.Log
import com.tangem.blockchain.common.*
import com.tangem.blockchain.common.pagination.Page
import com.tangem.blockchain.common.pagination.PaginationWrapper
import com.tangem.blockchain.common.txhistory.TransactionHistoryItem
import com.tangem.blockchain.common.txhistory.TransactionHistoryItem.TransactionStatus
import com.tangem.blockchain.common.txhistory.TransactionHistoryProvider
import com.tangem.blockchain.common.txhistory.TransactionHistoryRequest
import com.tangem.blockchain.common.txhistory.TransactionHistoryState
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.network.blockbook.network.BlockBookApi
import com.tangem.blockchain.network.blockbook.network.responses.GetAddressResponse
import com.tangem.common.extensions.guard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

private const val PREFIX = "0x"

internal class TronTransactionHistoryProvider(
    private val blockchain: Blockchain,
    private val blockBookApi: BlockBookApi,
) : TransactionHistoryProvider {
    override suspend fun getTransactionHistoryState(
        address: String,
        filterType: TransactionHistoryRequest.FilterType,
    ): TransactionHistoryState {
        return try {
            val response = withContext(Dispatchers.IO) {
                blockBookApi.getTransactions(
                    address = address,
                    page = null,
                    pageSize = 1, // We don't need to know all transactions to define state
                    filterType = filterType,
                )
            }
            if (!response.transactions.isNullOrEmpty()) {
                TransactionHistoryState.Success.HasTransactions(response.transactions.size)
            } else {
                TransactionHistoryState.Success.Empty
            }
        } catch (e: Exception) {
            TransactionHistoryState.Failed.FetchError(e)
        }
    }

    override suspend fun getTransactionsHistory(
        request: TransactionHistoryRequest,
    ): Result<PaginationWrapper<TransactionHistoryItem>> {
        return try {
            val response =
                withContext(Dispatchers.IO) {
                    blockBookApi.getTransactions(
                        address = request.address,
                        page = request.pageToLoad,
                        pageSize = request.pageSize,
                        filterType = request.filterType,
                    )
                }
            val pageToLoad = request.pageToLoad?.toIntOrNull() ?: 0
            val page = response.page ?: 0

            // If response page is lower than request page, it means that we reached end. NowNodes works not
            // correctly.
            val isEndReached = page < pageToLoad
            if (isEndReached) {
                Result.Success(
                    PaginationWrapper(
                        nextPage = Page.LastPage,
                        items = emptyList(),
                    ),
                )
            } else {
                val txs = response.transactions
                    .orEmpty()
                    .mapNotNull { tx ->
                        tx.toTransactionHistoryItem(
                            walletAddress = request.address,
                            decimals = request.decimals,
                            filterType = request.filterType,
                        )
                    }
                val nextPage = if (response.page != null && request.page !is Page.LastPage) {
                    Page.Next(response.page.inc().toString())
                } else {
                    Page.LastPage
                }
                Result.Success(
                    PaginationWrapper(
                        nextPage = nextPage,
                        items = txs,
                    ),
                )
            }
        } catch (e: Exception) {
            Result.Failure(e.toBlockchainSdkError())
        }
    }

    private fun GetAddressResponse.Transaction.toTransactionHistoryItem(
        walletAddress: String,
        decimals: Int,
        filterType: TransactionHistoryRequest.FilterType,
    ): TransactionHistoryItem? {
        val destinationType = extractDestinationType(this, filterType).guard {
            Log.info { "Transaction $this doesn't contain a required value" }
            return null
        }
        val amount = extractAmount(tx = this, decimals = decimals, filterType = filterType).guard {
            Log.info { "Transaction $this doesn't contain a required value" }
            return null
        }
        val sourceType = extractSourceType(tx = this, filterType = filterType).guard {
            Log.info { "Transaction $this doesn't contain a required value" }
            return null
        }
        return TransactionHistoryItem(
            txHash = txid.removePrefix(PREFIX),
            timestamp = TimeUnit.SECONDS.toMillis(blockTime.toLong()),
            isOutgoing = isOutgoing(walletAddress, sourceType),
            destinationType = destinationType,
            sourceType = sourceType,
            status = if (confirmations > 0) TransactionStatus.Confirmed else TransactionStatus.Unconfirmed,
            type = extractType(filterType = filterType, tx = this),
            amount = amount,
        )
    }

    private fun isOutgoing(walletAddress: String, sourceType: TransactionHistoryItem.SourceType): Boolean {
        return when (sourceType) {
            is TransactionHistoryItem.SourceType.Multiple -> {
                sourceType.addresses.any { it.equals(walletAddress, ignoreCase = true) }
            }
            is TransactionHistoryItem.SourceType.Single -> {
                sourceType.address.equals(walletAddress, ignoreCase = true)
            }
        }
    }

    private fun extractDestinationType(
        tx: GetAddressResponse.Transaction,
        filterType: TransactionHistoryRequest.FilterType,
    ): TransactionHistoryItem.DestinationType? {
        tx.toAddress ?: return null
        tx.fromAddress ?: return null
        return when (filterType) {
            TransactionHistoryRequest.FilterType.Coin -> {
                TransactionHistoryItem.DestinationType.Single(
                    addressType = if (tx.tokenTransfers.isEmpty()) {
                        TransactionHistoryItem.AddressType.User(tx.toAddress)
                    } else {
                        TransactionHistoryItem.AddressType.Contract(tx.toAddress)
                    },
                )
            }

            is TransactionHistoryRequest.FilterType.Contract -> {
                val transfer = tx.getTokenTransfer(filterType.address) ?: return null
                TransactionHistoryItem.DestinationType.Single(
                    addressType = TransactionHistoryItem.AddressType.User(transfer.to),
                )
            }
        }
    }

    private fun extractSourceType(
        tx: GetAddressResponse.Transaction,
        filterType: TransactionHistoryRequest.FilterType,
    ): TransactionHistoryItem.SourceType? {
        val address = when (filterType) {
            TransactionHistoryRequest.FilterType.Coin -> tx.fromAddress
            is TransactionHistoryRequest.FilterType.Contract -> {
                tx.getTokenTransfer(filterType.address)?.from
            }
        }.guard { return null }

        return TransactionHistoryItem.SourceType.Single(address = address)
    }

    private fun extractType(
        filterType: TransactionHistoryRequest.FilterType,
        tx: GetAddressResponse.Transaction,
    ): TransactionHistoryItem.TransactionType {
        return when (filterType) {
            TransactionHistoryRequest.FilterType.Coin -> {
                if (tx.isContractInteraction()) {
                    TransactionHistoryItem.TransactionType.ContractMethod(id = tx.contractAddress.orEmpty())
                } else {
                    TransactionHistoryItem.TransactionType.Transfer
                }
            }
            is TransactionHistoryRequest.FilterType.Contract -> {
                // All TRC10 and TRC20 token transactions are considered simple & plain transfers
                TransactionHistoryItem.TransactionType.Transfer
            }
        }
    }

    private fun extractAmount(
        tx: GetAddressResponse.Transaction,
        decimals: Int,
        filterType: TransactionHistoryRequest.FilterType,
    ): Amount? {
        return when (filterType) {
            TransactionHistoryRequest.FilterType.Coin -> Amount(
                value = BigDecimal(tx.value).movePointLeft(blockchain.decimals()),
                blockchain = blockchain,
                type = AmountType.Coin,
            )

            is TransactionHistoryRequest.FilterType.Contract -> {
                val transfer = tx.getTokenTransfer(filterType.address) ?: return null
                val transferValue = transfer.value ?: "0"
                val token = Token(
                    name = transfer.name.orEmpty(),
                    symbol = transfer.symbol.orEmpty(),
                    contractAddress = transfer.token.orEmpty(),
                    decimals = decimals,
                )
                Amount(value = BigDecimal(transferValue).movePointLeft(decimals), token = token)
            }
        }
    }

    private fun GetAddressResponse.Transaction.getTokenTransfer(
        contractAddress: String,
    ): GetAddressResponse.Transaction.TokenTransfer? {
        return tokenTransfers.firstOrNull { contractAddress.equals(it.token, ignoreCase = true) }
    }

    private fun GetAddressResponse.Transaction.isContractInteraction(): Boolean = contractType != null &&
        contractType != TRANSFER_CONTRACT_TYPE &&
        contractType != TRANSFER_ASSET_CONTRACT_TYPE

    private companion object {
        private const val TRANSFER_CONTRACT_TYPE = 1
        private const val TRANSFER_ASSET_CONTRACT_TYPE = 2
    }
}
