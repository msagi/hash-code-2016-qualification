package com.msagi.hashcode.delivery

import com.msagi.hashcode.delivery.algorithm.Algorithm
import com.msagi.hashcode.delivery.algorithm.ChopChopDumbAlgorithm
import com.msagi.hashcode.delivery.model.Command
import com.msagi.hashcode.delivery.model.Order
import org.slf4j.LoggerFactory
import kotlin.math.ceil

class Simulation(private val model: Model, private val algorithm: Algorithm) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private var totalScore = 0
    private var time = 0
    private val commandHistory = mutableListOf<Command>()

    fun run() {
        logger.debug("Simulation started (deadline: ${model.deadline}, algorithm: ${algorithm.javaClass.simpleName})")

        while (time < model.deadline && !model.orders.isEmpty()) {
            logger.debug("time = $time")
            model.drones.forEach { drone ->
                drone.advanceTime()
                if (drone.isIdle()) {
                    algorithm.getCommandsFor(drone).forEach {
                        logger.debug("New command: drone: $drone, command: $it")
                        drone.enqueueCommand(it)
                        commandHistory.add(it)
                    }

                } else {
                    logger.trace("$drone is busy.")
                }
            }
            model.orders.filter { order -> order.isCompleted() }.forEach { order ->
                orderCompleted(order)
            }
            time++
        }

        if (time == model.deadline) {
            val builder = StringBuilder("\nSimulation aborted: deadline, incomplete orders: ${model.orders.size}\n")
            model.orders.filter { !it.isCompleted() }.forEach { order ->
                order.getOutstandingItems().forEach {
                    builder.append("Undelivered: $order / ${it.product}, available: ${it.available}, reserved: ${it.reserved}\n")
                }
            }
            logger.error(builder.toString())
        }

        logger.debug("Simulation complete (total score: $totalScore)")

        logger.info("\n${toSolutionString()}")
    }

    private fun orderCompleted(order: Order) {
        model.orders.remove(order)
        val score = ceil(((model.deadline - time) * 100).toDouble() / model.deadline).toInt()
        totalScore += score
        logger.debug("Order#${order.id} completed, score: $score, totalScore: $totalScore")
    }

    private fun toSolutionString(): String {
        val builder = StringBuilder("${commandHistory.size}\n")
        commandHistory.forEach {
            builder.append(it.toCommandString()).append("\n")
        }
        return builder.toString()
    }
}

fun main(args: Array<String>) {
//    val model = Model("assets/mother_of_all_warehouses.in")
//    val model = Model("assets/example.in")
//    val model = Model("assets/busy_day.in")
    val model = Model("assets/redundancy.in")
    val algorithm = ChopChopDumbAlgorithm()
//    val algorithm = GreedyAlgorithm()
    algorithm.init(model)

    val simulation = Simulation(model, algorithm)
    simulation.run()
}