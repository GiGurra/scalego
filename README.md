# Scalego

[![Build Status](https://travis-ci.org/GiGurra/scalego.svg?branch=master)](https://travis-ci.org/GiGurra/scalego)

An Entity Component System implementation in Scala

Suppose you could have ..

* The efficiency and composability of ECS 
* The API of OOP
* Without macros or reflection

Try Scalego!

```scala

import se.gigurra.scalego.core._

type StringIds = IdTypes {
  type SystemId = String
  type EntityId = String
}

case class Position(x: Int, y: Int)
case class Velocity(x: Int, y: Int)

implicit val positionSystem = new System[Position, StringIds]("position", mutable.HashMap())
implicit val velocitySystem = new System[Velocity, StringIds]("velocity", mutable.HashMap())

val ecs = ECS(positionSystem, velocitySystem)

val e1 = Entity.Builder + Position(1, 2) + Velocity(3, 4) build(entityId = "1")
val e2 = Entity.Builder + Position(5, 6) + Velocity(7, 8) build(entityId = "2")

ecs.system[Position].size shouldBe 2
ecs.system[Velocity].size shouldBe 2
ecs.containsEntity("1") shouldBe true
ecs.containsEntity("2") shouldBe true

e1[Position] shouldBe Position(1,2)
e1[Velocity] shouldBe Velocity(3,4)
e2[Position] shouldBe Position(5,6)
e2[Velocity] shouldBe Velocity(7,8)


```


## Extensions

Have a look at 

* scalego-serialization : A plug-in serialization API for scalego ECS 
* scalego-serialization-json : A first use/implementantation of scalego-serialization

All extensions are built separately on top of scalego-core without intrusive code. 

Here is some example code using scalego-serialization-json:

```scala

val serializer = JsonSerializer[StringIds]()
import serializer._ // Adds a .toJson method to the ECS class

val ugly = ecs.toJson(pretty = false)
val pretty = ecs.toJson(pretty = true)

ugly shouldBe "{\"systems\":[{\"systemId\":\"position\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":5,\"y\":6}},{\"id\":\"1\",\"data\":{\"x\":1,\"y\":2}}]},{\"systemId\":\"velocity\",\"components\":[{\"id\":\"2\",\"data\":{\"x\":7,\"y\":8}},{\"id\":\"1\",\"data\":{\"x\":3,\"y\":4}}]}]}"

```
