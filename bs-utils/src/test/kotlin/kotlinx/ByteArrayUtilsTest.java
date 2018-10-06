package kotlinx;

import org.junit.Test;

/**
 * JUnit test suite for {@code byte} array extensions.
 */
public final class ByteArrayUtilsTest {

    @Test(expected = IllegalArgumentException.class)
    public void toHexString_whenNullPassed_shouldThrowIllegalArgumentException() {
        ByteArrayUtils.toHexString(null);
    }
}