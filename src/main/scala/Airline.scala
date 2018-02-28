case class Address (
  street: String,
  city: String,
  zipCode: Int)

case class Person (
    name: String,
    address: Address)


// For payroll subsystem
class Agent(name: String, address: Address) extends Person(name, address) {
  // ...
}

// For ticketing subsystem
class Passenger(name: String, address: Address) extends Person(name, address) {
  // ...
}
