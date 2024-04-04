package com.tangem.blockchain.blockchains.xdc

import com.tangem.blockchain.blockchains.ethereum.EthereumLikeProvidersBuilder
import com.tangem.blockchain.blockchains.ethereum.network.EthereumJsonRpcProvider
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.BlockchainSdkConfig

internal class XDCProvidersBuilder(
    override val config: BlockchainSdkConfig,
) : EthereumLikeProvidersBuilder(config) {

    override val supportedBlockchains: List<Blockchain> = listOf(Blockchain.XDC, Blockchain.XDCTestnet)

    override fun createProviders(blockchain: Blockchain): List<EthereumJsonRpcProvider> {
        return if (blockchain.isTestnet()) {
            listOf(
                EthereumJsonRpcProvider(baseUrl = "https://rpc.apothem.network/"),
            )
        } else {
            listOfNotNull(
                ethereumProviderFactory.getNowNodesProvider(baseUrl = "https://xdc.nownodes.io/"),
                EthereumJsonRpcProvider(baseUrl = "https://rpc.xdcrpc.com"),
                EthereumJsonRpcProvider(baseUrl = "https://erpc.xdcrpc.com"),
                EthereumJsonRpcProvider(baseUrl = "https://rpc.xinfin.network"),
                EthereumJsonRpcProvider(baseUrl = "https://erpc.xinfin.network"),
                EthereumJsonRpcProvider(baseUrl = "https://rpc.xdc.org"),
                EthereumJsonRpcProvider(baseUrl = "https://rpc.ankr.com/xdc/"),
                EthereumJsonRpcProvider(baseUrl = "https://rpc1.xinfin.network"),
            )
        }
    }
}
