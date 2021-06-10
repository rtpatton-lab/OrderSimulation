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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.sirwellington.alchemy.kotlin.extensions.createListOf
import tech.sirwellington.alchemy.kotlin.extensions.random
import tech.sirwellington.alchemy.test.hamcrest.isNull
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateCustom
import tech.sirwellington.alchemy.test.junit.runners.Repeat

@RunWith(AlchemyTestRunner::class)
@Repeat
class ShelfSetImplTest
{

    private lateinit var instance: ShelfSet

    @Mock
    private lateinit var hotShelf: Shelf
    @Mock
    private lateinit var coldShelf: Shelf
    @Mock
    private lateinit var frozenShelf: Shelf
    @Mock
    private lateinit var overflowShelf: Shelf

    @Mock
    private lateinit var display: Display

    @Mock
    private lateinit var events: GlobalEvents

    @GenerateCustom(Generators.OrderAlchemyGenerator::class)
    private lateinit var order: Order

    private lateinit var orders: List<Order>

    @Before
    fun setup()
    {
        instance = ShelfSet.newDefaultShelfSet(events = events,
                                               coldShelf = coldShelf,
                                               hotShelf = hotShelf,
                                               frozenShelf = frozenShelf,
                                               overflowShelf = overflowShelf)

        instance.shelves.forEach()
        {
            whenever(it.isAtCapacity).then { false }
            whenever(it.notFull).then { true }
        }
    }

    @Test
    fun testAddOrder()
    {
        instance.addOrder(order)
        val expectedShelf = shelfForOrder(order)
        verify(expectedShelf).addOrder(order)
    }

    @Test
    fun testAddOrderWhenMainShelfAtCapacity()
    {
        val expectedShelf = shelfForOrder(order)
        whenever(expectedShelf.notFull).then { false }
        whenever(expectedShelf.isAtCapacity).then { true }
        whenever(overflowShelf.notFull).then { true }

        instance.addOrder(order)
        verify(overflowShelf).addOrder(order)
        verify(expectedShelf, never()).addOrder(any())
    }

    @Test
    fun testAddOrderWhenMainAndOverflowAtCapacity()
    {
        val expectedShelf = shelfForOrder(order)
        whenever(expectedShelf.notFull).then { false }
        whenever(overflowShelf.notFull).then { false }

        instance.addOrder(order)
        verify(expectedShelf, never()).addOrder(any())
        verify(overflowShelf, never()).addOrder(any())

        verify(events).onOrderDiscarded(order)
    }

    @Test
    fun testPickupOrderWhenNone()
    {
        val result = instance.pickupOrder(order.id)
        assertThat(result, isNull)
    }

    @Test
    fun testPickupOrderWhenOne()
    {
        instance.addOrder(order)

        val expectedShelf = shelfForOrder(order)
        whenever(expectedShelf.pickupOrder(order.id)).then { order }

        val result = instance.pickupOrder(order.id)
        assertThat(result, equalTo(order))
        assertThat(result, equalTo(order))
    }

    @Test
    fun testPickupOrderWhenMultiple()
    {
        val orders = createListOf(Int.random(10, 100)) { Generators.order() }
        orders.forEach(instance::addOrder)

        val target = orders.random()
        val targetShelf = shelfForOrder(target)
        whenever(targetShelf.pickupOrder(target.id)).then { target }

        val result = instance.pickupOrder(target.id)
        assertThat(result, equalTo(target))
    }

    @Test
    fun testGetHot()
    {
        assertThat(instance.hot, equalTo(hotShelf))
    }

    @Test
    fun testGetCold()
    {
        assertThat(instance.cold, equalTo(coldShelf))
    }

    @Test
    fun testGetFrozen()
    {
        assertThat(instance.frozen, equalTo(frozenShelf))
    }

    @Test
    fun testGetOverflow()
    {
        assertThat(instance.overflow, equalTo(overflowShelf))
    }

    private fun shelfForOrder(order: Order): Shelf
    {
        return when (order.request.temp)
        {
            Temperature.HOT    -> hotShelf
            Temperature.COLD   -> coldShelf
            Temperature.FROZEN -> frozenShelf
        }
    }

}