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

import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE
import kotlin.random.Random


/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
interface PoissonGenerator
{
    fun getPoisson(lambda: Double): Int

    companion object
    {
        val LINEAR: PoissonGenerator = LinearPoissonGenerator()
        val KNUTH : PoissonGenerator = KnuthPoissonGenerator()
    }
}

@StrategyPattern(role = CONCRETE_BEHAVIOR)
private class KnuthPoissonGenerator: PoissonGenerator
{

    override fun getPoisson(lambda: Double): Int
    {
        val L = Math.exp(-lambda)
        var k = 0
        var p = 1.0

        do
        {
            k++
            val u = Math.random()
            p *= u
        } while (p > L)

        return k - 1
    }
}

/**
 * Taken from [https://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables](https://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables).
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
private class LinearPoissonGenerator(private var STEP: Int = 500): PoissonGenerator
{

    private var eSTEP = Math.exp(STEP.toDouble())

    override fun getPoisson(lambda: Double): Int
    {

        var λ = 0
        var k = 0
        var p = 1.0

        do
        {
            k += 1
            val u = Random.nextDouble()
            p *= u

            while (p < 1 && λ > 0)
            {
                if (λ > STEP)
                {
                    p *= eSTEP
                    λ -= STEP
                }
                else
                {
                    p *= Math.exp(λ.toDouble())
                    λ = 0
                }
            }
        }
        while(p > 1)

        return k - 1
    }
}