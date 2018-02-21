package com.msagi.hashcode.delivery.algorithm

import com.msagi.hashcode.delivery.Model
import com.msagi.hashcode.delivery.model.*
import org.slf4j.LoggerFactory
import kotlin.math.floor

class PayloadItem(val warehouse: Warehouse, val product: Product, val quantity: Int)

class Payload(val order: Order, val items: List<PayloadItem>) {
    override fun toString(): String {
        val builder = StringBuilder("\n$order\nitems:\n")
        var payloadWeight = 0
        items.forEach {
            payloadWeight += it.product.weight * it.quantity
            builder.append("\t${it.warehouse}, ${it.product}, ${it.quantity}\n")
        }
        builder.append("total payload weight: $payloadWeight")
        return builder.toString()
    }
}

/**
 * A greedy evaluation algorithm with maximum loaded drones (hence the name 'greedy heavy lifting').
 *
 * Optimisation steps/approaches:
 *      - sort the orders in an ascending order by their number of order items (the less items the order has
 *          the quicker to complete it)
 *      - for each order, and each order items in the order, build a record contains a full payload for the
 *          drone using the closest stores which has the items on the product item list
 *          (and reserve the capacity in the warehouse and in the order).
 *      - store/cache these payloads in a list hence issuing new commands for drones is a simple
 *          'get the next payload and translate it to drone commands' task
 */
class GreedyHeavyLiftingAlgorithm : Algorithm {

    private val logger = LoggerFactory.getLogger(javaClass)

    private lateinit var model: Model

    private lateinit var payloadCache: MutableList<Payload>

    override fun init(model: Model) {
        logger.trace("Pre-processing model...")
        this.model = model

        payloadCache = mutableListOf()
        model.orders
                .sortedBy { it.getOutstandingItems().size }
                .forEach { order ->
                    var droneLocation: Location = order

                    var remainingDroneCapacity = 0
                    lateinit var payloadItems: MutableList<PayloadItem>
                    lateinit var payload: Payload

                    order.getOutstandingItems().forEach { outstandingItem ->

                        while (outstandingItem.available > 0) {

                            if (remainingDroneCapacity < outstandingItem.product.weight) {
                                remainingDroneCapacity = model.maxDroneLoad
                                payloadItems = mutableListOf()
                                payload = Payload(order, payloadItems)
                                payloadCache.add(payload)
                            }
                            //reserve product items from the warehouses closest to the order
                            val warehouse = model.warehouses
                                    .filter { it.getProduct(outstandingItem.product).available > 0 } //warehouse has available product we want to reserve
                                    .sortedBy { model.map.distance(droneLocation, it) + model.map.distance(it, order) + model.map.distance(droneLocation, order)}
                                    .first()
                            logger.trace("Reservation cache: process: $warehouse (distance: $warehouse -> $order: ${model.map.distance(order, warehouse)})")

                            val warehouseProduct = warehouse.getProduct(outstandingItem.product)

                            val maxQuantityTheDroneCanStillLoad = floor(remainingDroneCapacity.toDouble() / outstandingItem.product.weight).toInt()
                            val reserveQuantity = listOf(outstandingItem.available, warehouseProduct.available, maxQuantityTheDroneCanStillLoad).min()!!
                            outstandingItem.available -= reserveQuantity
                            outstandingItem.reserved += reserveQuantity
                            warehouseProduct.available -= reserveQuantity
                            warehouseProduct.reserved += reserveQuantity

                            remainingDroneCapacity -= reserveQuantity * outstandingItem.product.weight
                            droneLocation = warehouse

                            payloadItems.add(PayloadItem(warehouse, outstandingItem.product, reserveQuantity))

                            logger.debug("Reservation cache: booking: $reserveQuantity of ${outstandingItem.product} reserved in $warehouse")
                        }

                    }
                    logger.debug("$payload")
                }


        //self-check if all the order items are reserved (hence the whole model is processed)
        model.orders
                .filter { order -> !order.getOutstandingItems().isEmpty() }
                .forEach { order ->
                    logger.error("Pre-processing error: $order has outstanding items: ${order.getOutstandingItems()}")
                }

        logger.debug("Pre-processing done.")
    }

    override fun getCommandsFor(drone: Drone): List<Command> {
        logger.trace("Searching for commands for drone $drone...")

        if (payloadCache.isEmpty()) return emptyList()

        val droneLoadCommands = mutableListOf<Command>()
        val droneDeliverCommands = mutableListOf<Command>()

        val payload = payloadCache.removeAt(0)
        val targetOrder = payload.order

        //add load commands with tracking drone location
        payload.items.forEach { payloadItem ->

            logger.trace("$payloadItem (payload) is selected for drone $drone...")

            drone.reserve(payloadItem.product, payloadItem.quantity)

            val loadCommand = LoadCommand(drone, payloadItem.warehouse, StoreProduct(payloadItem.product, payloadItem.quantity, 0))
            droneLoadCommands.add(loadCommand)

            val deliveryCommand = DeliverCommand(drone, targetOrder, StoreProduct(payloadItem.product, payloadItem.quantity, 0))
            droneDeliverCommands.add(deliveryCommand)
        }

        return droneLoadCommands + droneDeliverCommands
    }

}