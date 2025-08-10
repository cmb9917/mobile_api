package com.mobileapi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class MobileApiApplicationTest {

    @Test
    fun `should load application context`() {
        // This test verifies that the Spring application context loads successfully
        // If the context fails to load, this test will fail
    }
}