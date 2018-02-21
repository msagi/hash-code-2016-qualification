package com.msagi.hashcode.delivery.model

class Warehouse(id: Int, row: Int, column: Int, stock: MutableList<StoreProduct>) : Store(id, row, column, stock)