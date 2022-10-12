package com.tangem.blockchain.blockchains.polkadot.polkaj.extensions

import io.emeraldpay.polkaj.ss58.SS58Type
import io.emeraldpay.polkaj.types.DotAmount
import io.emeraldpay.polkaj.types.Units
import java.math.BigDecimal

/**
 * Created by Anton Zhilenkov on 30/08/2022.
 */
// https://support.polkadot.network/support/solutions/articles/65000168651-what-is-the-existential-deposit-
val SS58Type.Network.existentialDeposit: BigDecimal
    get() = when (this) {
        SS58Type.Network.POLKADOT -> BigDecimal.ONE
        SS58Type.Network.WESTEND -> 0.01.toBigDecimal()
        SS58Type.Network.KUSAMA -> 0.000033333333.toBigDecimal()
        else -> throw UnsupportedOperationException()
    }

val SS58Type.Network.hosts: List<String>
    get() = when (this) {
        SS58Type.Network.POLKADOT -> listOf(
            "https://rpc.polkadot.io/",
            "https://polkadot.api.onfinality.io/public-ws/",
            "https://polkadot-rpc.dwellir.com/",
        )
        SS58Type.Network.WESTEND -> listOf(
            "https://westend-rpc.polkadot.io/"
        )
        SS58Type.Network.KUSAMA -> listOf(
            "https://kusama-rpc.polkadot.io/",
            "https://kusama.api.onfinality.io/public-ws/",
            "https://kusama-rpc.dwellir.com/",
        )
        else -> throw UnsupportedOperationException()
    }

val SS58Type.Network.amountUnits: Units
    get() = DotAmount.getUnitsForNetwork(this)

fun SS58Type.Network.isPolkadot(): Boolean {
    return this.value == SS58Type.Network.POLKADOT.value
}

fun SS58Type.Network.isWestend(): Boolean {
    return this.value == SS58Type.Network.KUSAMA.value
}

fun SS58Type.Network.isKusama(): Boolean {
    return this.value == SS58Type.Network.WESTEND.value
}
