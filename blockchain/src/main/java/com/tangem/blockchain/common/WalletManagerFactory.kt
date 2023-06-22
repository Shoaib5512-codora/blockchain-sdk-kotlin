package com.tangem.blockchain.common

import com.tangem.blockchain.common.assembly.WalletManagerAssemblyInput
import com.tangem.blockchain.common.assembly.impl.*
import com.tangem.common.card.EllipticCurve
import com.tangem.crypto.hdWallet.DerivationPath
import com.tangem.crypto.hdWallet.bip32.ExtendedPublicKey

class WalletManagerFactory(
    private val config: BlockchainSdkConfig = BlockchainSdkConfig()
) {

    /**
     * Base wallet manager initializer
     * @param blockchain: blockchain to create. If null, card native blockchain will be used
     * @param seedKey: Public Key of the wallet
     * @param derivedKey: Derived ExtendedPublicKey by the card
     * @param derivation: derivation style or derivation path
     */
    fun makeWalletManager(
        blockchain: Blockchain,
        seedKey: ByteArray,
        derivedKey: ExtendedPublicKey,
        derivation: DerivationParams,
    ): WalletManager? {
        val derivationPath: DerivationPath? = when (derivation) {
            is DerivationParams.Custom -> derivation.path
            is DerivationParams.Default -> blockchain.derivationPath(derivation.style)
        }

        return makeWalletManager(
            blockchain = blockchain,
            publicKey = Wallet.PublicKey(
                seedKey = seedKey,
                derivedKey = derivedKey.publicKey,
                derivationPath = derivationPath
            )
        )
    }

    /**
     * Creates manager initializer for twin cards
     */
    fun makeTwinWalletManager(
        walletPublicKey: ByteArray,
        pairPublicKey: ByteArray,
        blockchain: Blockchain = Blockchain.Bitcoin,
        curve: EllipticCurve = EllipticCurve.Secp256k1,
    ): WalletManager? {
        return makeWalletManager(
            blockchain = blockchain,
            publicKey = Wallet.PublicKey(walletPublicKey, null, null),
            pairPublicKey = pairPublicKey,
            curve = curve
        )
    }

    /**
     * Legacy wallet manager initializer
     */
    fun makeWalletManager(
        blockchain: Blockchain,
        walletPublicKey: ByteArray,
        curve: EllipticCurve = EllipticCurve.Secp256k1,
    ): WalletManager? {
        return makeWalletManager(
            blockchain = blockchain,
            publicKey = Wallet.PublicKey(walletPublicKey, null, null),
            curve = curve
        )
    }

    fun makeWalletManager(
        blockchain: Blockchain,
        publicKey: Wallet.PublicKey,
        tokens: Collection<Token> = emptyList(),
        pairPublicKey: ByteArray? = null,
        curve: EllipticCurve = EllipticCurve.Secp256k1,
    ): WalletManager? {
        if (checkIfWrongKey(curve, publicKey)) return null

        val addresses = blockchain.makeAddresses(publicKey.blockchainKey, pairPublicKey, curve)
        val mutableTokens = tokens.toMutableSet()
        val wallet = Wallet(blockchain, addresses, publicKey, mutableTokens)

        val assembly = when (blockchain) {
            // region BTC-like blockchains

            Blockchain.Bitcoin,
            Blockchain.BitcoinTestnet,
            Blockchain.Dash -> {
                BitcoinWalletManagerAssembly
            }

            Blockchain.Dogecoin -> {
                DogecoinWalletManagerAssembly
            }

            Blockchain.Litecoin -> {
                LitecoinWalletManagerAssembly
            }

            Blockchain.BitcoinCash, Blockchain.BitcoinCashTestnet -> {
                BitcoinCashWalletManagerAssembly
            }

            Blockchain.Ravencoin, Blockchain.RavencoinTestnet -> {
                RavencoinWalletManagerAssembly
            }

            // endregion

            // region ETH-like blockchains

            Blockchain.Ethereum, Blockchain.EthereumClassic -> {
                EthereumWalletManagerAssembly
            }

            Blockchain.Arbitrum,
            Blockchain.ArbitrumTestnet,
            Blockchain.Avalanche,
            Blockchain.AvalancheTestnet,
            Blockchain.EthereumTestnet,
            Blockchain.EthereumClassicTestnet,
            Blockchain.Fantom,
            Blockchain.FantomTestnet,
            Blockchain.RSK,
            Blockchain.BSC,
            Blockchain.BSCTestnet,
            Blockchain.Polygon,
            Blockchain.PolygonTestnet,
            Blockchain.Gnosis,
            Blockchain.EthereumFair,
            Blockchain.EthereumPow,
            Blockchain.EthereumPowTestnet,
            Blockchain.Kava, Blockchain.KavaTestnet,
            Blockchain.Cronos -> {
                EthereumLikeWalletManagerAssembly
            }

            Blockchain.Optimism, Blockchain.OptimismTestnet -> {
                OptimismWalletManagerAssembly
            }

            // endregion

            Blockchain.Solana, Blockchain.SolanaTestnet -> {
                SolanaWalletManagerAssembly
            }

            Blockchain.Ducatus -> {
                DucatusWalletManagerAssembly
            }

            Blockchain.Polkadot, Blockchain.PolkadotTestnet, Blockchain.Kusama -> {
                PolkadotWalletManagerAssembly
            }

            Blockchain.Stellar, Blockchain.StellarTestnet -> {
                StellarWalletManagerAssembly
            }

            Blockchain.Cardano, Blockchain.CardanoShelley -> {
                CardanoWalletManagerAssembly
            }

            Blockchain.XRP -> {
                XRPWalletManagerAssembly
            }

            Blockchain.Binance, Blockchain.BinanceTestnet -> {
                BinanceWalletManagerAssembly
            }

            Blockchain.Tezos -> {
                TezosWalletManagerAssembly
            }

            Blockchain.Tron, Blockchain.TronTestnet -> {
                TronWalletManagerAssembly
            }

            Blockchain.Kaspa -> {
                KaspaWalletManagerAssembly
            }

            Blockchain.TON, Blockchain.TONTestnet -> {
                TonWalletManagerAssembly
            }

            Blockchain.Cosmos, Blockchain.CosmosTestnet -> {
                CosmosWalletManagerAssembly
            }

            Blockchain.TerraV1 -> {
                TerraV1WalletManagerAssembly
            }

            Blockchain.TerraV2 -> {
                TerraV2WalletManagerAssembly
            }

            Blockchain.Unknown -> throw Exception("unsupported blockchain")
        }

        return assembly.make(
            WalletManagerAssemblyInput(
            wallet = wallet,
            config = config,
            presetTokens = mutableTokens,
            curve = curve
        )
        )
    }

    private fun checkIfWrongKey(curve: EllipticCurve, publicKey: Wallet.PublicKey): Boolean {
        return when (curve) {
            EllipticCurve.Ed25519 -> publicKey.seedKey.size > 32 || publicKey.blockchainKey.size > 32
            else -> false
        }
    }


}
