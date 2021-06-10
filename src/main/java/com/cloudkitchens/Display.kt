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

import com.cloudkitchens.driver.Driver
import com.google.gson.Gson
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE
import java.io.File


/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
interface Display: EventListener
{

    fun displayShelfSet(shelfSet: ShelfSet)
    {

    }

    fun connect(events: GlobalEvents)
    {
        events.subscribe(this)
    }

    fun disconnect(events: GlobalEvents)
    {
        events.unsubscribe(this)
    }

    override fun onOrderAddedToShelf(order: Order, shelfSet: ShelfSet, shelf: Shelf)
    {
        displayShelfSet(shelfSet)
    }

    override fun onOrderPickedUp(order: Order, shelfSet: ShelfSet, driver: Driver)
    {
        displayShelfSet(shelfSet)
    }

    companion object Instances
    {
        val Print = PrintDisplay()
        val Logger = LoggerDisplay()
    }

}

data class ShelfStatus(val shelfType: ShelfType,
                       val size: Int,
                       val capacity: Int,
                       val orders: List<OrderSummary>)

data class OrderSummary(val name: String,
                        val id: String,
                        val temperature: Temperature,
                        val shelfLife: Int,
                        val value: Int,
                        val normalizedValue: Double)

val Shelf.status: ShelfStatus
    get() = ShelfStatus(shelfType = type,
                        size = size,
                        capacity = capacity,
                        orders = items.map { it.summary })

val ShelfSet.status get() = shelves.map { it.status }

val Order.summary get() = OrderSummary(name = request.name,
                                       id = id,
                                       shelfLife = shelfLife,
                                       temperature = temperature,
                                       value = value,
                                       normalizedValue = normalizedValue)

//===========================================
// PRINT DISPLAY
//===========================================
@StrategyPattern(role = CONCRETE_BEHAVIOR)
class PrintDisplay(private val gson: Gson = Gson()): Display
{

    override fun displayShelfSet(shelfSet: ShelfSet)
    {
        val status = shelfSet.status
        val json = gson.toJson(status)
        println("–––––Log Display–––––\n$json\n\n")
    }

}

//===========================================
// LOGGER DISPLAY
//===========================================
@StrategyPattern(role = CONCRETE_BEHAVIOR)
class LoggerDisplay(private val gson: Gson = Gson()): Display
{
    private val LOG = getLogger()

    override fun displayShelfSet(shelfSet: ShelfSet)
    {
        val status = shelfSet.status
        val json = gson.toJson(status)
        LOG.info("\n–––––Log Display–––––\n$json\n\n")
    }
}

//===========================================
// FILE APPENDER
//===========================================
@StrategyPattern(role = CONCRETE_BEHAVIOR)
class FileAppendDisplay(val file: File,
                        val gson: Gson = Gson()): Display
{

    private val LOG = getLogger()

    override fun displayShelfSet(shelfSet: ShelfSet)
    {
        val status = shelfSet.status
        val json = gson.toJson(status)
        file.writeText(json, Charsets.UTF_8)

        LOG.info("Updated display to file [$file]")
    }
}
