package com.tangem.blockchain.blockchains.vechain.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class VechainGetAccountResponse(
    @Json(name = "balance") val balance: String,
    @Json(name = "energy") val energy: String,
)

@JsonClass(generateAdapter = true)
internal data class VechainLatestBlockResponse(
    @Json(name = "number") val number: Long,
    @Json(name = "id") val blockId: String,
)

@JsonClass(generateAdapter = true)
internal data class VechainCommitTransactionRequest(@Json(name = "raw") val raw: String)

@JsonClass(generateAdapter = true)
internal data class VechainCommitTransactionResponse(@Json(name = "id") val txId: String)

@JsonClass(generateAdapter = true)
internal data class VechainTransactionInfoResponse(@Json(name = "id") val txId: String)

internal data class VechainContractCallRequest(
    @Json(name = "clauses") val clauses: List<VechainClause>,
    @Json(name = "caller") val caller: String?,
    @Json(name = "gas") val gas: Int?,
)

@JsonClass(generateAdapter = true)
internal data class VechainClause(
    @Json(name = "to") val to: String,
    @Json(name = "value") val value: String,
    @Json(name = "data") val data: String,
)

internal data class VechainContractCallResponse(
    @Json(name = "data") val data: String?,
    @Json(name = "gasUsed") val gasUsed: Long?,
    @Json(name = "vmError") val vmError: String?,
)
