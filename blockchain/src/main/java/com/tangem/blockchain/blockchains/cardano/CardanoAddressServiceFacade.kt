package com.tangem.blockchain.blockchains.cardano

import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.address.Address
import com.tangem.blockchain.common.address.AddressService
import com.tangem.blockchain.common.address.TrustWalletAddressService
import com.tangem.common.card.EllipticCurve

internal class CardanoAddressServiceFacade : AddressService() {

    private val legacyService = CardanoAddressService(Blockchain.Cardano)
    private val trustWalletService = TrustWalletAddressService(Blockchain.Cardano)

    override fun makeAddress(walletPublicKey: ByteArray, curve: EllipticCurve?): String {
        return if (CardanoUtils.isExtendedPublicKey(walletPublicKey)) {
            trustWalletService.makeAddress(walletPublicKey, curve)
        } else {
            legacyService.makeAddress(walletPublicKey, curve)
        }
    }

    override fun validate(address: String): Boolean {
        return trustWalletService.validate(address) || legacyService.validate(address)
    }

    override fun makeAddresses(walletPublicKey: ByteArray, curve: EllipticCurve?): Set<Address> {
        return if (CardanoUtils.isExtendedPublicKey(walletPublicKey)) {
            trustWalletService.makeAddresses(walletPublicKey, curve)
        } else {
            legacyService.makeAddresses(walletPublicKey, curve)
        }
    }
}
