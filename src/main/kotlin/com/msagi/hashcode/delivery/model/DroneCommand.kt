package com.msagi.hashcode.delivery.model

enum class CommandTag(val tag: String) {
    LOAD("L"),
    DELIVER("D")
}

open class Command(val drone: Drone, val commandTag: CommandTag, val location: Location) {
    open fun toCommandString(): String = TODO("Not implemented")
}

class LoadCommand(drone: Drone, val warehouse: Warehouse, val stock: StoreProduct) : Command(drone, CommandTag.LOAD, warehouse) {

    override fun toCommandString(): String {
        return "${drone.id} ${commandTag.tag} ${warehouse.id} ${stock.product.id} ${stock.available}"
    }

    override fun toString(): String {
        return "$drone: load ${stock.available}x ${stock.product} at $warehouse"
    }
}

class DeliverCommand(drone: Drone, val order: Order, val stock: StoreProduct) : Command(drone, CommandTag.DELIVER, order) {

    override fun toCommandString(): String {
        return "${drone.id} ${commandTag.tag} ${order.id} ${stock.product.id} ${stock.available}"
    }

    override fun toString(): String {
        return "$drone: fly to $order and deliver ${stock.available}x ${stock.product}"
    }
}
