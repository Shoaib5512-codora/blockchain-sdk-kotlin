package com.tangem.blockchain.blockchains.polkadot.polkaj.extentions

import io.emeraldpay.polkaj.ss58.SS58Type
import io.emeraldpay.polkaj.types.DotAmount
import java.math.BigDecimal

/**
 * Created by Anton Zhilenkov on 04/09/2022.
 */
fun DotAmount.toBigDecimal(network: SS58Type.Network): BigDecimal {
    val decimals = network.amountUnits.main.decimals
    return if (decimals == 0) {
        value.toBigDecimal()
    } else {
        value.toBigDecimal().movePointLeft(decimals)
    }
}

fun BigDecimal.toDot(network: SS58Type.Network): DotAmount {
    return DotAmount.from(this.toDouble(), network.amountUnits)
}