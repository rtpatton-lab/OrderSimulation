/*
 * Copyright 2019 SirWellington
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudkitchens

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.sirwellington.alchemy.kotlin.extensions.random
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import java.io.File

@RunWith(AlchemyTestRunner::class)
class KitchenSimulationTest
{

    private lateinit var instance: KitchenSimulation

    @Mock
    private lateinit var display: Display

    @Mock
    private lateinit var poissonGenerator: PoissonGenerator

    @Before
    fun setup()
    {
        instance = KitchenSimulation()
    }

    @After
    fun done()
    {
        instance.stop()
    }

    @Test
    fun testBegin()
    {
        instance.begin()

        Thread.sleep(100)
    }

    @Test
    fun testStop()
    {
        instance.begin()

        Thread.sleep(100)

        instance.stop()
    }

    @Test
    fun testWithDisplay()
    {
        val result = instance.withDisplay(display)
        assertThat(result, notNull and equalTo(instance))
    }

    @Test
    fun testWithDisplayToFile()
    {
        val file = File.createTempFile("something", ".json")
        val result = instance.withDisplayToFile(file)
        assertThat(result, notNull and equalTo(instance))
    }

    @Test
    fun testWithPoissonGenerator()
    {
        val result = instance.withPoissonGenerator(poissonGenerator)
        assertThat(result, notNull and equalTo(instance))
    }

    @Test
    fun testWithPoissonLambda()
    {
        val λ = Double.random(1.0, 10.0)
        val result = instance.withPoissonLambda(λ)
        assertThat(result, notNull and equalTo(instance))
    }

    @Test
    fun testWithTrafficDelayRange()
    {
        val min = Int.random(1, 10)
        val max = Int.random(min + 1, 200)
        val result = instance.withTrafficDelayRange(min..max)
        assertThat(result, notNull and equalTo(instance))
    }

}