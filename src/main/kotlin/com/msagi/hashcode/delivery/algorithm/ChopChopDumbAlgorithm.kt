package com.msagi.hashcode.delivery.algorithm

import com.msagi.hashcode.delivery.Model
import com.msagi.hashcode.delivery.model.*

/**
 * A dumb algorithm which has no optimisation at all (hence the name 'chop chop').
 * It takes the first unallocated product item and looks for the first warehouse which has it then
 * commands the drone to pick it up and deliver it to the customer. If the first warehouse does
 * not have enough stock then it goes to the next one and picks up more items until the
 * product item quantity is satisfied or the drone cannot load more.
 */
class ChopChopDumbAlgorithm : Algorithm {

    private lateinit var model: Model

    override fun init(model: Model) {
        //this is a dumb algorithm without any pre-processing for the model.
        this.model = model
    }

    override fun getCommandsFor(drone: Drone): List<Command> {

        val droneCommands = mutableListOf<Command>()

        val outstandingOrder = model.orders.firstOrNull { !it.isCompleted() && !it.getOutstandingItems().isEmpty() } ?: return emptyList()
        val outstandingProduct = outstandingOrder.getOutstandingItems().firstOrNull() ?: return emptyList()

        var quantityToDeliver = 0
        val product = outstandingProduct.product

        run loadProductFromWarehouses@ {
            model.warehouses.forEach warehouseLoop@ { warehouse ->
                val warehouseProduct = warehouse.getProduct(product)
                //this warehouse does not have the product we want to load
                if (warehouseProduct.available == 0) return@warehouseLoop

                val loadQuantity = listOf(
                        outstandingProduct.available,
                        warehouseProduct.available,
                        drone.getFreeCapacityAsQuantityFor(product)
                ).min()!!

                if (loadQuantity == 0) {
                    return@loadProductFromWarehouses
                }

                warehouse.reserve(product, loadQuantity)
                outstandingOrder.reserve(product, loadQuantity)
                drone.reserve(product, loadQuantity)

                quantityToDeliver += loadQuantity

                val command = LoadCommand(drone, warehouse, StoreProduct(product, loadQuantity, 0))
                droneCommands.add(command)

                //no more item is to be reserved for delivery for given outstanding product
                if (outstandingProduct.available == 0) {
                    return@loadProductFromWarehouses
                }
            }
        }

        val command = DeliverCommand(drone, outstandingOrder, StoreProduct(product, quantityToDeliver, 0))
        droneCommands.add(command)

        return droneCommands
    }

}