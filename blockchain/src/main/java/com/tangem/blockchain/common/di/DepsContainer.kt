package com.tangem.blockchain.common.di

import com.tangem.blockchain.common.BlockchainFeatureToggles
import com.tangem.blockchain.common.BlockchainSdkConfig
import kotlin.properties.Delegates

/**
 * Container for keeping dependencies
 *
 * @author Andrew Khokhlov on 28/02/2024
 */
internal object DepsContainer {

    var blockchainSdkConfig: BlockchainSdkConfig by Delegates.notNull()
        private set

    var blockchainFeatureToggles: BlockchainFeatureToggles by Delegates.notNull()
        private set

    /** Save dependencies that provided on BlockchainSDK's creation */
    fun onInit(config: BlockchainSdkConfig, featureToggles: BlockchainFeatureToggles) {
        blockchainSdkConfig = config
        blockchainFeatureToggles = featureToggles
    }
}
