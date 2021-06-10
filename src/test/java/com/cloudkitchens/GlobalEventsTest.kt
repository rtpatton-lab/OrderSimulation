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
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateCustom
import tech.sirwellington.alchemy.test.junit.runners.Repeat
import java.util.concurrent.Executor

@RunWith(AlchemyTestRunner::class)
@Repeat
class GlobalEventsTest
{

    private val instance = GlobalEvents

    @Mock
    private lateinit var listener: EventListener

    @GenerateCustom(Generators.OrderAlchemyGenerator::class)
    private lateinit var order: Order
    private val request get() = order.request

    @Mock
    private lateinit var driver: Driver

    @Mock
    private lateinit var shelfSet: ShelfSet
    @Mock
    private lateinit var shelf: Shelf

    @Before
    fun prepare()
    {
        instance.eventThread = Executor { it.run() }
        instance.subscribe(listener)
    }

    @Test
    fun testSubscribe()
    {
        instance.subscribe(listener)
    }

    @Test
    fun testUnsubscribe()
    {
        instance.unsubscribe(listener)
        instance.onOrderReceived(request)
        verify(listener, never()).onOrderReceived(request)
    }

    @Test
    fun testOnOrderReceived()
    {
        instance.onOrderReceived(request)
        verify(listener).onOrderReceived(request)
    }

    @Test
    fun testOnOrderPrepared()
    {
        instance.onOrderPrepared(order)
        verify(listener).onOrderPrepared(order)
    }

    @Test
    fun testOnOrderDiscarded()
    {
        instance.onOrderDiscarded(order)
        verify(listener).onOrderDiscarded(order)
    }

    @Test
    fun testOnOrderAddedToShelf()
    {
        instance.onOrderAddedToShelf(order, shelfSet, shelf)
        verify(listener).onOrderAddedToShelf(order, shelfSet, shelf)
    }

    @Test
    fun testOnOrderPickedUp()
    {
        instance.onOrderPickedUp(order, shelfSet, driver)
        verify(listener).onOrderPickedUp(order, shelfSet, driver)
    }

    @Test
    fun testOnOrderDelivered()
    {
        instance.onOrderDelivered(order, driver)
        verify(listener).onOrderDelivered(order, driver)
    }

}