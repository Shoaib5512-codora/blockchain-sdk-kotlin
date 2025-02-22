package com.tangem.blockchain.blockchains.bitcoin

import com.tangem.blockchain.blockchains.dash.DashMainNetParams
import com.tangem.blockchain.blockchains.ducatus.DucatusMainNetParams
import com.tangem.blockchain.blockchains.ravencoin.RavencoinMainNetParams
import com.tangem.blockchain.blockchains.ravencoin.RavencoinTestNetParams
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.BlockchainSdkError
import com.tangem.blockchain.common.TransactionData
import com.tangem.blockchain.common.transaction.getMinimumRequiredUTXOsToSend
import com.tangem.blockchain.extensions.Result
import com.tangem.common.extensions.calculateRipemd160
import com.tangem.common.extensions.calculateSha256
import com.tangem.common.extensions.isZero
import com.tangem.common.extensions.toCompressedPublicKey
import org.bitcoinj.core.*
import org.bitcoinj.crypto.TransactionSignature
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptPattern
import org.libdohj.params.DogecoinMainNetParams
import org.libdohj.params.LitecoinMainNetParams
import java.math.BigDecimal
import java.math.BigInteger

open class BitcoinTransactionBuilder(
    private val walletPublicKey: ByteArray,
    blockchain: Blockchain,
    walletAddresses: Set<com.tangem.blockchain.common.address.Address> = emptySet(),
) {
    private val walletScripts =
        walletAddresses.filterIsInstance<BitcoinScriptAddress>().map { it.script }
    protected lateinit var transaction: Transaction
    private var transactionSizeWithoutWitness = 0

    protected var networkParameters = when (blockchain) {
        Blockchain.Bitcoin, Blockchain.BitcoinCash -> MainNetParams()
        Blockchain.BitcoinTestnet, Blockchain.BitcoinCashTestnet -> TestNet3Params()
        Blockchain.Litecoin -> LitecoinMainNetParams()
        Blockchain.Dogecoin -> DogecoinMainNetParams()
        Blockchain.Ducatus -> DucatusMainNetParams()
        Blockchain.Dash -> DashMainNetParams()
        Blockchain.Ravencoin -> RavencoinMainNetParams()
        Blockchain.RavencoinTestnet -> RavencoinTestNetParams()
        else -> error("${blockchain.fullName} blockchain is not supported by ${this::class.simpleName}")
    }
    var unspentOutputs: List<BitcoinUnspentOutput>? = null

    open fun buildToSign(transactionData: TransactionData): Result<List<ByteArray>> {
        transactionData.requireUncompiled()

        if (unspentOutputs.isNullOrEmpty()) {
            return Result.Failure(BlockchainSdkError.CustomError("Unspent outputs are missing"))
        }

        val outputsToSend = getMinimumRequiredUTXOsToSend(
            unspentOutputs = unspentOutputs!!,
            transactionAmount = transactionData.amount.value!!,
            transactionFeeAmount = transactionData.fee?.amount?.value!!,
            unspentToAmount = { it.amount },
        )

        val change: BigDecimal = calculateChange(transactionData, outputsToSend)
        transaction =
            transactionData.toBitcoinJTransaction(networkParameters, outputsToSend, change)

        val hashesToSign = MutableList(transaction.inputs.size) { byteArrayOf() }
        for (input in transaction.inputs) {
            val index = input.index
            val scriptPubKey = Script(transaction.inputs[index].scriptBytes)

            val scriptToSign = when (scriptPubKey.scriptType) {
                Script.ScriptType.P2PKH -> scriptPubKey
                Script.ScriptType.P2SH, Script.ScriptType.P2WSH -> findSpendingScript(scriptPubKey)
                Script.ScriptType.P2WPKH -> ScriptBuilder.createP2PKHOutputScript(
                    ECKey.fromPublicOnly(walletPublicKey.toCompressedPublicKey()),
                )
                else -> error("Unsupported output script")
            }
            hashesToSign[index] = when (scriptPubKey.scriptType) {
                Script.ScriptType.P2PKH, Script.ScriptType.P2SH -> {
                    transaction.hashForSignature(
                        index,
                        scriptToSign,
                        Transaction.SigHash.ALL,
                        false,
                    ).bytes
                }
                Script.ScriptType.P2WPKH, Script.ScriptType.P2WSH -> {
                    transaction.hashForWitnessSignature(
                        index,
                        scriptToSign,
                        Coin.parseCoin(outputsToSend[index].amount.toPlainString()),
                        Transaction.SigHash.ALL,
                        false,
                    ).bytes
                }
                else -> error("Unsupported output script")
            }
        }
        return Result.Success(hashesToSign)
    }

    @Suppress("MagicNumber")
    open fun buildToSend(signatures: ByteArray): ByteArray {
//        witnessSize = 0

        for (index in transaction.inputs.indices) {
            val scriptPubKey = Script(transaction.inputs[index].scriptBytes) // output script
            val signature = extractSignature(index, signatures)

            transaction.inputs[index].scriptSig = when (scriptPubKey.scriptType) {
                Script.ScriptType.P2PKH -> ScriptBuilder.createInputScript(
                    signature,
                    ECKey.fromPublicOnly(walletPublicKey),
                )
                Script.ScriptType.P2SH -> { // only 1 of 2 multisig script for now
                    val script = findSpendingScript(scriptPubKey)
                    if (!ScriptPattern.isSentToMultisig(script)) {
                        error("Unsupported wallet script")
                    }
                    ScriptBuilder.createP2SHMultiSigInputScript(mutableListOf(signature), script)
                }
                Script.ScriptType.P2WPKH, Script.ScriptType.P2WSH -> ScriptBuilder.createEmpty()
                else -> error("Unsupported output script")
            }
            transactionSizeWithoutWitness = transaction.messageSize

            transaction.inputs[index].witness = when (scriptPubKey.scriptType) {
                Script.ScriptType.P2WPKH -> TransactionWitness.redeemP2WPKH(
                    signature,
                    ECKey.fromPublicOnly(walletPublicKey.toCompressedPublicKey()),
                )
                Script.ScriptType.P2WSH -> { // only 1 of 2 multisig script for now
                    val witness = TransactionWitness(3)
                    witness.setPush(0, byteArrayOf())
                    witness.setPush(1, signature.encodeToBitcoin())
                    witness.setPush(2, findSpendingScript(scriptPubKey).program)
                    witness
                }
                else -> null
            }
        }
        return transaction.bitcoinSerialize()
    }

    fun getTransactionHash() = transaction.txId.bytes

    @Suppress("MagicNumber")
    fun getEstimateSize(transactionData: TransactionData): Result<Int> {
        return when (val buildTransactionResult = buildToSign(transactionData)) {
            is Result.Failure -> buildTransactionResult
            is Result.Success -> {
                val hashes = buildTransactionResult.data
                val finalTransactionSize =
                    buildToSend(ByteArray(64 * hashes.size) { -128 }).size // needed for longer signature
                val vsize = if (transaction.hasWitnesses()) {
                    val weight = transactionSizeWithoutWitness * 3 + finalTransactionSize
                    (weight + 3) / 4 // round up
                } else {
                    finalTransactionSize
                }
                Result.Success(vsize)
            }
        }
    }

    fun calculateChange(transactionData: TransactionData, unspentOutputs: List<BitcoinUnspentOutput>): BigDecimal {
        transactionData.requireUncompiled()

        val fullAmount = unspentOutputs.map { it.amount }.reduce { acc, number -> acc + number }
        return fullAmount - (
            transactionData.amount.value!! + (
                transactionData.fee?.amount?.value
                    ?: 0.toBigDecimal()
                )
            )
    }

    @Suppress("MagicNumber")
    open fun extractSignature(index: Int, signatures: ByteArray): TransactionSignature {
        val r = BigInteger(1, signatures.copyOfRange(index * 64, 32 + index * 64))
        val s = BigInteger(1, signatures.copyOfRange(32 + index * 64, 64 + index * 64))
        val canonicalS = ECKey.ECDSASignature(r, s).toCanonicalised().s
        return TransactionSignature(r, canonicalS)
    }

    @Suppress("MagicNumber")
    private fun findSpendingScript(scriptPubKey: Script): Script {
        val scriptHash = ScriptPattern.extractHashFromP2SH(scriptPubKey)
        return when (scriptHash.size) {
            20 -> walletScripts.find {
                it.program.calculateSha256().calculateRipemd160().contentEquals(scriptHash)
            }
            32 -> walletScripts.find {
                it.program.calculateSha256().contentEquals(scriptHash)
            }
            else -> null
        } ?: error("No script for P2SH output found")
    }
}

internal fun TransactionData.toBitcoinJTransaction(
    networkParameters: NetworkParameters?,
    unspentOutputs: List<BitcoinUnspentOutput>,
    change: BigDecimal,
): Transaction {
    requireUncompiled()

    val transaction = Transaction(networkParameters)
    for (utxo in unspentOutputs) {
        transaction.addInput(
            Sha256Hash.wrap(utxo.transactionHash),
            utxo.outputIndex,
            Script(utxo.outputScript),
        )
    }
    transaction.addOutput(
        Coin.parseCoin(this.amount.value!!.toPlainString()),
        Address.fromString(networkParameters, this.destinationAddress),
    )
    if (!change.isZero()) {
        transaction.addOutput(
            Coin.parseCoin(change.toPlainString()),
            Address.fromString(
                networkParameters,
                this.sourceAddress,
            ),
        )
    }
    return transaction
}
