package com.mohammdalloush.kanastascore.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {
    @Test
    fun comparesSemanticVersions() {
        assertTrue(VersionComparator.isNewer("v1.1.0", "1.0.0"))
        assertFalse(VersionComparator.isNewer("0.9.0-beta.1", "1.0.0"))
        assertEquals(0, VersionComparator.compare("1.0", "1.0.0"))
    }

    @Test
    fun prereleaseOrderingIsHandled() {
        assertTrue(VersionComparator.isNewer("1.0.0", "1.0.0-beta.1"))
        assertTrue(VersionComparator.isNewer("1.0.0-beta.2", "1.0.0-beta.1"))
    }
}
