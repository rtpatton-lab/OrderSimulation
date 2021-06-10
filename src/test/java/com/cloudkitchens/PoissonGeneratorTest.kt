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
import com.natpryce.hamkrest.greaterThanOrEqualTo
import com.natpryce.hamkrest.lessThan
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.generator.NumberGenerators
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import kotlin.math.roundToInt


//===========================================
// LINEAR TEST
//===========================================
class LinearPoissonGeneratorTest: PoissonGeneratorTest()
{

    override fun createInstance(): PoissonGenerator
    {
        return PoissonGenerator.LINEAR
    }

}

//===========================================
// KNUTH TEST
//===========================================
class KnuthPoissonGeneratorTest: PoissonGeneratorTest()
{

    override fun createInstance(): PoissonGenerator
    {
        return PoissonGenerator.KNUTH
    }
}

//===========================================
// BASE TEST CLASS
//===========================================
@RunWith(AlchemyTestRunner::class)
@Repeat(1_000)
abstract class PoissonGeneratorTest
{

    protected lateinit var instance: PoissonGenerator

    @Before
    fun setup()
    {
        instance = createInstance()
    }

    abstract fun createInstance(): PoissonGenerator

    @Test
    fun testGetPoisson()
    {
        val min = 1.0
        val max = 100.0
        val λ = NumberGenerators.doubles(min, max).get()
        val result = instance.getPoisson(λ)
        assertThat(result, greaterThanOrEqualTo(0))
        assertThat(result, lessThan(max.toInt() toThe 2))
    }

    private infix fun Int.toThe(rhs: Int): Int
    {
        return Math.pow(this.toDouble(), rhs.toDouble()).roundToInt()
    }


}
