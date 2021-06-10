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

import com.google.gson.GsonBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.GenerateList
import java.io.File

//===========================================
// LOGGER DISPLAY TEST
//===========================================
class LoggerDisplayTest : BaseDisplayTest()
{

    override fun createDisplay(): Display
    {
        return LoggerDisplay(gson)
    }

}

//===========================================
// PRINT DISPLAY TEST
//===========================================
class PrintDisplayTest : BaseDisplayTest()
{

    override fun createDisplay(): Display
    {
        return PrintDisplay(gson)
    }

}

//===========================================
// FILE APPENDER
//===========================================
class FileAppendDisplayTest: BaseDisplayTest()
{
    override fun createDisplay(): Display
    {
        val file = File.createTempFile("Display", ".json")
        return FileAppendDisplay(file, gson)
    }
}

//===========================================
// BASE TEST CLASS
//===========================================
@RunWith(AlchemyTestRunner::class)
abstract class BaseDisplayTest
{

    private lateinit var display: Display

    protected val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()


    @GenerateList(value = Order::class, size = 10, customGenerator = Generators.OrderAlchemyGenerator::class)
    private lateinit var orders: List<Order>

    private lateinit var shelfSet: ShelfSet

    @Before
    fun setup()
    {
        display = createDisplay()

        shelfSet = ShelfSet.newDefaultShelfSet(events = GlobalEvents)
    }

    abstract fun createDisplay(): Display

    @Test
    fun testDisplayWhenEmpty()
    {
        display.displayShelfSet(shelfSet)
    }

    @Test
    fun testDisplayWithOrders()
    {
        orders.forEach(shelfSet::addOrder)
        display.displayShelfSet(shelfSet)
    }

}