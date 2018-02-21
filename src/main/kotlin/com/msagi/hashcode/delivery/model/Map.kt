package com.msagi.hashcode.delivery.model

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sqrt

open class Location(var row: Int, var column: Int) {
    //common UID for all child classes so distance can be calculated and stored regardless of the child class type
    val locationId: Int = Companion.idCounter++

    companion object {
        var idCounter: Int = 0
    }
}

open class Store(val id: Int, row: Int, column: Int, val storeProducts: MutableList<StoreProduct>) : Location(row, column) {

    fun getProduct(product: Product) = storeProducts.firstOrNull { it.product.id == product.id }
            ?: throw IllegalArgumentException("Product#${product.id} not found in ${this}")

    fun reserve(product: Product, quantity: Int) {
        val storeProduct = getProduct(product)

        if (storeProduct.available < quantity)
            throw IllegalArgumentException("$this has ${storeProduct.available} of Product#${product.id} to reserve but $quantity is requested")

        storeProduct.available -= quantity
        storeProduct.reserved += quantity
    }

    fun checkoutReservation(product: Product, quantity: Int) {
        val storeProduct = getProduct(product)

        if (storeProduct.reserved < quantity)
            throw IllegalArgumentException("$this has ${storeProduct.reserved} of Product#${product.id} to check out but $quantity is requested")

        storeProduct.reserved -= quantity
    }

    override fun toString(): String {
        return "${javaClass.simpleName}#$id[$row, $column]"
    }
}

class Map(size: Int) {

    private val distances = Array(size, { IntArray(size, { -1 }) })

    /**
     * Get the distance in between the two location. Uses lazy evaluation and caching.
     */
    fun distance(location1: Location, location2: Location): Int {

        fun distance(ra: Int, ca: Int, rb: Int, cb: Int): Int {
            val r = abs(ra - rb)
            val c = abs(ca - cb)
            return ceil(sqrt((r * r + c * c).toDouble())).toInt()
        }

        var d = distances[location1.locationId][location2.locationId]
        if (d == -1) {
            d = distance(location1.row, location1.column, location2.row, location2.column)
            distances[location1.locationId][location2.locationId] = d
        }

        return d
    }
}