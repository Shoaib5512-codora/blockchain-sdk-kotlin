package com.tangem.blockchain.blockchains.ethereum.providers

import com.tangem.blockchain.blockchains.ethereum.network.EthereumJsonRpcProvider
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.network.providers.OnlyPublicProvidersBuilder
import com.tangem.blockchain.common.network.providers.ProviderType

internal class OctaSpaceProvidersBuilder(
    override val providerTypes: List<ProviderType>,
) : OnlyPublicProvidersBuilder<EthereumJsonRpcProvider>(providerTypes) {

    override fun createProvider(url: String, blockchain: Blockchain) = EthereumJsonRpcProvider(url)
}
