package com.tangem.blockchain.blockchains.hedera.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class HederaAccountResponse(
    @Json(name = "accounts")
    val accounts: List<HederaAccount>,
)

@JsonClass(generateAdapter = true)
internal data class HederaExchangeRateResponse(
    @Json(name = "current_rate")
    val currentRate: HederaRate,

    @Json(name = "next_rate")
    val nextRate: HederaRate,
)

internal data class HederaAccount(
    @Json(name = "account")
    val account: String,
)

@JsonClass(generateAdapter = true)
internal data class HederaRate(
    @Json(name = "cent_equivalent")
    val centEquivalent: String,

    @Json(name = "hbar_equivalent")
    val hbarEquivalent: String,

    @Json(name = "expiration_time")
    val expirationTime: String,
)

@JsonClass(generateAdapter = true)
internal data class HederaBalancesResponse(@Json(name = "balances") val balances: List<HederaBalanceResponse>)

@JsonClass(generateAdapter = true)
internal data class HederaBalanceResponse(
    @Json(name = "account")
    val account: String,

    @Json(name = "balance")
    val balance: Long,

    @Json(name = "tokens")
    val tokenBalances: List<HederaTokenBalanceResponse>,
)

@JsonClass(generateAdapter = true)
internal data class HederaTokenBalanceResponse(
    @Json(name = "token_id")
    val tokenId: String,

    @Json(name = "balance")
    val balance: Long,
)

@JsonClass(generateAdapter = true)
internal data class HederaTransactionsResponse(
    @Json(name = "transactions")
    val transactions: List<HederaTransactionResponse>,
)

@JsonClass(generateAdapter = true)
internal data class HederaTransactionResponse(
    @Json(name = "transaction_hash")
    val transactionHash: String,

    @Json(name = "transaction_id")
    val transactionId: String,

    @Json(name = "result")
    val result: String,
)
