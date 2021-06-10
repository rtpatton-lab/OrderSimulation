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


import com.cloudkitchens.ShelfType.OVERFLOW
import com.cloudkitchens.Temperature.COLD
import com.cloudkitchens.Temperature.FROZEN
import com.cloudkitchens.Temperature.HOT
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role.FACTORY_METHOD
import tech.sirwellington.alchemy.annotations.designs.patterns.FactoryMethodPattern.Role.PRODUCT
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE
import java.util.concurrent.ConcurrentHashMap

/**
 * Where orders are placed once they are ready for delivery.
 *
 * @author SirWellington
 */
//===========================================
// SHELVES
//===========================================
@StrategyPattern(role = INTERFACE)
@FactoryMethodPattern(role = PRODUCT)
interface Shelf
{

    val capacity: Int
    val type: ShelfType
    val items: List<Order>
    val size: Int get() = items.size
    val isAtCapacity: Boolean get() = size >= capacity
    val notFull get() = !isAtCapacity

    fun addOrder(order: Order)
    fun pickupOrder(orderId: String): Order?
    fun removeAnItem(type: Temperature): Order?
    fun removeWasteItems()

    fun numberOfItems(temperature: Temperature): Int
    {
        return items.count { it.temperature == temperature }
    }

    companion object Factory
    {

        @FactoryMethodPattern(role = FACTORY_METHOD)
        fun ofType(type: ShelfType): Shelf
        {
            val capacity = if (type == OVERFLOW) 20 else 15
            return ShelfImpl(type = type, capacity = capacity)
        }
    }
}

enum class ShelfType
{
    HOT,
    COLD,
    FROZEN,
    OVERFLOW
}

//===========================================
// IMPLEMENTATION
//===========================================
@StrategyPattern(role = CONCRETE_BEHAVIOR)
internal class ShelfImpl(override val type: ShelfType,
                         override val capacity: Int = 15): Shelf
{

    private val orders = ConcurrentHashMap<String, Order>()
    private val LOG = getLogger()

    override val items: List<Order>
        get() =  orders.values.toList()

    override fun addOrder(order: Order)
    {
        if (isAtCapacity)
        {
            LOG.warn("Shelf is full! Cannot add new order [${order.id}]")
            return
        }

        orders[order.id] = order
        order.registerPlacementIn(this)
    }

    override fun pickupOrder(orderId: String): Order?
    {
        val order = orders.remove(orderId) ?: return null

        return if (order.isWaste) null else order
    }

    override fun removeAnItem(type: Temperature): Order?
    {
        val order = orders.values.firstOrNull { it.request.temp == type }
        return order?.id?.let { orders.remove(it) }
    }

    override fun removeWasteItems()
    {
        val wastedItems = orders.filter { it.value.isWaste }
        if (wastedItems.isEmpty()) return

        LOG.warn("Removed [${wastedItems.size}] waste items from [$type] shelf: [${wastedItems.map { it.key to it.value.normalizedValue }}]")
    }

}

//===========================================
// SHELF SET
//===========================================
/**
 * A [ShelfSet] is an abstraction over the group of [Shelves][Shelf] that are
 * used in a [Kitchen].
 *
 * @author SirWellington
 */
@FactoryMethodPattern(role = PRODUCT)
interface ShelfSet
{

    val hot: Shelf
    val cold: Shelf
    val frozen: Shelf
    val overflow: Shelf

    val shelves get() = listOf(hot, cold, frozen, overflow)

    fun addOrder(order: Order)

    fun pickupOrder(orderId: String): Order?

    fun removeWaste()
    {
        shelves.forEach { it.removeWasteItems() }
    }

    companion object Factory
    {

        @FactoryMethodPattern(role = FACTORY_METHOD)
        fun newDefaultShelfSet(hotShelf: Shelf = Shelf.ofType(ShelfType.HOT),
                               coldShelf: Shelf = Shelf.ofType(ShelfType.COLD),
                               frozenShelf: Shelf = Shelf.ofType(ShelfType.FROZEN),
                               overflowShelf: Shelf = Shelf.ofType(ShelfType.OVERFLOW),
                               events: GlobalEvents): ShelfSet
        {
            return ShelfSetImpl(events = events,
                                hot = hotShelf,
                                cold = coldShelf,
                                frozen = frozenShelf,
                                overflow = overflowShelf)
        }
    }

}

//===========================================
// SHELF SET IMPL
//===========================================
@ThreadSafe
internal class ShelfSetImpl(private val events: GlobalEvents,
                            override val hot: Shelf,
                            override val cold: Shelf,
                            override val frozen: Shelf,
                            override val overflow: Shelf): ShelfSet
{
    private val LOG = getLogger()
    private val lock = Object()

    override fun addOrder(order: Order)
    {
        LOG.info("Adding order [${order.id}] to shelf set]")

        synchronized(lock)
        {
            _addOrder(order)
        }
    }

    private fun _addOrder(order: Order, shouldRetry: Boolean = true)
    {
        val shelf = shelfFor(order.temperature)

        when
        {
            shelf.notFull    ->
            {
                shelf.addOrder(order)
                events.onOrderAddedToShelf(order, this, shelf)
            }

            overflow.notFull ->
            {
                overflow.addOrder(order)
                events.onOrderAddedToShelf(order, this, overflow)
            }

            else             ->
            {
                if (shouldRetry)
                {
                    _addOrder(order, shouldRetry = false)
                    LOG.warn("Both the [${shelf.type}] and the Overflow shelves are full! Clearing inventory.")
                    removeWaste()
                }
                else
                {
                    discard(order)
                }
            }
        }
    }

    override fun pickupOrder(orderId: String): Order?
    {
        synchronized(lock)
        {
            val results = shelves.mapNotNull { it.pickupOrder(orderId) }
            val order = results.firstOrNull() ?: return null
            rearrangeOrdersAtTemperature(order.temperature)
            return order
        }
    }

    private fun rearrangeOrdersAtTemperature(temperature: Temperature)
    {
        val shelf = shelfFor(temperature)

        while (overflow.numberOfItems(temperature) > 0 && shelf.notFull)
        {
            val nextItem = overflow.removeAnItem(temperature) ?: return
            if (nextItem.isWaste)
            {
                LOG.info("Clearing overflow of waste item [${nextItem.id}]")
                discard(nextItem)
            }
            else
            {
                LOG.warn("Moving order [${nextItem.id}] from overflow back to [$temperature] shelf at value [${nextItem.normalizedValue}].")
                shelf.addOrder(nextItem)
            }
        }
    }

    private fun shelfFor(temperature: Temperature): Shelf
    {
        return when (temperature)
        {
            COLD   -> cold
            HOT    -> hot
            FROZEN -> frozen
        }
    }

    private fun discard(order: Order)
    {
        LOG.error("Discarding order [${order.id}] of temp [${order.temperature}]. No space available to put it on.")
        events.onOrderDiscarded(order)
    }

}