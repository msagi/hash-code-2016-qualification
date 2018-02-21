package com.msagi.hashcode.delivery

import com.msagi.hashcode.delivery.model.*
import com.msagi.hashcode.delivery.model.Map
import java.io.File

class Model(path: String) {
    val deadline: Int
    val maxDroneLoad: Int
    val drones: List<Drone>
    val warehouses: List<Warehouse>
    val orders: MutableList<Order>
    val map: Map

    init {
        val lines = File(path).readLines()
        var lineIndex = 0

        fun nextLineAsInt(): Int = lines[lineIndex++].toInt()

        fun nextLineAsIntList(): MutableList<Int> = lines[lineIndex++].split(' ').map { it.toInt() }.toMutableList()

        val simulationParameters = nextLineAsIntList()
        //val rows = simulationParameters[0]
        //val columns = simulationParameters[1]
        val numberOfDrones = simulationParameters[2]
        deadline = simulationParameters[3]
        maxDroneLoad = simulationParameters[4]

        val productTypes = nextLineAsInt()
        val productWeights = nextLineAsIntList()
        val products = List(size = productTypes, init = { index -> Product(index, productWeights[index]) })

        val numberOfWarehouses = nextLineAsInt()
        warehouses = List(
                size = numberOfWarehouses,
                init = { warehouseId ->
                    val coordinates = nextLineAsIntList()
                    val r = coordinates[0]
                    val c = coordinates[1]
                    val numberOfProductsAvailable = nextLineAsIntList()
                    val warehouseProductStocks = mutableListOf<StoreProduct>()
                    numberOfProductsAvailable.forEachIndexed { productId, quantity ->
                        warehouseProductStocks.add(StoreProduct(products[productId], quantity, 0))
                    }
                    Warehouse(warehouseId, r, c, warehouseProductStocks)
                }
        )

        val numberOfCustomerOrders = nextLineAsInt()
        orders = MutableList(
                size = numberOfCustomerOrders,
                init = { orderId ->
                    val coordinates = nextLineAsIntList()
                    val r = coordinates[0]
                    val c = coordinates[1]
                    nextLineAsInt() //number of ordered products

                    //translate list of product types to list of products with quantities
                    //e.g. 2 2 3 3 0 -> product(#0, 1), product(#1, 0), product(#2, 2), product(#3, 2)
                    val orderProductTypes = nextLineAsIntList()
                    val orderItemQuantities = List(
                            size = products.size,
                            init = { index ->
                                StoreProduct(products[index], 0, 0)
                            }
                    )
                    orderProductTypes.forEach { productId ->
                        orderItemQuantities[productId].available++
                    }

                    val orderItems = orderItemQuantities.filter { it.available > 0 }.toMutableList()
                    Order(orderId, r, c, orderItems)
                }
        )

        val totalNumberOfLocationObjects = Location.Companion.idCounter
        map = Map(totalNumberOfLocationObjects)

        drones = List(
                size = numberOfDrones,
                init = { droneId ->
                    Drone(droneId, warehouses[0], map, maxDroneLoad)
                }
        )

    }

}
