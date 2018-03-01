case class Address (
  street: String,
  city: String,
  zipCode: Int)

case class Person (
    name: String,
    address: Address)

abstract class PersonRole(val person: Person)

// For payroll subsystem
class Agent(p: Person) extends PersonRole(p) {
  // ...
}

// For ticketing subsystem
class Passenger(p: Person) extends PersonRole(p) {
  // ...
}
