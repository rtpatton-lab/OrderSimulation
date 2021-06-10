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
import tech.sirwellington.alchemy.annotations.designs.patterns.SingletonPattern
import tech.sirwellington.alchemy.kotlin.extensions.asWeak
import tech.sirwellington.alchemy.kotlin.extensions.removeWhere
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Listens for events across the system.
 */
interface EventListener
{
    fun onOrderReceived(request: OrderRequest) { }
    fun onOrderPrepared(order: Order) { }
    fun onOrderAddedToShelf(order: Order, shelfSet: ShelfSet, shelf: Shelf) { }
    fun onOrderDiscarded(order: Order) { }
    fun onOrderPickedUp(order: Order, shelfSet: ShelfSet, driver: Driver) { }
    fun onOrderDelivered(order: Order, driver: Driver) { }
}

/**
 * This is the [Singleton][SingletonPattern] instance where
 * all events flow through the system.
 *
 * @author SirWellington
 */
@SingletonPattern
object GlobalEvents: EventListener
{

    private val LOG = getLogger()

    private val listeners: MutableList<WeakReference<EventListener>> = mutableListOf()
    internal var eventThread: Executor = Executors.newSingleThreadExecutor()

    fun subscribe(listener: EventListener)
    {
        val weakRef = listener.asWeak()
        listeners.add(weakRef)
    }

    fun unsubscribe(listener: EventListener)
    {
        listeners.removeWhere { it.get() === listener }
    }

    override fun onOrderReceived(request: OrderRequest)
    {
        listeners.forEach()
        {
            eventThread.execute { it.get()?.onOrderReceived(request)  }
        }
    }

    override fun onOrderPrepared(order: Order)
    {
        listeners.forEach()
        {
            eventThread.execute { it.get()?.onOrderPrepared(order) }
        }

    }

    override fun onOrderDiscarded(order: Order)
    {
        LOG.warn("Order discarded => [${order.id}]")
        listeners.forEach()
        {
            eventThread.execute { it.get()?.onOrderDiscarded(order) }
        }
    }

    override fun onOrderAddedToShelf(order: Order, shelfSet: ShelfSet, shelf: Shelf)
    {
        listeners.forEach()
        {
            eventThread.execute { it.get()?.onOrderAddedToShelf(order, shelfSet, shelf) }
        }
    }

    override fun onOrderPickedUp(order: Order, shelfSet: ShelfSet, driver: Driver)
    {
        LOG.info("Order picked up [${order.id}] by [${driver.name}] at value [${order.value}] or [${order.normalizedValue}]")

        listeners.forEach()
        {
            eventThread.execute { it.get()?.onOrderPickedUp(order, shelfSet, driver) }
        }
    }

    override fun onOrderDelivered(order: Order, driver: Driver)
    {
        LOG.info("Order [${order.id}] delivered by [${driver.name}]")

        listeners.forEach()
        {
            eventThread.execute { it.get()?.onOrderDelivered(order, driver) }
        }
    }

}
