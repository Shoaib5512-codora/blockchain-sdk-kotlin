package com.tangem.blockchain.blockchains.polkadot.polkaj.extentions

import io.emeraldpay.polkaj.scaletypes.AccountInfo
import io.emeraldpay.polkaj.ss58.SS58Type
import java.math.BigDecimal

/**
 * Created by Anton Zhilenkov on 05/09/2022.
 */
fun AccountInfo.balance(network: SS58Type.Network): BigDecimal {
    return data.free.toBigDecimal(network)
}