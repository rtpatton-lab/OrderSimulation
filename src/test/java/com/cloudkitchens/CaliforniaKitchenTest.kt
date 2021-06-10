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

import com.nhaarman.mockito_kotlin.argWhere
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateCustom
import tech.sirwellington.alchemy.test.junit.runners.Repeat

@RunWith(AlchemyTestRunner::class)
@Repeat
class CaliforniaKitchenTest
{

    private lateinit var instance: Kitchen

    @Mock
    private lateinit var shelfSet: ShelfSet

    @Mock
    private lateinit var events: GlobalEvents

    @GenerateCustom(Generators.OrderRequestAlchemyGenerator::class)
    private lateinit var request: OrderRequest


    @Before
    fun setup()
    {
        instance = Kitchen.newCaliforniaKitchen(events = events, shelfSet = shelfSet)
    }

    @Test
    fun testReceiveOrder()
    {
        instance.receiveOrder(request)

        verify(shelfSet).addOrder(argWhere { it.request == request })
        verify(events).onOrderReceived(request)
        verify(events).onOrderPrepared(argWhere { it.request == request })
    }

}