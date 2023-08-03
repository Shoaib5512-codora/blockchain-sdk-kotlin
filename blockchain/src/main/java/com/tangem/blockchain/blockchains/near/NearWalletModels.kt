package com.tangem.blockchain.blockchains.near

import java.math.BigDecimal

/**
 * @author Anton Zhilenkov on 03.08.2023.
 */
data class NearWalletInfo(
    val amount: BigDecimal,
    val blockHash: String,
    val blockHeight: Long,
)

data class NearGasPrice(
    val gasPrice: BigDecimal,
    val blockHeight: Long,
)

data class SendTransactionResult(
    val hash: String,
)