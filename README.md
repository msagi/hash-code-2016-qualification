## Google Hash Code 2016 qualification challenge solution

This is an example solution for the Google Hash Code 2016 qualification round challenge written in pure Kotlin for educational purposes.

**Competitive coding HOWTO**

1. Do NOT immediately start with coding but fully understand the problem (ask all the questions you have, it will save lots of time during coding)
2. Look for a generic problem matching the challenge by removing the 'story' from the challenge (e.g. supplier/consumer, Vehicle Routing, graph search, etc).
 Try to think about what data structure would fit this problem best (this can lead to the generic problem, e.g. the data structure is a graph and the generic
 problem is a custom tweaked DFS or BFS)
3. Google this problem to find generic solutions or approaches, learn about pros, cons, tricks and tips
4. Break down the complexity to multiple, smaller, less complex steps (but keep the overall design flexible in case you would have 'oops' effect and decide to change 
  or tweak your approach on the way). Think about a simple and brute force solution (it does not have to scale at this point)
5. Draw an architecture of the solution (components, objects, relations)
6. Implement the model objects, the input parsing and output serializing logic (e.g. how to load and parse the input file to domain model; how to create a solution file from the domain model with the solution)
7. Come up with test data (or download the given input files if provided)
8. Implement your 'hello world' approach (the simplest, dumbest solution comes to your mind - don't try to come up with the super optimal solution for the first because it can easily happen that
 you will be unable to finish its coding on time and you end up with 0 score.)
9. Test your implementation and submit your result (so you have a result now, well done :) )
10. Start and continue optimising your solution and re-submit it so your score will increase

**How I applied these steps to the 2016 qualification challenge**

1. I read the problem statement from top to bottom 10x, understanding the big picture first then paying attention to the little details.
2. This problem is a spiced up version of the [Vehicle Routing Problem](https://en.wikipedia.org/wiki/Vehicle_routing_problem). The generic problem behind it is
 "What is the optimal set of routes for a fleet of vehicles to traverse in order to deliver to a given set of customers?".
3. The optimal solution to this problem is NP-hard, therefore the generic approach is to use heuristics (brute force would take too long). There are lots of algorithms 
 (heuristics) tries to cut the calculation times but they are not necessarily provide the optimal solution (e.g. a specific algorithm gives good solution to input A but 
 less than optimal to input B). There are multiple open source 3rd party libraries
 (solvers) addressing this issue (e.g. [JSprit](https://jsprit.github.io/) or [OptaPlanner](https://docs.optaplanner.org) but I chose not to use them, so
    1. I can learn more by solving this issue on my own
    1. In order to quickly and effectively use a 3rd party library, you have to be familiar with it already, learning it during the competition is very risky (sometimes they don't address
   your problem directly and tweaking them can take long time).
4. My first approach to solve the challenge was:
    1. Simplest ('multiple drone' dummy force):
```
    for all the order o1/o2 combinations
        for all the items in the order
        for all the warehouses
            if there is an idle drone (drone#i)
            drone #i pick up the item and deliver to the order location
```
5. The first (raw) model / interface
```
    Solution
        - void addCommand(Command)
        - int getScore()
```
```
    Drone object
        Location location
        Boolean idle
        - void load(productTypeId, quantity) throws OverCapacityException
        - void deliver(productTypeId, Order)
```
```
    Warehouse
        Location location
        - int availableQuantity(Product)
        - void loadItemToDrone(Drone, Product, quantity)
```
```
    Order
        Location location
        - List<Product> products()
```
```
    Map
        - int distance(Drone, Warehouse)
        - int distance(Drone, Order)
        - int distance(Order, Warehouse)
        - int distance(Warehouse, Warehouse)
```
6. Memory allocation is to be closely looked at during implementation. Some competitions limit your run time or memory which is luckily not the case for this challenge.
    1. Order memory allocation.
        1. 10k order (max) and 10k products (max) -> cost is 10k * 10k * 4B = 400MB (max)
    1. Warehouse memory allocation
        1. onStock: 10k warehouse (max) and 10k products (max) -> cost is 400MB (max)
        1. bookedStock: 10k warehouse (max) and 10k products (max) -> cost 400MB (max)
    1. The distances needed to be calculated in between warehouse, order and drone
        1. to avoid multiple calculation of the same, the Map object caches the already calculated values
        1. there are 10k warehouses (max) and 10k orders (max) in order to cache each distance to each others it costs 400MB (max)
    1. how to represent the products loaded to the drone
        1. there are 1k drones (max) and 10k product types (max) in order to easily use product types on drones it costs (1k*10k*4 bytes) = 40MB (max)

7. I got example.in, busy_day.in, mother_of_all_warehouses.in, redundancy.in (see the assets folder for these files) 

8. "oops" effects (they happens all the time)
    * the simulation logic is missing, in order to simulate the delivery we need a framework for that so we can see if an algorithm is better or worse than the other.
  The simulation functionality should do: advance time, check if we have free drones and allocate job, check if all the orders are competed, etc.
```
            Simulation
                round - this is the time
                model - this is the model the simulation runs on
                algorithm - this is the algorithm the simulation runs with

                print result (list of commands and score)
```
* the warehouse and the order should have a 'reserved stock' function which is 'reserved for delivery' so we can avoid drones 'racing' for the same stock
    * warehouse: the reserved stock is for a drone which is on the way to pick it up
    * order: the reserved stock is which is on the way to delivery

9. I implemented two algorithm, the 'Dumb' and the 'Greedy'. The dumb does not have any optimisation at all just iterates through the orders. 
 The greedy has greedy heuristics trying to optimise the model before processing the orders. Here are the statistics for the two algorithms:
    
| Algorithm | Input | Score | Comment |
| --- | --- |    
| Dumb | Busy day in | 55335 | baseline |
| Greedy | Busy day in | 98361 | +77% |
| Dumb | Mother of all Warehouses | 59141 | baseline |
| Greedy | Mother of all Warehouses | 73826 | +25% |
| Dumb | Redundancy | 76234 | baseline |
| Greedy | Redundancy | 94635 | +24% |

**I hope it helps you to learn the best practices to follow on a coding competition such as Google Hash Code. Enjoy!**