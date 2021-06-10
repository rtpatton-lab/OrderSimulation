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

import com.cloudkitchens.OrderGenerator.Companion
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.kotlin.extensions.createListOf
import tech.sirwellington.alchemy.kotlin.extensions.random
import tech.sirwellington.alchemy.test.hamcrest.notNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.Repeat


//===========================================
// RANDOM GENERATOR
//===========================================
class RandomOrderGeneratorTest: BaseOrderGeneratorTest()
{

    override fun createInstance(): OrderGenerator
    {
        return OrderGenerator.random
    }

    @DontRepeat
    @Test
    fun testOrderUniqueness()
    {
        val size = Int.random(100, 1_000)
        val orders = createListOf(size) { instance.generateOrderRequest() }
        val set = orders.toSet()
        assertThat(set.size, equalTo(size))
    }

}

//===========================================
// FROM RESOURCE FILE
//===========================================
class GeneratorFromResourceTests: BaseOrderGeneratorTest()
{

    override fun createInstance(): OrderGenerator
    {
        return Companion.fromResourceFile
    }

}

//===========================================
// BASE TEST
//===========================================
@RunWith(AlchemyTestRunner::class)
@Repeat
abstract class BaseOrderGeneratorTest
{

    protected lateinit var instance: OrderGenerator

    @Before
    fun setup()
    {
        instance = createInstance()
    }

    abstract fun createInstance(): OrderGenerator


    @Test
    fun testGenerateOrderRequest()
    {
        val order = instance.generateOrderRequest()
        assertThat(order, notNull)
    }

}