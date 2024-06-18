package com.tangem.blockchain.common.address

import com.google.common.truth.Truth
import com.tangem.blockchain.common.Blockchain
import org.junit.Test

class EstimationFeeAddressFactoryTest {

    private val factory = EstimationFeeAddressFactory()
    private val hardcodedAddressesForBlockchain = mapOf(
        Blockchain.Cardano to "addr1q95pg4z9tf26r5dwf72vmh62u3pr9sewq2waahyhpjzm3enz43pvhh0us3z0z5xen2skq200e67eu89s5v2s0sdh3fnsm9lknu",
        Blockchain.Chia to "",
        Blockchain.ChiaTestnet to "",
        Blockchain.XRP to "",
        Blockchain.Stellar to "",
        Blockchain.StellarTestnet to "",
        Blockchain.Binance to "",
        Blockchain.BinanceTestnet to "",
        Blockchain.SolanaTestnet to "",
        Blockchain.Hedera to "",
        Blockchain.HederaTestnet to "",
        Blockchain.Tezos to "",
        Blockchain.Kaspa to "",
        Blockchain.Ducatus to "",
        Blockchain.Bitcoin to "bc1qkrc5kmpq546wr2xk0errg58yw9jjq7thvhdk5k",
        Blockchain.BitcoinTestnet to "bc1qkrc5kmpq546wr2xk0errg58yw9jjq7thvhdk5k",
        Blockchain.Litecoin to "ltc1qelzg874tr0zap72ckcc9exa3lgyyt6rvfhhekc",
        Blockchain.BitcoinCash to "bitcoincash:qrn96yyxa93t6sqmehvls6746qafkcsuku6zmd9460",
        Blockchain.BitcoinCashTestnet to "bitcoincash:qrn96yyxa93t6sqmehvls6746qafkcsuku6zmd9460",
        Blockchain.Dogecoin to "DRVD4B4YD9CBSjqaa3UfF42vSN6k2tJwhz",
        Blockchain.Dash to "Xqfekbgca2HDaXhrNYP2HTnuQ5go2E8dDE",
        Blockchain.Ravencoin to "RT5qKgXdmh9pqtz71cgfL834VfeXFVH1sG",
        Blockchain.RavencoinTestnet to "RT5qKgXdmh9pqtz71cgfL834VfeXFVH1sG",
        Blockchain.Solana to "9wuDg6Y4H4j86Kg5aUGrUeaBa3sAUzjMs37KbeGFnRuM",
        Blockchain.EthereumClassic to "0xc49722a6f4Fe5A1347710dEAAa1fafF4c275689b",
        Blockchain.EthereumClassicTestnet to "0xc49722a6f4Fe5A1347710dEAAa1fafF4c275689b",
        Blockchain.Decimal to "d0122a5qy59f7qge7d6hkz4u389qmd0dsrh6a7qnx",
        Blockchain.DecimalTestnet to "d0122a5qy59f7qge7d6hkz4u389qmd0dsrh6a7qnx",
        Blockchain.Ethereum to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.EthereumPow to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Dischain to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.RSK to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.BSC to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Polygon to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Avalanche to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Fantom to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Arbitrum to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Gnosis to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Optimism to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Kava to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Cronos to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Telos to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Shibarium to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.OctaSpace to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Playa3ull to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Aurora to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Areon to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.PulseChain to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.ZkSyncEra to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Base to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Moonbeam to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Manta to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.PolygonZkEVM to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Moonriver to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Mantle to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Flare to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Taraxa to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.EthereumTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.EthereumPowTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.BSCTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.PolygonTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.AvalancheTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.FantomTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.ArbitrumTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.OptimismTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.KavaTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.TelosTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.ShibariumTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.OctaSpaceTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.AuroraTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.AreonTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.PulseChainTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.ZkSyncEraTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.BaseTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.MoonbeamTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.MantaTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.PolygonZkEVMTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.MoonriverTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.MantleTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.FlareTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.TaraxaTestnet to "0x52bb4012854f808CF9BAbd855e44E506dAf6C077",
        Blockchain.Polkadot to "15RRtiC2akPUE9FGqqa66awoAFz6XCnZiFUf34k2CHbLWNfC",
        Blockchain.PolkadotTestnet to "15RRtiC2akPUE9FGqqa66awoAFz6XCnZiFUf34k2CHbLWNfC",
        Blockchain.Kusama to "CsNtwDXUzMR4ZKBQrXCfA6bBXQBFU1DDbtSwLAsaVr13sGs",
        Blockchain.AlephZero to "5DaWppqEJPc6BhFKD2NBC1ACXPDMPYfv2AQDB5uH5KT4mpef",
        Blockchain.AlephZeroTestnet to "5DaWppqEJPc6BhFKD2NBC1ACXPDMPYfv2AQDB5uH5KT4mpef",
        Blockchain.Cosmos to "cosmos1lhjvds604fvac32j4eygpr820lyc82dlyq70m5",
        Blockchain.CosmosTestnet to "cosmos1lhjvds604fvac32j4eygpr820lyc82dlyq70m5",
        Blockchain.TerraV1 to "terra1pfamr0t2daet92grdvxqex235q58qrx6xclldg",
        Blockchain.TerraV2 to "terra1pfamr0t2daet92grdvxqex235q58qrx6xclldg",
        Blockchain.Tron to "TA4Tkaj2nAJjkVbDHdUQDxYCbLfsZzS8pA",
        Blockchain.TON to "EQAY92urFDKejoDRdi_EfRKLGB1JkGjD8z1inj_DhgBaD0Xo",
        Blockchain.Near to "4a9fb267a005b7e923233b59aff1b73e577347a1ab36aa231a1880a91776c416",
        Blockchain.XDC to "xdc9606Af4939f6F9fb9731A39a32B00aD966348ED6",
        Blockchain.VeChain to "0x1C5B4935709583758BE5b9ECeeBaf5cD6AFecF41",
        Blockchain.Aptos to "0x4626b7ef23fb2800a0e224e8249f47e0db3579070262da2a7efb0bc52c882867",
        Blockchain.Algorand to "CW6XDCKQAZUGAIOTGE2NEPYFFVW6H6IKFOTOF3W5WDUVHH4ZIDCIKYDPXY",
        Blockchain.TronTestnet to "TA4Tkaj2nAJjkVbDHdUQDxYCbLfsZzS8pA",
        Blockchain.TONTestnet to "EQAY92urFDKejoDRdi_EfRKLGB1JkGjD8z1inj_DhgBaD0Xo",
        Blockchain.NearTestnet to "4a9fb267a005b7e923233b59aff1b73e577347a1ab36aa231a1880a91776c416",
        Blockchain.XDCTestnet to "xdc9606Af4939f6F9fb9731A39a32B00aD966348ED6",
        Blockchain.VeChainTestnet to "0x1C5B4935709583758BE5b9ECeeBaf5cD6AFecF41",
        Blockchain.AptosTestnet to "0x4626b7ef23fb2800a0e224e8249f47e0db3579070262da2a7efb0bc52c882867",
        Blockchain.AlgorandTestnet to "CW6XDCKQAZUGAIOTGE2NEPYFFVW6H6IKFOTOF3W5WDUVHH4ZIDCIKYDPXY",
        Blockchain.Koinos to "1C423Vbd44zjghhJR5fKJdLFS3rgVFUc9A",
        Blockchain.Radiant to "1K8jBuCKzuwvFCjL7Qpqq69k1hnVXJ31Nc",
        Blockchain.Joystream to "j4SXkX46sABwjxeuzicd2e5m8gDu4ieoWHW3aggbBKkh4WvtF",
        Blockchain.Bittensor to "5HLcF8UkyCTK5oszoTxx8LKxEzmtEEfPWeAxCz5NiDjqWH9y",
    )

    @Test
    fun makeAddressTest() {
        // tests that hardcoded addresses is not changed
        hardcodedAddressesForBlockchain.forEach {
            Truth.assertThat(factory.makeAddress(blockchain = it.key)).isEqualTo(it.value)
        }
    }
}
