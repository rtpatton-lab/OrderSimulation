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

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR
import tech.sirwellington.alchemy.generator.StringGenerators
import tech.sirwellington.alchemy.kotlin.extensions.anyElement
import tech.sirwellington.alchemy.kotlin.extensions.random


//===========================================
// ORDER GENERATOR
//===========================================
/**
 * Responsible for generating a new Order for use with the [KitchenSimulation].
 * @author SirWellington
 */
interface OrderGenerator
{

    fun generateOrderRequest(): OrderRequest

    companion object
    {
        val random: OrderGenerator = RandomOrderGenerator()
        val fromResourceFile: OrderGenerator = GeneratorFromResource()
    }
}

//===========================================
// RANDOM GENERATOR
//===========================================
@StrategyPattern(role = CONCRETE_BEHAVIOR)
internal class RandomOrderGenerator: OrderGenerator
{
    override fun generateOrderRequest(): OrderRequest
    {
        return OrderRequest(name = StringGenerators.alphabeticStrings().get(),
                            decayRate = Double.random(0.0, 1.0),
                            shelfLife = Int.random(100, 300),
                            temp = Temperature.any)
    }
}


//===========================================
// GENERATOR FROM RESOURCE FILE
//===========================================
@StrategyPattern(role = CONCRETE_BEHAVIOR)
internal class GeneratorFromResource: OrderGenerator
{
    private val LOG = getLogger()

    private var gson = GsonBuilder()
                            .setPrettyPrinting()
                            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                            .registerTypeAdapter(Temperature::class.java, TemperatureAdapter)
                            .create()

    private val orders = parseOrders()

    override fun generateOrderRequest(): OrderRequest
    {
        return orders.anyElement!!
    }

    internal fun parseOrders(): List<OrderRequest>
    {
        val file = this.javaClass.classLoader.getResource("data/sample-data.json") ?: return emptyList()
        val json = file.readText(Charsets.UTF_8)
        val type = object: TypeToken<List<OrderRequest>>() {}

        return try
        {
            gson.fromJson<List<OrderRequest>>(json, type.type)
        }
        catch (ex: Exception)
        {
            LOG.error("Failed to parse json from file", ex)
            return emptyList()
        }
    }

}

//===========================================
// GSON ADAPTERS
//===========================================
object TemperatureAdapter: TypeAdapter<Temperature>()
{

    override fun write(out: JsonWriter?, value: Temperature?)
    {
        val string = value?.toString()?.toLowerCase() ?: return
        out?.value(string)
    }

    override fun read(`in`: JsonReader?): Temperature?
    {
        val string = `in`?.nextString() ?: return null
        val temp = Temperature.fromString(string)
        return temp
    }
}