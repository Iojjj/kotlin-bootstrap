package kotlinx

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JUnit test suite for [ByteArray] extensions.
 */
class ByteArrayTest {

    private val map: Map<String, String> = mapOf(
            "awesome" to "617765736f6d65",
            "qwerty" to "717765727479",
            "sample" to "73616d706c65"
    )

    @Test
    fun toHexString_whenNonNullPassed_shouldConvertToHexString() {
        map.forEach { string, hex -> assertTrue(hex.equals(string.toByteArray().toHexString(), true)) }
    }
}