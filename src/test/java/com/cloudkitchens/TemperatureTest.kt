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

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.kotlin.extensions.Booleans
import tech.sirwellington.alchemy.test.hamcrest.notEmpty
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GenerateEnum
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
@Repeat
class TemperatureTest
{

    @GenerateEnum
    private lateinit var temperature: Temperature

    @Test
    fun testFromString()
    {
        var string = temperature.toString()

        if (Booleans.any)
        {
            string = string.toLowerCase()
        }

        val result = Temperature.fromString(string)
        assertThat(result, notNull)
        assertTrue { result == temperature }
    }

    @Test
    fun testAny()
    {
        val result = Temperature.any
        assertThat(result, notNull)
    }

    @DontRepeat
    @Test
    fun testAll()
    {
        val all = Temperature.all
        assertThat(all, notEmpty)
    }
}