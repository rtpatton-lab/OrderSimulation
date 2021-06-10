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

import com.cloudkitchens.driver.Dispatcher
import com.cloudkitchens.driver.UnlimitedDispatcher
import com.google.gson.GsonBuilder
import tech.sirwellington.alchemy.kotlin.extensions.createListOf
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * Runs the simulation of a kitchen with the given parameters.
 *
 * @author SirWellington
 */
class KitchenSimulation
{

    private val LOG = getLogger()

    private var scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(4)
    private var orderGenerator: OrderGenerator = OrderGenerator.fromResourceFile
    private var poissonGenerator = PoissonGenerator.KNUTH
    private var λ = 3.25
    private var events: GlobalEvents = GlobalEvents
    private var shelfSet: ShelfSet = ShelfSet.newDefaultShelfSet(events = events)
    private var kitchen: Kitchen = Kitchen.newCaliforniaKitchen(events = events, shelfSet = shelfSet)
    private var deliveryTimeRange = 5..30
    private var dispatcher: Dispatcher = UnlimitedDispatcher(trafficDelayRange = deliveryTimeRange, scheduler = scheduler)
    private var display: Display = Display.Logger
    private var gson = GsonBuilder().setPrettyPrinting().create()


    fun begin()
    {
        dispatcher.connect(events)

        val generateOrders = Runnable { generateNewOrders() }
        scheduler.scheduleAtFixedRate(generateOrders, 0, 1, TimeUnit.SECONDS)

        display.connect(events)
    }

    fun stop()
    {
        scheduler.shutdownNow()
        dispatcher.disconnect(events)
        display.disconnect(events)
    }

    private fun generateNewOrders()
    {
        val newOrderCount = poissonGenerator.getPoisson(λ)
        val newOrders = createListOf(newOrderCount)
        {
            orderGenerator.generateOrderRequest()
        }

        LOG.info("Generating [$newOrderCount] new orders…")
        newOrders.forEach { kitchen.receiveOrder(it) }
        LOG.info("Added [$newOrderCount] new orders to the kitchen")
    }

    fun withDisplay(display: Display): KitchenSimulation
    {
        this.display.disconnect(events)
        this.display = display
        display.connect(events)

        return this
    }

    fun withDisplayToFile(file: File): KitchenSimulation
    {
        val display = FileAppendDisplay(file, gson)
        return withDisplay(display)
    }

    fun withPoissonGenerator(poissonGenerator: PoissonGenerator): KitchenSimulation
    {
        this.poissonGenerator = poissonGenerator
        return this
    }

    fun withPoissonLambda(λ: Double): KitchenSimulation
    {
        this.λ = maxOf(λ, 1.0)
        return this
    }

    fun withTrafficDelayRange(range: IntRange): KitchenSimulation
    {
        this.deliveryTimeRange = range
        this.dispatcher.trafficDelayRange = range

        return this
    }

}