package com.tangem.blockchain.blockchains.ethereum.providers

import com.tangem.blockchain.blockchains.ethereum.EthereumLikeProvidersBuilder
import com.tangem.blockchain.blockchains.ethereum.network.EthereumJsonRpcProvider
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.BlockchainSdkConfig
import com.tangem.blockchain.common.network.providers.ProviderType

internal class ArbitrumProvidersBuilder(
    override val providerTypes: List<ProviderType>,
    override val config: BlockchainSdkConfig,
) : EthereumLikeProvidersBuilder(config) {

    override fun createProviders(blockchain: Blockchain): List<EthereumJsonRpcProvider> {
        return providerTypes.mapNotNull {
            when (it) {
                is ProviderType.Public -> createPublicProvider(url = it.url)
                ProviderType.NowNodes -> {
                    ethereumProviderFactory.getNowNodesProvider(baseUrl = "https://arbitrum.nownodes.io/")
                }
                ProviderType.EthereumLike.Infura -> {
                    ethereumProviderFactory.getInfuraProvider(baseUrl = "https://arbitrum-mainnet.infura.io/v3/")
                }
                else -> null
            }
        }
    }

    override fun createTestnetProviders(blockchain: Blockchain): List<EthereumJsonRpcProvider> {
        return listOf(
            EthereumJsonRpcProvider(baseUrl = "https://goerli-rollup.arbitrum.io/rpc/"),
        )
    }

    private fun createPublicProvider(url: String): EthereumJsonRpcProvider {
        return EthereumJsonRpcProvider.createWithPostfixIfContained(baseUrl = url, postfixUrl = BASE_URL_LAST_PATH)
    }

    private companion object {
        const val BASE_URL_LAST_PATH = "arb"
    }
}
