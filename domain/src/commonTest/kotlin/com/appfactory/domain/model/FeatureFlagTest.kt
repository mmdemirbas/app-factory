package com.appfactory.domain.model

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlin.test.Test
import com.appfactory.domain.model.TeamId

class FeatureFlagTest {

    private val mockTeamId = TeamId.generate()

    @Test
    fun `valid key and description creates a flag with generated id`() {
        val result = FeatureFlag.create(mockTeamId, "my_flag", "Test flag")
        result.isSuccess shouldBe true
        result.getOrNull()?.id shouldNotBe null
        result.getOrNull()?.key shouldBe "my_flag"
    }

    @Test
    fun `blank key always fails validation`() {
        val result = FeatureFlag.create(mockTeamId, "", "description")
        result.isFailure shouldBe true
    }

    @Test
    fun `key with uppercase always fails validation`() {
        val result = FeatureFlag.create(mockTeamId, "MyFlag", "description")
        result.isFailure shouldBe true
    }

    @Test
    fun `created flag respects defaultEnabled`() {
        val enabled = FeatureFlag.create(mockTeamId, "flag_on", "on", defaultEnabled = true)
        val disabled = FeatureFlag.create(mockTeamId, "flag_off", "off", defaultEnabled = false)

        enabled.getOrNull()?.defaultEnabled shouldBe true
        disabled.getOrNull()?.defaultEnabled shouldBe false
    }

    @Test
    fun `environment override takes precedence over default`() {
        val flag = FeatureFlag.create(mockTeamId, "f", "desc", defaultEnabled = false)
            .getOrNull()!!
            .copy(environmentOverrides = mapOf(Environment.PROD to true))

        flag.isEnabledFor(Environment.PROD) shouldBe true
        flag.isEnabledFor(Environment.DEV) shouldBe false
    }
}
