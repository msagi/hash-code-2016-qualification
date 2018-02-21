package com.msagi.hashcode.delivery.model

data class Product(val id: Int, val weight: Int) {
    override fun toString(): String {
        return "${javaClass.simpleName}#$id[weight:$weight]"
    }
}

data class StoreProduct(val product: Product, var available: Int, var reserved: Int)