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

import tech.sirwellington.alchemy.kotlin.extensions.anyElement
import tech.sirwellington.alchemy.kotlin.extensions.tryOrNull
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


//===========================================
// ORDER
//===========================================

/**
 * Represents an incoming order to the system.
 *
 * @param name The name of the order
 * @param temp The temperature of the food, as measured in [Temperature]
 * @param shelfLife Read in seconds
 * @param decayRate The rate at which this order loses its value.
 *                  Once the value of an order reaches 0, it is defined as waste and should be removed from the shelf.
 * @author SirWellington
 */
data class OrderRequest(val name: String,
                        val temp: Temperature,
                        val shelfLife: Int,
                        val decayRate: Double)

/**
 * Represents an order that has been fulfilled by the [Kitchen].
 *
 * @param request The incoming request that generated this order.
 * @param id The unique ID of this order.
 * @param timeOfOrder The time this order was created.
 * @author SirWellington
 */
data class Order(val request: OrderRequest,
                 val id: String,
                 val timeOfOrder: ZonedDateTime = ZonedDateTime.now())
{

    private val LOG = getLogger()

    private val placementHistory: MutableList<Pair<Instant, ShelfType>> = mutableListOf()

    val temperature get() = request.temp
    val shelfLife get() = request.shelfLife
    val decayRate get() = request.decayRate

    fun decayRateIn(shelfType: ShelfType): Double
    {
        return when (shelfType)
        {
            ShelfType.OVERFLOW -> decayRate * 2
            else               -> decayRate
        }
    }

    /**
     * Registers placement of this order on a shelf.
     * This is used to calculate the proper [value] for the order.
     */
    fun registerPlacementIn(shelf: Shelf)
    {
        val type = shelf.type
        val time = Instant.now()
        placementHistory.add(Pair(time, type))
    }

    val value: Int
        get()
        {
//            val value = (shelfLife - orderAge) - (decayRate * orderAge)

            // Start with the shelf life
            var value = shelfLife

            // Remove value with each placement
            for (i in 0 until placementHistory.size)
            {
                val shelfType = placementHistory[i].second
                val start = placementHistory[i].first
                val end = placementHistory.getOrNull(i + 1)?.first ?: Instant.now()
                val age = start.until(end, ChronoUnit.SECONDS).absoluteValue
                val decayRate = decayRateIn(shelfType)
                val calculatedValue = (shelfLife - age) - (decayRate * age)
                val adjustment = (shelfLife - calculatedValue).absoluteValue.roundToInt()
                value -= adjustment
            }

            if (value <= 0)
            {
                LOG.warn("Order now has no value [$value]")
            }

            return value
        }

    val normalizedValue: Double
        get() = value.toDouble() / shelfLife.toDouble()

    /** Determines whether this order is now waste and should be removed. */
    val isWaste get() = value <= 0

}


enum class Temperature
{

    FROZEN,
    COLD,
    HOT

    ;

    companion object
    {
        @JvmStatic
        val all = values().toList()

        @JvmStatic
        val any get() = all.anyElement ?: HOT

        @JvmStatic
        fun fromString(string: String): Temperature?
        {
            return tryOrNull { valueOf(string.toUpperCase()) }
        }
    }
}
