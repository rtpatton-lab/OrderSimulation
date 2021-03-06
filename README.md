********************
This is a take-home submission challenge for a promisign startup. The code uses Kotlin and can be ran
using a JVM.
********************

## Getting Started

### From Code
To get started from code, open the `Main.kt` file and run the main function.

### From Maven
To run this program using Maven, run the following commands at the project root:

```
mvn clean install
mvn exec:java
```

## Technologies Used
1. Kotlin
2. Maven
3. JDK
4. Alchemy Libraries

---------------------------------------------------------------------------------------------------------
## Challenges

### Top Challenges

+ The calculation of an order's value over time proved really tricky, 
  because it depends on how long an order sits in its designated shelf 
  vs the overflow shelf.
  
+ Synchronizing the shelves across multiple threads.
  Between the Kitchen adding orders, Drivers coming to pick
  up orders, and the kitchen reorganizing the shelves, 
  there were many opportunities for race conditions.
  
+ Overall this was a really challenging problem. 
  There are many complexities to this system that one would just not expect.
  
  
### Handling Overflow Items
Handling overflow items is handled by the `ShelfSet` object,
which is an abstraction that I wrote over the four main shelves. 
The kitchen adds orders to it, and the drivers pick up orders from it.

Doing it this way allows me to handle item overflow in an encapsulated manner, 
without troubling other parts of the system. 

The `ShelfSet` moves orders to the overflow shelf if the current shelf is full
and there is nowhere else to go. And whenever an order is picked-up by a driver,
it checks to see if there are any items in the overflow that can take its place.

This made the `ShelfSet` the only point of contention across multiple threads.
In order for this to work, the `ShelfSet` had to be made Thread-Safe. 

---------------------------------------------------------------------------------------------------------

## Areas for improvement 

The result code is far from perfect. Here are some areas I have 
identified for improvement.

+ **Dependency Injection**: Dependencies between different parts of the system could have been better handled by the use
of a DI framework such as Guice or Spring.

+ **Real-World Dispatcher**: I followed the guide, and so the Dispatcher dispatches a driver per request.
In the real world it won't be this simple. Sometimes you only have a small pool of
drivers doing deliveries. In fact there are a lot of complexities on the 
delivery side that were just glossed over for the purpose of this examination. 
One can imagine for instance, not just traffic, but the distance the driver
must go to deliver an order. There is also a decay that happens when 
an order sits with a delivery person.

* doesn't have REST API right now *

* Software *
IntelliJ Ultimate
Oracle JDK
Sublime text editor

* Hardware *
2020 Macbook with M1 chip
