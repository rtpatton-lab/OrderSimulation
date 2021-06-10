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
import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.kotlin.extensions.createListOf
import tech.sirwellington.alchemy.kotlin.extensions.random
import tech.sirwellington.alchemy.test.hamcrest.*
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateCustom
import tech.sirwellington.alchemy.test.junit.runners.GenerateEnum
import tech.sirwellington.alchemy.test.junit.runners.Repeat

@RunWith(AlchemyTestRunner::class)
@Repeat
class ShelfTest
{

    private lateinit var shelf: Shelf


    @GenerateEnum
    private lateinit var type: ShelfType

    @GenerateCustom(Generators.OrderAlchemyGenerator::class)
    private lateinit var order: Order
    private val orderId get() = order.id

    private lateinit var orders: List<Order>

    @Before
    fun setup()
    {
        shelf = Shelf.ofType(type)
        orders = createListOf(Int.random(1, shelf.capacity))
        {
            Generators.order()
        }
    }

    @Test
    fun testGetItemsWhenNone()
    {
        val result = shelf.items
        assertThat(result, isEmpty)
    }

    @Test
    fun testGetItemsWhenOne()
    {
        shelf.addOrder(order)
        val result = shelf.items
        assertThat(result, notEmpty and hasElement(order))
    }

    @Test
    fun testGetItemsWhenMultiple()
    {
        orders.forEach(shelf::addOrder)

        val result = shelf.items
        assertThat(result, notNullOrEmpty)

        orders.forEach()
        {
            assertThat(result, hasElement(it))
        }
    }

    @Test
    fun testAddOrder()
    {
        shelf.addOrder(order)
        val result = shelf.pickupOrder(orderId)
        assertThat(result, equalTo(order))
    }

    @Test
    fun testAddOrderTwice()
    {
        shelf.addOrder(order)
        shelf.addOrder(order)

        val result = shelf.pickupOrder(orderId)
        assertThat(result, equalTo(order))
    }

    @Test
    fun testAddOrderBeyondCapacity()
    {
        val n = shelf.capacity + 1
        val orders = createListOf(n) { Generators.order() }
        orders.forEach(shelf::addOrder)

        assertThat(shelf.size, equalTo(shelf.capacity))
        assertThat(shelf.items, hasSize(shelf.capacity))
    }

    @Test
    fun testPickupOrderWhenNone()
    {
        val result = shelf.pickupOrder(orderId)
        assertThat(result, isNull)
    }

    @Test
    fun testPickupOrderWhenOne()
    {
        shelf.addOrder(order)
        val result = shelf.pickupOrder(orderId)
        assertThat(result, equalTo(order))
    }

    @Test
    fun testRemoveItemOfTypeWhenNone()
    {
        val result = shelf.removeAnItem(order.request.temp)
        assertThat(result, isNull)
    }

    @Test
    fun testRemoveItemOfTypeWhenOne()
    {
        shelf.addOrder(order)
        val result = shelf.removeAnItem(order.request.temp)
        assertThat(result, equalTo(order))
    }

    @Test
    fun testRemoveItemOfTypeWhenMultiple()
    {
        orders.forEach(shelf::addOrder)
        val temperature = Temperature.any
        val validOrders = orders.filter { it.temperature == temperature }
        val result = shelf.removeAnItem(temperature)

        if (validOrders.isEmpty())
        {
            assertThat(result, isNull)
        }
        else
        {
            assertThat(result, notNull)
            assertThat(result!!, isIn(validOrders))
            assertThat(shelf.pickupOrder(result.id), isNull)
        }
    }

    @Test
    fun testRemoveWasteItemsWhenEmpty()
    {
        shelf.removeWasteItems()
    }

    @Test
    fun testGetType()
    {
        assertThat(shelf.type, equalTo(type))
    }

    @Test
    fun testGetCapacity()
    {
        val capacity = shelf.capacity
        val expected = if (type == OVERFLOW) 20 else 15
        assertThat(capacity, equalTo(expected))
    }

    @Test
    fun testNumberOfItemsWhenNone()
    {
        val result = shelf.numberOfItems(Temperature.any)
        assertThat(result, equalTo(0))
    }

    @Test
    fun testNumberOfItemsWhenOne()
    {
        shelf.addOrder(order)
        val result = shelf.numberOfItems(order.temperature)
        assertThat(result, equalTo(1))
    }

    @Test
    fun testNumberOfItemsWhenMultiple()
    {
        orders.forEach(shelf::addOrder)
        val temp = Temperature.any
        val expected = orders.count { it.temperature == temp }
        val result = shelf.numberOfItems(temp)
        assertThat(result, equalTo(expected))
    }

}