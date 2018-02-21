package com.msagi.hashcode.delivery.algorithm

import com.msagi.hashcode.delivery.Model
import com.msagi.hashcode.delivery.model.Command
import com.msagi.hashcode.delivery.model.Drone

interface Algorithm {

    /**
     * Initialize the algorithm. Can be used to pre-process the model before issuing commands.
     * @param model The model to initialize with.
     */
    fun init(model: Model)

    /**
     * Get commands for the given drone.
     * @param drone The drone.
     * @return The list with one or more commands.
     */
    fun getCommandsFor(drone: Drone): List<Command>
}