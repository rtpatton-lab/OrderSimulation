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

import tech.sirwellington.alchemy.generator.AlchemyGenerator
import tech.sirwellington.alchemy.generator.PeopleGenerators
import tech.sirwellington.alchemy.generator.StringGenerators
import tech.sirwellington.alchemy.generator.TimeGenerators
import tech.sirwellington.alchemy.kotlin.extensions.random
import java.time.ZoneOffset


/**
 *
 * @author SirWellington
 */
object Generators
{

    class OrderRequestAlchemyGenerator: AlchemyGenerator<OrderRequest>
    {
        override fun get() = orderRequest()
    }

    class OrderAlchemyGenerator: AlchemyGenerator<Order>
    {
        override fun get() = order()
    }

    fun orderRequest(name: String = Strings.name,
                     temp: Temperature = Temperature.any,
                     shelfLife: Int = Int.random(100, 500),
                     decayRate: Double = Double.random(0.1, 0.9)): OrderRequest
    {
        return OrderRequest(name = name,
                            temp = temp,
                            shelfLife = shelfLife,
                            decayRate = decayRate)
    }


    fun order(id: String = Strings.id,
              request: OrderRequest = orderRequest()): Order
    {
        return Order(request = request,
                     id = id,
                     timeOfOrder = Times.present)
    }

    object Strings
    {
        val name get() = PeopleGenerators.names().get()

        val id get() = StringGenerators.hexadecimalString(20).get()
    }

    object Times
    {
        val any get() = TimeGenerators.anytime().get().atZone(ZoneOffset.UTC)

        val past get() = TimeGenerators.pastInstants().get().atZone(ZoneOffset.UTC)

        val present get() = TimeGenerators.presentInstants().get().atZone(ZoneOffset.UTC)

        val future get() = TimeGenerators.futureInstants().get().atZone(ZoneOffset.UTC)
    }


}