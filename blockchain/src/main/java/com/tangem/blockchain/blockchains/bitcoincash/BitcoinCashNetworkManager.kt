package com.tangem.blockchain.blockchains.bitcoincash

import com.tangem.blockchain.blockchains.bitcoin.network.BitcoinAddressInfo
import com.tangem.blockchain.blockchains.bitcoin.network.BitcoinFee
import com.tangem.blockchain.blockchains.bitcoin.network.BitcoinNetworkService
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.SimpleResult
import com.tangem.blockchain.network.API_BLOCKCHAIR
import com.tangem.blockchain.network.blockchair.BlockchairApi
import com.tangem.blockchain.network.blockchair.BlockchairProvider
import com.tangem.blockchain.network.createRetrofitInstance

class BitcoinCashNetworkManager : BitcoinNetworkService {
    private val blockchain = Blockchain.BitcoinCash

    private val blockchairProvider by lazy {
        val api = createRetrofitInstance(API_BLOCKCHAIR).create(BlockchairApi::class.java)
        BlockchairProvider(api, blockchain)
    }

    override suspend fun getInfo(address: String): Result<BitcoinAddressInfo> {
        return blockchairProvider.getInfo(address)
    }

    override suspend fun getFee(): Result<BitcoinFee> {
        return blockchairProvider.getFee()
    }

    override suspend fun sendTransaction(transaction: String): SimpleResult {
        return blockchairProvider.sendTransaction(transaction)
    }

    override suspend fun getSignatureCount(address: String): Result<Int> {
        return blockchairProvider.getSignatureCount(address)
    }
}