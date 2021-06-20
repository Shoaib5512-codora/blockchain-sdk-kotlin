package com.tangem.blockchain.blockchains.dogecoin

import com.google.common.truth.Truth
import com.tangem.blockchain.blockchains.bitcoin.BitcoinAddressService
import com.tangem.blockchain.blockchains.bitcoin.BitcoinTransactionBuilder
import com.tangem.blockchain.blockchains.bitcoin.BitcoinTransactionTest
import com.tangem.blockchain.common.Amount
import com.tangem.blockchain.common.AmountType
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.TransactionData
import com.tangem.blockchain.common.address.DefaultAddressType
import com.tangem.blockchain.extensions.Result
import com.tangem.common.extensions.hexToBytes
import org.junit.Test
import org.libdohj.params.DogecoinMainNetParams

class DogecoinTransactionTest {

    private val blockchain = Blockchain.Dogecoin
    private val networkParameters = DogecoinMainNetParams()

    @Test
    fun buildCorrectTransaction() {
        // arrange
        val walletPublicKey =
            "04E3F3BE3CE3D8284DB3BA073AD0291040093D83C11A277B905D5555C9EC41073E103F4D9D299EDEA8285C51C3356A8681A545618C174251B984DF841F49D2376F"
                .hexToBytes()
        val signature =
            "88E322D377878E83F25FD2E258344F0A7CC401654BF71C43DF96FC6B46766CAE30E97BD9018E9B2E918EF79E15E2747D4E00C55D69FA0B8ADFAFD07F41144F81337D7F3BD0798D66FDCE04B07C30984424B13B98BB2C3645744A696AD26ECC780157EA9D44DC41D0BCB420175A5D3F543079F4263AA2DBDE0EE2D33A877FC583"
                .hexToBytes()
        val sendValue = "0.1".toBigDecimal()
        val feeValue = "0.01".toBigDecimal()
        val destinationAddress = "DRgF4iLXRhnYeQEV9kHmkvvnz128uCFZXL"

        val addresses = BitcoinAddressService(blockchain).makeAddresses(walletPublicKey)
        val address = addresses.find { it.type == DefaultAddressType }!!.value
        val transactionBuilder = BitcoinTransactionBuilder(walletPublicKey, blockchain, addresses)
        transactionBuilder.unspentOutputs =
            BitcoinTransactionTest.prepareTwoUnspentOutputs(listOf(address), networkParameters)

        val amountToSend = Amount(sendValue, blockchain, AmountType.Coin)
        val fee = Amount(amountToSend, feeValue)
        val transactionData = TransactionData(
            sourceAddress = address,
            destinationAddress = destinationAddress,
            amount = amountToSend,
            fee = fee
        )

        val expectedHashToSign1 = "821AB220C94E463A312C5D1AA8F30D01EA20FAD896C077D0E539D7F21FD0AC77"
            .hexToBytes().toList()
        val expectedHashToSign2 = "3613B882F3A09047E8BF7D37FF0E09A255B0D4293C2CE894D6EE6B4C8C487AD4"
            .hexToBytes().toList()
        val expectedSignedTransaction =
            "0100000002B6A2673BDD04D57B5560F4E46CAC3C1F974E41463568F2A11E7D3175521D9C6D000000008B48304502210088E322D377878E83F25FD2E258344F0A7CC401654BF71C43DF96FC6B46766CAE022030E97BD9018E9B2E918EF79E15E2747D4E00C55D69FA0B8ADFAFD07F41144F81014104E3F3BE3CE3D8284DB3BA073AD0291040093D83C11A277B905D5555C9EC41073E103F4D9D299EDEA8285C51C3356A8681A545618C174251B984DF841F49D2376FFFFFFFFF3F86D67DC12F3E3E7EE47E3B02D30D476823B594CBCABF1123A8C272CC91F2AE490000008A4730440220337D7F3BD0798D66FDCE04B07C30984424B13B98BB2C3645744A696AD26ECC7802200157EA9D44DC41D0BCB420175A5D3F543079F4263AA2DBDE0EE2D33A877FC583014104E3F3BE3CE3D8284DB3BA073AD0291040093D83C11A277B905D5555C9EC41073E103F4D9D299EDEA8285C51C3356A8681A545618C174251B984DF841F49D2376FFFFFFFFF0280969800000000001976A914E14686E153D98A799BDD1DC973D949AF5541B74188AC80790CD4E80000001976A914C5C53741303B67E7FE2EA62CB5730B3DD32D75FF88AC00000000"
                .hexToBytes()

        // act
        val buildToSignResult = transactionBuilder.buildToSign(transactionData) as Result.Success
        val signedTransaction = transactionBuilder.buildToSend(signature)

        // assert
        Truth.assertThat(buildToSignResult.data.map { it.toList() })
            .containsExactly(expectedHashToSign1, expectedHashToSign2)
        Truth.assertThat(signedTransaction).isEqualTo(expectedSignedTransaction)
    }
}