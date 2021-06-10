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

import com.cloudkitchens.EventListener
import com.cloudkitchens.ShelfSet
import com.cloudkitchens.getLogger
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Responsible for picking up orders and making deliveries.
 *
 * @author SirWellington
 */
class Driver(val name: String,
             private val scheduler: ScheduledExecutorService,
             private val trafficDelayRange: IntRange,
             private val listener: EventListener)
{

    private val LOG = getLogger()

    fun respondToOrder(orderId: String, shelfSet: ShelfSet)
    {
        val trafficDelay = trafficDelayRange.random().toLong()
        val command = Runnable { this.pickupOrder(orderId, shelfSet) }
        scheduler.schedule(command, trafficDelay, SECONDS)
        LOG.info("[$name] on their way to pickup order [$orderId]")
    }

    internal fun pickupOrder(orderId: String, shelfSet: ShelfSet)
    {
        val order = shelfSet.pickupOrder(orderId)

        if (order == null)
        {
            LOG.warn("Driver [$name] could not find order [$orderId] on the shelves.")
        }
        else
        {
            listener.onOrderPickedUp(order, shelfSet, this)
            listener.onOrderDelivered(order, this)
        }
    }

}

