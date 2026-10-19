package dev.plain.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ButterDreamTest {
    @Test fun `bouncing reflects at both edges and preserves overshoot`() {
        assertEquals(0f, bouncePosition(0f, 100f), 0f)
        assertEquals(100f, bouncePosition(100f, 100f), 0f)
        assertEquals(95f, bouncePosition(105f, 100f), 0f)
        assertEquals(0f, bouncePosition(200f, 100f), 0f)
        assertEquals(5f, bouncePosition(405f, 100f), 0f)
        for (distance in 0..10_000) assertTrue(bouncePosition(distance.toFloat(), 100f) in 0f..100f)
    }

    @Test fun `screens smaller than the wordmark have no negative travel`() {
        assertEquals(0f, bouncePosition(200f, 0f), 0f)
        assertEquals(0f, bouncePosition(200f, -20f), 0f)
    }
}
