# Scalego

[![Build Status](https://travis-ci.org/GiGurra/scalego.svg?branch=master)](https://travis-ci.org/GiGurra/scalego)

An Entity Component System implementation in Scala

Goals:

* The efficiency and composability of ECS 
* The API of OOP
* Without macros or reflection

Example use:

```scala

import com.github.gigurra.scalego.core._

// Decide the types you want to use as entity/system Ids
type StringIds = IdTypes {
  type SystemId = String
  type EntityId = String
}

// Define your component types
case class Position(x: Int, y: Int)
case class Velocity(x: Int, y: Int)

// Create your systems - All component data is stored inside these systems. 
// You can pick the backing storage type yourself, as long as you implement scala's mutable.Map trait. 
// In these examples I will use scala's regular mutable.HashMap
implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())

// Create the ECS
val ecs = ECS(positionSystem, velocitySystem)

// Add some entities. When you execute the .build(entityId) method the 
// components get added to the relevant systems. The Entity class actually
// has no other fields than the entityId
val e1 = Entity.Builder + Position(1, 2) + Velocity(0, 0) build(entityId = "1")
val e2 = Entity.Builder + Position(0, 0) build(entityId = "2")

// Or just add components manually
ecs.system[Position].put("2", Position(5, 6)) // From the ECS ..
velocitySystem.put("2", Velocity(7, 8)) // Or on the system ..
e1 += Velocity(3,4) // Or directly on the Entity itself

// Extract the component data from the ECS ..
ecs.system[Position].apply("1") shouldBe Position(1,2)
ecs.system[Velocity].get("2") shouldBe Some(Velocity(7,8))

// From the systems ..
positionSystem("1") shouldBe Position(1, 2)
positionSystem.get("3") shouldBe None

// Or directly on the entities themselves - This is achieved by using the implicit System variables above
e1[Position] shouldBe Position(1,2)
e1[Velocity] shouldBe Velocity(3,4)
e2.get[Position] shouldBe Some(Position(5,6))
e2.get[Velocity] shouldBe Some(Velocity(7,8))

// Systems can be treated as mutable.Map's
// Entities can be thought of as lenses


```


## Extensions

Have a look at 

* scalego-serialization : A plug-in serialization API for scalego ECS 
* scalego-serialization-json : Implementantation of scalego-serialization using json4s

Example code using scalego-serialization-json:

```scala

import com.github.gigurra.scalego.serialization.json.JsonSerializer

val serializer = JsonSerializer[StringIds]()
import serializer._ // Adds a .toJson method to the ECS class

val ugly = ecs.toJson(pretty = false)
val pretty = ecs.toJson(pretty = true)

ugly shouldBe "{\"systems\":[{\"systemId\":\"position\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":5,\"y\":6}},{\"id\":\"1\",\"data\":{\"x\":1,\"y\":2}}]},{\"systemId\":\"velocity\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":7,\"y\":8}},{\"id\":\"1\",\"data\":{\"x\":3,\"y\":4}}]}]}"

// Or the opposite case, perhaps reading a game world from a file or sent over the network.
val ecs = ECS(..,..,..)
ecs.appendJson(json: String)


```


## Try it!

```sbt
libraryDependencies += "com.github.gigurra" %% "scalego" % "0.3.7"
```
