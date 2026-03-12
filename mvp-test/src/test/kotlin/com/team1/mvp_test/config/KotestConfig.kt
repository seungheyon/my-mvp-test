package com.team1.mvp_test.config

import io.kotest.common.ExperimentalKotest
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.ProjectListener
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

object KotestConfig : AbstractProjectConfig() {
    //override val testListeners = listOf(ExecutionTimeLogger)

    private var totalTestTime: Duration = Duration.ZERO

    // Use a ProjectListener instead of overriding afterProject/beforeProject
    object projectListener : ProjectListener {
        override suspend fun beforeProject() {
            totalTestTime = Duration.ZERO // Reset timer before tests start
            println("Starting Kotest Project")
        }

        override suspend fun afterProject() {
            println("Total test execution time: $totalTestTime")
        }
    }

    // override val listeners = listOf(projectListener)
    //
    // override val displayTestTimes: Boolean = true // Enables per-test execution time logging

    @ExperimentalKotest
    override val concurrentSpecs = Int.MAX_VALUE
    override val parallelism: Int = 4
}