package com.tangem.blockchain.blockchains.ethereum.providers

import com.tangem.blockchain.blockchains.ethereum.network.EthereumJsonRpcProvider
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.network.providers.OnlyPublicProvidersBuilder
import com.tangem.blockchain.common.network.providers.ProviderType

internal class FlareProvidersBuilder(
    override val providerTypes: List<ProviderType>,
) : OnlyPublicProvidersBuilder<EthereumJsonRpcProvider>(
    providerTypes = providerTypes,
    testnetProviders = listOf("https://coston2-api.flare.network/ext/C/rpc/"),
) {

    override fun createProvider(url: String, blockchain: Blockchain): EthereumJsonRpcProvider {
        return EthereumJsonRpcProvider.createWithPostfixIfContained(
            url = url,
            postfixUrl = BASE_URL_LAST_PATHS.toTypedArray(),
        )
    }

    private companion object {
        val BASE_URL_LAST_PATHS = listOf("ext/C/rpc", "ext/bc/C/rpc")
    }
}
