package com.tangem.blockchain.common

import java.math.BigDecimal
import java.util.*

class Wallet(
        val blockchain: Blockchain,
        val address: String,
        val token: Token? = null
) {
    val exploreUrl: String
    val shareUrl: String
    val recentTransactions: MutableList<TransactionData> = mutableListOf() //we put only unconfirmed transactions here, but never delete them, change status to confirmed instead
    val amounts: MutableMap<AmountType, Amount> = mutableMapOf()

    init {
        setAmount(Amount(null, blockchain, address))
        if (token != null) setAmount(Amount(token))

        exploreUrl = blockchain.getExploreUrl(address, token)
        shareUrl = blockchain.getShareUri(address)
    }

    fun setAmount(amount: Amount) {
        amounts[amount.type] = amount
    }

    fun setCoinValue(value: BigDecimal) {
        val amount = Amount(value, blockchain, address)
        setAmount(amount)
    }

    fun setTokenValue(value: BigDecimal) {
        if (token != null) {
            val amount = Amount(token, value)
            setAmount(amount)
        }
    }

    fun setReserveValue(value: BigDecimal) {
        val amount = Amount(value, blockchain, address, AmountType.Reserve)
        setAmount(amount)
    }

    fun addTransactionDummy(direction: TransactionDirection? = null) {
        val transaction = TransactionData(
                amount = Amount(null, blockchain),
                fee = null,
                sourceAddress = if (direction == TransactionDirection.Outgoing) address else "unknown",
                destinationAddress = if (direction == TransactionDirection.Incoming) address else "unknown",
                date = Calendar.getInstance()
        )
        recentTransactions.add(transaction)
    }

    fun addOutgoingTransaction(transactionData: TransactionData) {
        transactionData.apply {
            date = Calendar.getInstance()
            hash = hash?.toLowerCase()
        }
        recentTransactions.add(transactionData)
    }

    fun fundsAvailable(amountType: AmountType): BigDecimal {
        return amounts[amountType]?.value ?: BigDecimal.ZERO
    }

}