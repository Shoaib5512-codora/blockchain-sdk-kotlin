package com.tangem.blockchain.blockchains.ethereum.providers

import com.tangem.blockchain.blockchains.ethereum.EthereumLikeProvidersBuilder
import com.tangem.blockchain.blockchains.ethereum.network.EthereumJsonRpcProvider
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.BlockchainSdkConfig
import com.tangem.blockchain.common.network.providers.ProviderType

internal class PolygonZkEVMProvidersBuilder(
    override val providerTypes: List<ProviderType>,
    override val config: BlockchainSdkConfig,
) : EthereumLikeProvidersBuilder(config) {

    override fun createProviders(blockchain: Blockchain): List<EthereumJsonRpcProvider> {
        return providerTypes.mapNotNull {
            when (it) {
                is ProviderType.Public -> createPublicProvider(url = it.url)
                ProviderType.GetBlock -> ethereumProviderFactory.getGetBlockProvider { polygonZkEvm?.jsonRpc }
                else -> null
            }
        }
    }

    override fun createTestnetProviders(blockchain: Blockchain): List<EthereumJsonRpcProvider> {
        return listOf("https://rpc.cardona.zkevm-rpc.com/")
            .map(::EthereumJsonRpcProvider)
    }

    private fun createPublicProvider(url: String): EthereumJsonRpcProvider {
        return EthereumJsonRpcProvider.createWithPostfixIfContained(baseUrl = url, postfixUrl = BASE_URL_LAST_PATH)
    }

    private companion object {
        const val BASE_URL_LAST_PATH = "zkevm"
    }
}
