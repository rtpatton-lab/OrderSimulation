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

import java.io.File


/**
 *
 * @author SirWellington
 */
object Main
{
    private val LOG = getLogger()

    private const val DISPLAY_FILE_PATH = "output.json"
    private val DISPLAY_FILE = File(DISPLAY_FILE_PATH)

    @JvmStatic
    fun main(args: Array<String>)
    {
        val system = KitchenSimulation()
                        .withDisplayToFile(DISPLAY_FILE)
                        .withPoissonLambda(3.25)
                        .withTrafficDelayRange(2..10)

        LOG.info("Beginning Kitchen Simulation…")
        system.begin()
    }

}
