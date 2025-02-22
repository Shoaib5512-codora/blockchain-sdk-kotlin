package com.tangem.blockchain.blockchains.cardano

import com.google.protobuf.ByteString
import com.tangem.blockchain.blockchains.cardano.network.common.models.CardanoUnspentOutput
import com.tangem.blockchain.blockchains.cardano.walletcore.CardanoTWTxBuilder
import com.tangem.blockchain.common.*
import com.tangem.blockchain.common.transaction.Fee
import com.tangem.blockchain.extensions.hexToBigDecimal
import com.tangem.blockchain.extensions.trustWalletCoinType
import com.tangem.common.extensions.toHexString
import wallet.core.java.AnySigner
import wallet.core.jni.CoinType
import wallet.core.jni.DataVector
import wallet.core.jni.TransactionCompiler
import wallet.core.jni.proto.Cardano
import wallet.core.jni.proto.Common
import wallet.core.jni.proto.TransactionCompiler.PreSigningOutput
import java.math.BigDecimal
import kotlin.properties.Delegates

// You can decode your CBOR transaction here: https://cbor.me
internal class CardanoTransactionBuilder(
    private val wallet: Wallet,
) : TransactionValidator {

    private val coinType: CoinType = wallet.blockchain.trustWalletCoinType
    private val decimals: Int = wallet.blockchain.decimals()

    private var twTxBuilder: CardanoTWTxBuilder by Delegates.notNull()

    fun update(outputs: List<CardanoUnspentOutput>) {
        twTxBuilder = CardanoTWTxBuilder(wallet, outputs)
    }

    override fun validate(transactionData: TransactionData): Result<Unit> {
        return runCatching {
            transactionData.requireUncompiled()

            val isCoinTransaction = transactionData.amount.type is AmountType.Coin
            val transactionValue = transactionData.amount.value ?: BigDecimal.ZERO

            throwIf(
                exception = BlockchainSdkError.Cardano.InsufficientSendingAdaAmount,
                condition = isCoinTransaction && transactionValue < BigDecimal.ONE,
            )

            val plan = AnySigner.plan(
                twTxBuilder.build(transactionData),
                coinType,
                Cardano.TransactionPlan.parser(),
            )

            throwIf(
                exception = BlockchainSdkError.Cardano.InsufficientMinAdaBalanceToSendToken,
                condition = !isCoinTransaction && plan.error == Common.SigningError.Error_low_balance,
            )

            throwIf(
                exception = BlockchainSdkError.Cardano.InsufficientRemainingBalanceToWithdrawTokens,
                condition = checkRequiredMinAdaValue(transactionData = transactionData, plan = plan),
            )

            checkRemainingAdaBalance(transactionData = transactionData, plan = plan)
        }
    }

    fun estimateFee(transactionData: TransactionData): Fee {
        transactionData.requireUncompiled()

        // Create input with zero fee amount
        val input = twTxBuilder.build(transactionData)
        val plan = AnySigner.plan(input, coinType, Cardano.TransactionPlan.parser())

        val feeAmount = Amount(
            value = BigDecimal(plan.fee).movePointLeft(decimals),
            blockchain = wallet.blockchain,
        )

        return when (val type = transactionData.amount.type) {
            AmountType.Coin -> Fee.Common(amount = feeAmount)
            is AmountType.Token -> {
                val tokenFee = Fee.CardanoToken(
                    amount = feeAmount,
                    // plan amount was calculated with zero fee amount
                    minAdaValue = BigDecimal(plan.amount).movePointLeft(decimals),
                )

                tokenFee.copy(
                    minAdaValue = estimateMinAdaValue(transactionData.copy(fee = tokenFee)),
                )
            }
            else -> throw BlockchainSdkError.CustomError("AmountType $type is not supported")
        }
    }

    /**
     * Estimate min-ada-value for sending a token taking into account the already calculated fee.
     * It's necessary to be sure that the remaining balance is correct.
     *
     * @param transactionData transaction with non zero fee amount
     *
     * @see CardanoTWTxBuilder.setTokenAmount
     */
    private fun estimateMinAdaValue(transactionData: TransactionData): BigDecimal {
        val input = twTxBuilder.build(transactionData)
        val plan = AnySigner.plan(input, coinType, Cardano.TransactionPlan.parser())

        return BigDecimal(plan.amount).movePointLeft(decimals)
    }

    fun buildForSign(transactionData: TransactionData): ByteArray {
        val input = twTxBuilder.build(transactionData)
        val txInputData = input.toByteArray()

        val preImageHashes = TransactionCompiler.preImageHashes(coinType, txInputData)
        val preSigningOutput = PreSigningOutput.parseFrom(preImageHashes)

        if (preSigningOutput.error != Common.SigningError.OK) {
            throw BlockchainSdkError.FailedToBuildTx
        }

        return preSigningOutput.dataHash.toByteArray()
    }

    fun buildForSend(transactionData: TransactionData, signatureInfo: SignatureInfo): ByteArray {
        val input = twTxBuilder.build(transactionData)
        val txInputData = input.toByteArray()

        val signatures = DataVector()
        signatures.add(signatureInfo.signature)

        val publicKeys = DataVector()

        // WalletCore used here `.ed25519Cardano` curve with 128 bytes publicKey.
        // Calculated as: chainCode + secondPubKey + chainCode
        // The number of bytes in a Cardano public key (two ed25519 public key + chain code).
        // We should add dummy chain code in publicKey if we use old 32 byte key to get 128 bytes in total
        val publicKey = if (CardanoUtils.isExtendedPublicKey(signatureInfo.publicKey)) {
            signatureInfo.publicKey
        } else {
            signatureInfo.publicKey + ByteArray(MISSING_LENGTH_TO_EXTENDED_KEY)
        }

        publicKeys.add(publicKey)

        val compileWithSignatures = TransactionCompiler.compileWithSignatures(
            coinType,
            txInputData,
            signatures,
            publicKeys,
        )

        val output = Cardano.SigningOutput.parseFrom(compileWithSignatures)

        if (output.error != Common.SigningError.OK || output.encoded.isEmpty) {
            throw BlockchainSdkError.FailedToBuildTx
        }

        return output.encoded.toByteArray()
    }

    /**
     * Require to check that the min-ada-value from Wallet-Core [Cardano.TransactionPlan] is equals real min-ada-value.
     * Because Wallet-Core can hold a fee value from min-ada-value.
     *
     * @param transactionData transaction
     * @param plan        wallet-core transaction input
     */
    private fun checkRequiredMinAdaValue(transactionData: TransactionData, plan: Cardano.TransactionPlan): Boolean {
        transactionData.requireUncompiled()

        return when (val type = transactionData.amount.type) {
            is AmountType.Token -> {
                val minAdaValue = twTxBuilder.calculateMinAdaValueToWithdrawToken(
                    contractAddress = type.token.contractAddress,
                    amount = transactionData.amount.longValueOrZero,
                )

                plan.amount < minAdaValue
            }
            else -> false // another types don't use min-ada-value
        }
    }

    private fun checkRemainingAdaBalance(transactionData: TransactionData, plan: Cardano.TransactionPlan) {
        val remainingTokens = getRemainingTokens(transactionData, plan)

        if (remainingTokens.isEmpty()) {
            val minChange = BigDecimal.ONE.movePointRight(decimals).toLong()

            throwIf(
                exception = BlockchainSdkError.Cardano.InsufficientRemainingBalance,
                condition = plan.change in 1 until minChange,
            )
        } else {
            val minChange = twTxBuilder.calculateMinAdaValueToWithdrawAllTokens(remainingTokens)

            throwIf(
                exception = BlockchainSdkError.Cardano.InsufficientRemainingBalanceToWithdrawTokens,
                condition = plan.change == 0L || plan.change in 1 until minChange,
            )
        }
    }

    private fun getRemainingTokens(
        transactionData: TransactionData,
        plan: Cardano.TransactionPlan,
    ): Map<Cardano.TokenAmount, Long> {
        transactionData.requireUncompiled()

        return plan.availableTokensList
            .associateWith { tokenAmount ->
                val amount = tokenAmount.amount.toLong()
                val remainingAmount = if (transactionData.contractAddress?.startsWith(tokenAmount.policyId) == true) {
                    amount - transactionData.amount.longValueOrZero
                } else {
                    amount
                }

                remainingAmount
            }
            .filter { it.value > 0 }
    }

    private fun ByteString.toLong(): Long {
        return toByteArray().toHexString().hexToBigDecimal().toLong()
    }

    private fun throwIf(exception: BlockchainSdkError.Cardano, condition: Boolean) {
        if (condition) throw exception
    }

    private companion object {
        const val MISSING_LENGTH_TO_EXTENDED_KEY = 32 * 3
    }
}
