package com.msagi.hashcode.delivery.model

class Order(id: Int, row: Int, column: Int, orderItems: MutableList<StoreProduct>) : Store(id, row, column, orderItems) {
    /**
     * Get the outstanding products (all product which has available quantity)
     */
    fun getOutstandingItems(): List<StoreProduct> = storeProducts.filter { it.available > 0 }

    fun getReservedItems(): List<StoreProduct> = storeProducts.filter { it.reserved > 0 }

    fun isCompleted(): Boolean = storeProducts.none { it.available > 0 || it.reserved > 0 }
}

