package com.github.iojjj.bootstrap.test

import org.junit.runners.model.InitializationError
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test runner that applies customization to all `Robolectric` tests.
 */
class BsRobolectricTestRunner @Throws(InitializationError::class) constructor(testClass: Class<*>) : RobolectricTestRunner(testClass) {

    companion object {

        private const val SDK_VERSION = 27
    }

    override fun buildGlobalConfig(): Config {
        return Config.Builder()
                .setSdk(SDK_VERSION)
                .build()
    }
}
