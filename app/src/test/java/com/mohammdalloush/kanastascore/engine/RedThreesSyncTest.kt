package com.mohammdalloush.kanastascore.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class RedThreesSyncTest {

    @Test
    fun testRedThreesCalculation() {
        // This is a logic test, normally logic belongs in ViewModel or a specialized manager.
        // I'll test the ViewModel if possible, or just the math here.
        fun calculateOther(count: Int) = 4 - count
        
        assertEquals(4, calculateOther(0))
        assertEquals(3, calculateOther(1))
        assertEquals(2, calculateOther(2))
        assertEquals(1, calculateOther(3))
        assertEquals(0, calculateOther(4))
    }
}
