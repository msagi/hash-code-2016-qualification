package com.msagi.hashcode.delivery.model

import kotlin.math.floor


class Drone(val id: Int, private var location: Location, private val map: Map, private val capacity: Int) {

    private val payload: MutableList<StoreProduct> = mutableListOf()
    private var totalWeight = 0

    private val commandQueue = mutableListOf<Command>()
    private var timeToCompleteActiveCommand = -1

    fun enqueueCommand(command: Command) {
        if (command.drone != this) throw IllegalArgumentException("Command is not for this drone")
        commandQueue.add(command)
        println("${this}: new command added: $command")
    }

    fun advanceTime() {
        if (timeToCompleteActiveCommand == -1) {
            if (commandQueue.isEmpty()) return

            val activeCommand = commandQueue.first()
            timeToCompleteActiveCommand = 1 + map.distance(location, activeCommand.location)
            println("${this}: new command active: $activeCommand, time to complete: $timeToCompleteActiveCommand")
        }

        timeToCompleteActiveCommand--

        if (timeToCompleteActiveCommand == 0) {
            val command = commandQueue.removeAt(0)
            when (command) {
                is LoadCommand -> {
                    val warehouse = command.warehouse
                    location = warehouse
                    val orderItem = command.stock
                    warehouse.checkoutReservation(orderItem.product, orderItem.available)
                    load(orderItem.product, orderItem.available)

                    println("$this: ${orderItem.product}, quantity: ${orderItem.available}, weight: ${orderItem.product.weight} loaded at $warehouse. " +
                            "Total load weight: $totalWeight of $capacity")

                }
                is DeliverCommand -> {
                    val order = command.order
                    location = order
                    val orderItem = command.stock
                    order.checkoutReservation(orderItem.product, orderItem.available)
                    unload(orderItem.product, orderItem.available)

                    println("$this: ${orderItem.product}, quantity: ${orderItem.available}, weight: ${orderItem.product.weight} unloaded at $order. " +
                            "Total load weight: $totalWeight of $capacity")

                }
            }
            timeToCompleteActiveCommand = -1
        }

    }

    fun isIdle() = commandQueue.isEmpty()

    fun reserve(product: Product, quantity: Int) {
        val loadWeight = quantity * product.weight
        if (totalWeight + loadWeight > capacity) throw DroneException("${this}: cannot reserve: has free capacity of ${capacity - totalWeight} but $loadWeight is requested\n${toDebugString()}")

        val p: StoreProduct? = payload.firstOrNull { it.product.id == product.id }
        when (p) {
            null -> payload.add(StoreProduct(product, 0, quantity))
            else -> p.reserved += quantity
        }

        totalWeight += loadWeight
    }

    private fun load(product: Product, quantity: Int) {
        val p: StoreProduct = payload.firstOrNull { it.product.id == product.id }
            ?: throw DroneException("${this}: cannot load: has no $product reserved\n${toDebugString()}")

        if (p.reserved < quantity) throw DroneException("${this}: cannot load: has quantity of ${p.reserved} $product reserved but $quantity is requested\n${toDebugString()}")

        p.available += quantity
        p.reserved -= quantity
    }

    fun getFreeCapacityAsQuantityFor(product: Product) = floor((capacity - totalWeight).toDouble() / product.weight).toInt()

    private fun unload(product: Product, quantity: Int) {
        val p: StoreProduct = payload.firstOrNull { it.product.id == product.id }
                ?: throw DroneException("${this}: Cannot deliver Product#${product.id}: zero quantity loaded")
        if (p.available < quantity) {
            throw DroneException("${this}: Cannot deliver Product#${product.id}: ${p.available} loaded but $quantity requested\n${toDebugString()}")
        }
        val loadWeight = quantity * product.weight
        totalWeight -= loadWeight

        p.available -= quantity
        if (p.available == 0 && p.reserved == 0) {
            payload.remove(p)
        }
    }

    override fun toString(): String {
        return "Drone#$id"
    }

    private fun toDebugString(): String {
        val builder = StringBuilder(toString())
                .append("\n")
                .append("capacity: $capacity, free capacity: ${capacity - totalWeight}\n")
        payload.forEach {
            builder.append(" ${it.product}, available: ${it.available} (weight: ${it.product.weight * it.available}), reserved: ${it.reserved} (weight: ${it.product.weight * it.reserved})\n")
        }
        return builder.toString()
    }
}

class DroneException(message: String) : Throwable(message)