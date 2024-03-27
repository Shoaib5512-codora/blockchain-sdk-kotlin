package com.tangem.blockchain.blockchains.cardano.network.adalite

import com.tangem.blockchain.blockchains.cardano.network.CardanoAddressResponse
import com.tangem.blockchain.blockchains.cardano.network.CardanoNetworkProvider
import com.tangem.blockchain.blockchains.cardano.network.InfoInput
import com.tangem.blockchain.blockchains.cardano.network.adalite.converters.AdaliteAddressResponseConverter
import com.tangem.blockchain.blockchains.cardano.network.adalite.request.AdaliteSendBody
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.toBlockchainSdkError
import com.tangem.blockchain.extensions.Result
import com.tangem.blockchain.extensions.SimpleResult
import com.tangem.blockchain.extensions.encodeBase64NoWrap
import com.tangem.blockchain.network.createRetrofitInstance
import com.tangem.common.extensions.toHexString
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import java.io.IOException

internal class AdaliteNetworkProvider(override val baseUrl: String) : CardanoNetworkProvider {

    private val api: AdaliteApi by lazy {
        createRetrofitInstance(baseUrl).create(AdaliteApi::class.java)
    }

    override suspend fun getInfo(input: InfoInput): Result<CardanoAddressResponse> {
        return try {
            coroutineScope {
                val addressDataDeferred = input.addresses.map { async { api.getAddressData(it) } }
                val unspentOutputsDeferred = async { api.getUnspentOutputs(input.addresses.toList()) }

                val addressDataResponse = addressDataDeferred.map { it.await() }
                val unspentOutputsResponse = unspentOutputsDeferred.await()

                Result.Success(
                    data = AdaliteAddressResponseConverter.convert(
                        addressDataResponse = addressDataResponse,
                        unspentOutputsResponse = unspentOutputsResponse,
                        tokens = input.tokens,
                    ),
                )
            }
        } catch (exception: Exception) {
            Result.Failure(exception.toBlockchainSdkError())
        }
    }

    override suspend fun sendTransaction(transaction: ByteArray): SimpleResult {
        return try {
            api.sendTransaction(body = AdaliteSendBody(signedTransaction = transaction.encodeBase64NoWrap()))
            SimpleResult.Success
        } catch (exception: Exception) {
            val result = if (exception is HttpException && exception.code() == HTTP_BAD_REQUEST_CODE) {
                createBadRequestException(transaction, exception)
            } else {
                exception
            }

            SimpleResult.Failure(result.toBlockchainSdkError())
        }
    }

    private fun createBadRequestException(transaction: ByteArray, cause: HttpException): Exception {
        return IOException(
            """
                ${Blockchain.Cardano}. Failed to send transaction ${transaction.toHexString()}
                with an error:
                ${cause.response()?.errorBody()?.string()}
            """.trimIndent(),
        )
    }

    private companion object {
        const val HTTP_BAD_REQUEST_CODE = 400
    }
}
