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

package com.cloudkitchens.driver

import com.cloudkitchens.*
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE
import tech.sirwellington.alchemy.generator.PeopleGenerators
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

/**
 * Responsible for dispatching drivers in response to [new orders][GlobalEvents.onOrderPrepared].
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
interface Dispatcher: EventListener
{

    /**
     * Defines the possible values for traffic delays, in seconds.
     */
    var trafficDelayRange: IntRange

    fun connect(events: GlobalEvents)

    fun disconnect(events: GlobalEvents)

    companion object
    {

        val UNLIMITED = UnlimitedDispatcher(scheduler = Executors.newScheduledThreadPool(2),
                                            trafficDelayRange = 2..10)

    }
}

//===========================================
// UNLIMITED DISPATCHER
//===========================================

/**
 * This Dispatchers creates a driver-per-order. In real life, there is a limited amount of drivers
 * available to fulfill orders at any given time.
 *
 * @author SirWellington
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
class UnlimitedDispatcher(private val scheduler: ScheduledExecutorService,
                          override var trafficDelayRange: IntRange): Dispatcher
{

    private val LOG = getLogger()
    private var events: GlobalEvents? = null

    override fun connect(events: GlobalEvents)
    {
        events.subscribe(this)
        this.events = events
    }

    override fun disconnect(events: GlobalEvents)
    {
        events.unsubscribe(this)
        this.events = null
    }

    private fun driverForOrder(order: Order): Driver
    {
        val name = PeopleGenerators.names().get()

        return Driver(name = name,
                      scheduler = scheduler,
                      trafficDelayRange = trafficDelayRange,
                      listener = events ?: this)
    }

    override fun onOrderAddedToShelf(order: Order, shelfSet: ShelfSet, shelf: Shelf)
    {
        val driver = driverForOrder(order)
        LOG.info("Dispatching driver [${driver.name}] to pickup order [${order.id}]")
        driver.respondToOrder(order.id, shelfSet)
    }

}
