package com.tangem.blockchain.blockchains.bitcoin.network

import com.tangem.blockchain.blockchains.bitcoin.BitcoinUnspentOutput
import com.tangem.blockchain.common.BasicTransactionData
import com.tangem.blockchain.network.blockcypher.BlockcypherApi
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.SimpleResult
import com.tangem.blockchain.network.*
import com.tangem.blockchain.network.blockchair.BlockchairApi
import com.tangem.blockchain.network.blockchair.BlockchairProvider
import com.tangem.blockchain.network.blockcypher.BlockcypherProvider
import retrofit2.HttpException
import java.io.IOException
import java.math.BigDecimal


class BitcoinNetworkManager(blockchain: Blockchain) : BitcoinNetworkService {

//    private val blockchainInfoProvider by lazy {
//        val api = createRetrofitInstance(API_BLOCKCHAIN_INFO)
//                .create(BlockchainInfoApi::class.java)
////        val bitcoinfeesEarnApi = createRetrofitInstance(API_BITCOINFEES_EARN)
////                .create(BitcoinfeesEarnApi::class.java)
//        BlockchainInfoProvider(api)
//    }

    private val blockchairProvider by lazy {
        val api = createRetrofitInstance(API_BLOCKCHAIR)
                .create(BlockchairApi::class.java)
        BlockchairProvider(api, blockchain)
    }

    private val blockcypherProvider by lazy {
        val api = createRetrofitInstance(API_BLOCKCYPHER)
                .create(BlockcypherApi::class.java)
        BlockcypherProvider(api, blockchain)
    }

    private var provider: BitcoinNetworkService = blockchairProvider

    private fun changeProvider() {
        provider = when (provider) {
            blockchairProvider -> blockcypherProvider
            else -> blockchairProvider
        }
    }

    override suspend fun getInfo(address: String): Result<BitcoinAddressInfo> {
        val result = provider.getInfo(address)
        when (result) {
            is Result.Success -> return result
            is Result.Failure -> {
                if (result.error is IOException || result.error is HttpException) {
                    changeProvider()
                    return provider.getInfo(address)
                } else {
                    return result
                }
            }
        }
    }

    override suspend fun getFee(): Result<BitcoinFee> {
        val result = provider.getFee()
        when (result) {
            is Result.Success -> return result
            is Result.Failure -> {
                if (result.error is IOException || result.error is HttpException) {
                    changeProvider()
                    return provider.getFee()
                } else {
                    return result
                }
            }
        }
    }

    override suspend fun sendTransaction(transaction: String): SimpleResult {
        val result = provider.sendTransaction(transaction)
        when (result) {
            is SimpleResult.Success -> return result
            is SimpleResult.Failure -> {
                if (result.error is IOException || result.error is HttpException) {
                    changeProvider()
                    return provider.sendTransaction(transaction)
                } else {
                    return result
                }
            }
        }
    }

    override suspend fun getSignatureCount(address: String): Result<Int> {
        val result = provider.getSignatureCount(address)
        when (result) {
            is Result.Success -> return result
            is Result.Failure -> {
                if (result.error is IOException || result.error is HttpException) {
                    changeProvider()
                    return provider.getSignatureCount(address)
                } else {
                    return result
                }
            }
        }
    }
}

data class BitcoinAddressInfo(
        val balance: BigDecimal,
        val unspentOutputs: List<BitcoinUnspentOutput>,
        val recentTransactions: List<BasicTransactionData>,
        val hasUnconfirmed: Boolean? = null //additional logic for when recent transactions are absent or not reliable
)

data class BitcoinFee(
        val minimalPerKb: BigDecimal,
        val normalPerKb: BigDecimal,
        val priorityPerKb: BigDecimal
)