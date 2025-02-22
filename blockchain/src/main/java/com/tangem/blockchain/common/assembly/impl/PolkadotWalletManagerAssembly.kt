package com.tangem.blockchain.common.assembly.impl

import com.tangem.blockchain.blockchains.polkadot.PolkadotWalletManager
import com.tangem.blockchain.blockchains.polkadot.network.PolkadotNetworkProvider
import com.tangem.blockchain.blockchains.polkadot.network.PolkadotNetworkService
import com.tangem.blockchain.blockchains.polkadot.network.accounthealthcheck.PolkadotAccountHealthCheckNetworkService
import com.tangem.blockchain.blockchains.polkadot.providers.*
import com.tangem.blockchain.blockchains.polkadot.providers.AlephZeroProvidersBuilder
import com.tangem.blockchain.blockchains.polkadot.providers.JoyStreamProvidersBuilder
import com.tangem.blockchain.blockchains.polkadot.providers.KusamaProvidersBuilder
import com.tangem.blockchain.blockchains.polkadot.providers.PolkadotProvidersBuilder
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.BlockchainSdkConfig
import com.tangem.blockchain.common.assembly.WalletManagerAssembly
import com.tangem.blockchain.common.assembly.WalletManagerAssemblyInput
import com.tangem.blockchain.common.network.providers.NetworkProvidersBuilder
import com.tangem.blockchain.common.network.providers.ProviderType

internal object PolkadotWalletManagerAssembly : WalletManagerAssembly<PolkadotWalletManager>() {

    override fun make(input: WalletManagerAssemblyInput): PolkadotWalletManager {
        val healthCheckService = input.wallet.blockchain.getPolkadotExtrinsicCheckHost()?.let {
            PolkadotAccountHealthCheckNetworkService(it)
        }
        return with(input.wallet) {
            PolkadotWalletManager(
                wallet = this,
                networkProvider = PolkadotNetworkService(
                    providers = getNetworkProvidersBuilder(
                        providerTypes = input.providerTypes,
                        config = input.config,
                        blockchain = blockchain,
                    ).build(blockchain),
                ),
                extrinsicCheckNetworkProvider = healthCheckService,
            )
        }
    }

    private fun getNetworkProvidersBuilder(
        providerTypes: List<ProviderType>,
        config: BlockchainSdkConfig,
        blockchain: Blockchain,
    ): NetworkProvidersBuilder<PolkadotNetworkProvider> {
        return when (blockchain) {
            Blockchain.Polkadot, Blockchain.PolkadotTestnet -> PolkadotProvidersBuilder(providerTypes)
            Blockchain.AlephZero, Blockchain.AlephZeroTestnet -> AlephZeroProvidersBuilder(providerTypes)
            Blockchain.Kusama -> KusamaProvidersBuilder(providerTypes)
            Blockchain.Joystream -> JoyStreamProvidersBuilder(providerTypes)
            Blockchain.Bittensor -> BittensorProvidersBuilder(providerTypes, config)
            else -> error("$blockchain isn't supported")
        }
    }

    private fun Blockchain.getPolkadotExtrinsicCheckHost(): String? {
        return when (this) {
            Blockchain.Polkadot -> "https://polkadot.api.subscan.io/"
            else -> null
        }
    }
}
