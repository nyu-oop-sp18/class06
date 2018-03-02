# Class 6

## Class Inheritance vs. Composition

When should one use class inheritance and when not? What are OO design
alternatives to class inheritance?

The advantages and disadvantages of class inheritance can be
summarized as follows:

**Advantages:**

* Subtyping, which yields polymorphic code

* New implementation is easy, since most of it is inherited

* Easy to modify or extend the implementation being reused

**Disadvantages:**

* Breaks encapsulation, since it exposes a subclass to implementation
  details of its superclass

  * "White-box" reuse, since internal details of superclasses are
    often visible to subclasses

  * Tight coupling between subclass and superclass: subclasses may
    have to be changed if the implementation of the superclass changes.

  * Implementations inherited from superclasses can not be changed at
    run-time.

* Complicated inheritance hierarchies can lead to code which is difficult
  to read and maintain.

  * Undisciplined, poorly applied subtype polymorphism leads to a
    large gap between reading the static code and what actually
    happens at run-time.
    
  * Bugs become very difficult to find (and sometimes
    recreate). Mental gymnastics required to debug.

### Example: Breaking Encapsulation Using Class Inheritance

The following example illustrates how class inheritance can break
encapsulation.

Suppose we have a Scala program that uses the `HashSet` data structure
from the `scala.collection.mutable` package and we want to monitor the number of
attempted element insertions into the data structure. One potential
solution is to extend the `HashSet` class as follows:

```scala
class InstrumentedSet[A] extends HashSet[A] {
  // counts the number of attempted insertions
  private var count: Int = 0
  
  def getCount: Int = count
  
  override def +=(x: A) = {
    count += 1
    super.+=(x)
  }
  
  override def ++=(xs: TraversableOnce[A]) = {
    count += xs.size
    super.++=(xs)
  }
}
```

The following test code reveals the problem with this solution:

```scala
val s = new InstrumentedSet
  
s ++= List("apple", "orange", "banana")
  
assert(s.getCount == 3)
```

The assertion fails because `getCount` returns `6` instead of the
expected result `3`. The problem is that the `++=` method of the
class `HashSet` is implemented by calling the `+=` method for all
elements of the traversable collection `xs`. Since `+=` is overriden by the
subclass, every inserted element is counted twice. We could solve this
problem by removing the increment of `count` in the overriding
`++=` method. However, the new solution would remain susceptible to
the introduction of bugs whenever the implementation of `HashSet`
changes. That is, the solution with subclass inheritance breaks
encapsulation.

A more viable solution is to wrap a `HashSet` object inside a new object
that delegates all work to the `HashSet` and only counts the number of
attempted insertions. This approach is called *delegation by
composition*. In fact, by using composition we can even make the wrapper
class independent of the concrete implementation of the set data
structure:

```scala
class InstrumentedSet[A](private val v: Set[A]) extends AbstractSet[A] {
  private var count: Int = 0
  
  def getCount: Int = count
  
  override def +(x: A) = {
    count += 1
    v + x
  }

  override def +=(x: A) = {
    count += 1
    v += x
    this 
  }
  
  override def -=(x: A) = { v -= x; this }
  
  override def iterator: Iterator[A] = v.iterator

  override def contains(elem: A): Boolean = v.contains(elem)
}
```

Note how we use the trait `Set` and the abstract class `AbstractSet`
from package `scala.collection.mutable` to achieve polymorphism
without making `InstrumentedSet` depend on any concrete implementation
of the set data structure.

### Example: Implementation of Classes Cannot be Changed at Run Time

Suppose we want to model the business logic of an airline. The
software consists of different components. One component handles
transactions with passengers such as ticket reservation and
purchase. The other component handles payroll. The ticketing component
involves passengers and agents, while the payroll component involves
only agents. The ticketing component needs to keep track of the name
and address of each customer. The payroll component needs to maintain
the same information for each agent.

A design for the two components might involve the following class hierarchy:

* A class `Person` is used to store the name and address of each person.

* There are two classes, `Passenger` and `Agent`, to model agents and
  passengers, respectively. Both of these classes are subclasses of
  `Person`.

What is wrong with this design?

* The roles of a person may change over time. A person may be a
  passenger at one point in time and an agent at another point in
  time, or even both at the same time. This is not reflected in the
  static class hierarchy.

One viable solution is to use subclassing and composition together:

* Introduce a new class `PersonRole` that has a reference to a `Person`.
* Define `Passenger` and `Agent` as subclasses of `PersonRole`.


### Example: Beware of Pitfalls - False Hierarchies

The relationships between objects in the problem domain do not always
carry over to an OO design.

For example, suppose we want to model geometric objects such as
rectangles, squares, circles, etc. The following class implements
rectangles:

```scala
class Rectangle(protected var height: Double, protected var width: Double) {

  def getWidth: Double = width
  def getHeight: Double = height

  def setWidth(w: Double): Unit = width = w
  def setHeight(h: Double): Unit = height = h

  def area: Double width * height
}
```

Since a square is a special kind of a rectangle, we might want to
implement squares as a subclass of `Rectangle`:

```java
class Square(l: Double) extends Rectangle(l, l) {

  override def setHeight(h: Double): Unit = {
    height = h
    width = h
  }

  override def setWidth(w: Double): Unit = {
    height = w
    width = w
  }
}
```

Note that we override the `setHeight` and `setWidth` to maintain the
invariant that the height and width of a square coincide.
 
What is the problem with this solution?

* Each square object stores redundant information because we don't
  need to keep track of both height and width for squares. That is, we
  waste 8 bytes of memory per allocated square object. However, this
  might be negligible.

* More importantly, a `Square` object does not behave like a
  `Rectangle` object because the methods `setHeight` and `setWidth` of
  class `Square` modify both height and width. Client code that uses
  the `Rectangle` class might assume that only one of the two
  attributes is modified when the corresponding mutator method is
  called, as the names of the methods suggest. Hence, we cannot safely
  use a `Square` object when a `Rectangle` object is expected, even
  though the subclass relationship suggests otherwise. This is a
  violation of the substitution principle.

It is worth noting that the second issue could be avoided by following
a functional design where the `setHeight` and `setWidth` methods
return a new `Rectangle` instance rather than modifying the state of
the `Square`.

### Advantages/Disadvantages of Composition

**Advantages:**

* Contained objects are accessed by the containing class solely
  through their interfaces

* "Black-box" reuse, since internal details of contained objects are
  not visible

* Good encapsulation

* Fewer implementation dependencies

* Each class is focused on just one task
  
* The composition can be defined dynamically at run-time through
  objects acquiring references to other objects. This is used e.g. for
  *dependency injection*.

**Disadvantages:**

* Resulting systems tend to have more objects. Though the JVM is good
  at dealing with short-lived objects.

* Interfaces must be carefully defined in order to use many different
  objects as composition blocks. This is where you can get into
  trouble. Dedicated design patterns can help.

* Potential "boiler plate" code for forwarding methods that delegate
  work to the contained objects. This can be mitigated by making good
  use of abstract classes that provide partial implementations of the
  desired functionality (as in our use of `AbstractSet` to implement
  `InstrumentedSet`). Also, the programming language may provide
  features that simplify delegation (e.g., Scala implicits - see
  below).

## Scala Implicits

Consider the following straightforward implementation of a merge sort
algorithm for sorting lists of `Int` values.

```scala
def mergeSort(xs: List[Int]): List[Int] = {
  // merge the sorted partitions
  def merge(xs: List[Int], ys: List[Int], zs: List[Int]): List[Int] = {
    (xs, ys) match {
      case (Nil, _) => zs.reverse ::: ys
      case (_, Nil) => zs.reverse ::: xs
      case (x :: xs1, y :: ys1) =>
        if (x < y) merge(xs1, ys, x :: zs)
        else merge(xs, ys1, y :: zs)
    }
  }
    
  if (xs.length <= 1) xs else {
    // partition xs into ys and zs
    val (ys, zs) = xs.foldLeft(Nil: List[Int], Nil: List[Int]) { case ((ys, zs), x) => (x :: zs, ys) }
      
    // sort ys and zs recursively and merge
    merge(mergeSort(ys), mergeSort(zs), Nil)
  }
}
```

The algorithm partitions the list `xs` into two sublists `ys` and
`zs` if `xs` contains at least two elements. The two partitions are
then sorted recursively and subsequently merged using the
tail-recursive `merge` function.

While the implementation has been written specifically for lists of
`Int` values, there is nothing particular about the algorithm that
restricts it to `Int`. Ideally, we would like to generalize
`mergeSort` to lists over any type `A` that has a total order defined
on its values.

The only place where the ordering is used in `mergeSort` is the
condition `x < y` in the third match case of `merge`. Recall that this
notation is just syntactic sugar for a method call `x.<(y)`. If we want to
generalize the algorithm to work over some generic type  `A`, we must
ensure that there is an appropriate method

```scala
def <(that: A): Boolean
```

defined on `A`. We can use an upper bound on `A` to express this. To
this end, let us defined a generic trait `Ordered[A]` that captures
types `A` whose values have an ordering defined on them:

```scala
trait Ordered[A] {
  def compare(that: A): Int
  
  def <(that: A): Boolean = compare(A) < 0
  
  def <=(that: A): Boolean = compare(A) <= 0
  
  def >(that: A): Boolean = compare(A) > 0
  
  def >=(that: A): Boolean = compare(A) >= 0
}
```

Note that `Ordered[A]` is already predefined in the Scala API.

To implement `Ordered[A]` for any particular type `A`, we just need to
implement the abstract method `compare`, which must satisfy the
following conditions:

* the return value is negative if `this` is strictly smaller than `that`

* the return value is positive if `this` is strictly greater than `that`, and

* the return value is `0` if `this` is equal to `that`.

We can now generalize `mergeSort` by replacing `Int` with a generic
type parameter `A` such that `A` is a subtype of `Ordered[A]`:

```scala
def mergeSort[A <: Ordered[A]](xs: List[A]): List[A] = {
  ...
}
```

The upper bound constraint, `A <: Ordered[A]`, ensures that the
condition `x < y` is well typed.

However, if we now try to sort a list of `Int` values like this

```scala
val xs = List(1, 5, -2, 12)

mergeSort(xs)
```

Then the compiler will report a type error:

```scala
error: inferred type arguments [Int] do not conform to method mergeSort's type parameter bounds [T <: Ordered[T]]
mergeSort(xs)
^
```

The problem is that `Int` is a predefined type that does not extend
`Ordered[A]`. So the upper bound constraint on the type parameter `A`
of `mergeSort` is not satisfied by `Int`.

One common solution to such type incompatibilities is to write a conversion
function that takes an `Int` and converts it to an `Ordered[Int]` by
wrapping it in an object that defines the `compare` method for `Int`
values appropriately. Here is one implementation of such a conversion
function:

```scala
def IntToOrdered(v: Int): Ordered[Int] = new Ordered[Int] {
  override def compare(that: Int) = v - that
}
```

In addition, we can generalize `mergeSort` a bit further. Rather than
requiring that `A` itself extends `Ordered[A]`, we can require that
the caller provides an appropriate conversion function `f: A =>
Ordered[A]` as additional argument to `mergeSort`. We can then replace
the condition `x < y` in `merge` by `f(x) < y`. That is, we convert
the receiver of the call to `<` from `A` to `Ordered[A]` to make sure
that the expression is well typed. The following code snippet
highlights the required changes to our implementation:

```scala
def mergeSort[A](xs: List[A])(f: A => Ordered[A]): List[A] = {
  ...
        if (f(x) < y) ...
  ...
  merge(mergeSort(ys)(f), mergeSort(zs)(f), Nil)
}
```

Using our conversion function for `Int` values declared earlier, we
can now sort lists of integers as expected:

```scala
scala> val xs = List(1, 5, -2, 12)
xs: List[Int] = List(1,5,-2,12)
scala> mergeSort(xs)(IntToOrdered)
res1: List[Int] = List(-2,1,5,12)
```

While this solution is quite flexible, it is also a bit cumbersome to
use because we have to provide the conversion function explicitly in
each call and apply the function manually at the points where it is
needed within the implementation of `mergeSort`. If we have to convert
more than one type, then the resulting code can become quite cluttered
and hard to read.

As this style of generic programming is quite common, Scala provides a
mechanism that allows us to hide the extra complexity arising from
these type conversions.

### Implicit Parameters

A recurring problem in programming is that when you design the client
interface of a library you have to satisfy conflicting demands. On one
hand, you want to make the interface as flexible as possible, allowing
clients to modify the behavior of the library without changing its
implementation. On the other hand, the interface should be as simple
as possible and not have thousands of "*knobs*" that clients have to
set each time they use the library.

Scala solves this problem with so-called *implicit parameters* to
methods. As an example, suppose we are implementing code for a
geometry application that provides a method `adjust`, which adjusts a
coordinate `c` of a point by a given offset `o`:

```scala
def adjust(c: Double)(o: Double) = c + o

val offset = 1.0

adjust(3.0)(offset)
res1: Double = 4.0
```

Suppose we write code that makes many calls to `adjust` within the same
scope using the same offset value. Ideally, we would like to use
`adjust` in a way that does not require us to provide the offset
explicitly in each call in that scope, while still retaining the
flexibility of using `adjust` with other offset values in other parts
of our program. 

To solve this kind of problem, Scala allows us to declare the final
parameter list of a method as implicit parameters:

```scala
def adjust(c: Double)(implicit o: Double) = c + o
```

In addition, whenever we declare a value or method in our program, we
can make that declaration implicit. For instance, in our example, we
could declare the fixed offset value that we want to provide to
`adjust` as an implicit value:

```scala
implicit val offset = 1.0
```

In the scope where the implicit value has been declared, we can now
call `adjust` without providing the argument to its implicit `offset`
parameter explicitly:

```scala
adjust(3.0)
res1: Double = 4.0
```

In general, whenever the implicit parameter list of a method is
omitted in a call to the method, the compiler will search for implicit
values in the scope of the call whose static types match the static
types of the implicit parameters. 

In our example, we call `adjust(3.0)` and the compiler thus searches
for an implicit value in the scope of the call whose type matches the
type `Double` of the implicit parameter `o`. The compiler finds the
implicit value `offset` and automatically expands the call to
`adjust(3.0)(offset)`.

If no compatible implicit value is found or if there is more than one
implicit value in scope that matches the type of the implicit
parameter, then the compiler will report an error. To resolve such
issues, we can always resort to providing the arguments to the
implicit parameters explicitly as if they were regular parameters.

Note that the resolution of the implicit arguments is done purely
based on the static types of the parameters. For instance, if we
expanded our definition of `adjust` so that it takes two implicit
values of type `Double`:

```scala
def adjust(c: Double)(implicit o1: Double, o2: Double) = c + o1 + o2
```

then a call `adjust(1.0)` in the context of the implicitly declared
`val offset` will always expand to `adjust(1.0)(offset, offset)`. That is,
implicit parameters of the same type will always be resolved to the
same implicit argument value in any given scope.

Note that the resolution of implicit arguments happens at compile
time. That is, the compiler statically determines which value to
provide to each implicit parameter at a call site where the implicit
parameters are omitted. It guarantees that the implicit arguments
exist and are unique in the scope of the call. 

### Implicit Type Conversions

Implicit parameters are particularly useful for dealing with type
conversions, as in our example of the `mergeSort` function. We can
declare the parameter `f: A => Ordered[A]` as an implicit parameter:

```scala
def mergeSort[A](xs: List[A])(implicit f: A => Ordered[A]): List[A] = {
  ...
        if (x < y) ...
  ...
  merge(mergeSort(ys), mergeSort(zs), Nil)
}
```

Note that we made two additional changes in the code:

1. We no longer need to pass the conversion function `f` to the
   recursive calls to `mergeSort` explicitly. Since the extra
   parameter is implicit, the compiler will search for an implicit
   value in the current scope to complete the call. Since `f` is
   implicit in the scope of the body of `mergeSort` and its type is
   compatible with the type of the missing parameter for the recursive
   call, the compiler automatically expands the call `mergeSort(ys)`
   to `mergeSort(ys)(f)` and similarly for `mergeSort(zs)`.
   
1. We omit the explicit call to `f` in the condition `f(x) < y` and
   instead simply write `x < y`.

The second point is important. Implicit values that are functions from
one type to another `f: A => B` are treated as implicit type
conversion functions by the compiler. That is, whenever the compiler
finds an expression of static type `A` (or one of `A`'s subtypes) in
the scope of such an implicit conversion function, and the context of
that expression expects a value of static type `B` (or one of `B`'s
supertypes), and `A` is not a subtype of `B`, then the compiler
automatically inserts a call to the conversion function `f` to
"*bridge the gap*".

In our example, in `x < y` the expression `x` is of type `A`. However,
the context expects an expression of type `Ordered[A]` since we call
the method `<` on `x` and `Ordered[A]` provides such a method. `A` and
`Ordered[A]` may not be related by subtyping. Hence, without a
conversion between the types we would have a static type
error. However, since the implicit conversion function `f: A =>
Ordered[A]` is in scope, the compiler automatically inserts the call
to `f` yielding the expression `f(x) < y` as in our original code. The
resulting code is then well typed.

There are three important rules to remember when using implicit type
conversions:

* Only conversion functions that are marked as `implicit` are used for
  implicit conversions.

* The compiler only considers implicit conversion functions that are
  in scope directly as a *single identifier* such as the function `f`
  in our example. That is, conversion functions that are methods of
  objects referenced by an identifier in scope are not considered. For
  instance, if we have an identifier `x` referencing an object with an
  implicit conversion function `f`, then `x.f` will not be considered
  as a candidate conversion in the current scope. The only exception
  to this are the companion objects of the source and target type of
  the required conversion. For instance, if the compiler searches for
  an implicit conversion function of type `A => B` and the companion
  object of `A` has a method of this type, then this method will be
  used for the conversion.
  
* The compiler uses at most one implicit conversion function at a
  time. For instance, if there are implicit conversion functions `f1:
  A => C` and `f2: C => B` in scope, the compiler will not insert a
  chain of calls to `f1` followed by `f2` to bridge the gap between
  types `A` and `B`.

Using our new implementation of `mergeSort`, we can now sort `Int`
values without providing the conversion function explicitly in the
call. All that we need to do is declare the conversion function once
as an implicit value so that it is in the scope of the `mergeSort` call.

```scala
implicit def IntToOrdered(v: Int): Ordered[Int] = new Ordered[Int] {
  override def compare(that: Int) = v - that
}

mergeSort(List(1,5,-2,7)
res1: List[Int] = List(-2,1,5,7)
```

If we use the predefined `Ordered[A]` trait from the Scala API in our
implementation of `mergeSort`, then we can even omit the declaration
of the implicit conversion function. This is possible because the
Scala API also defines appropriate conversions from the primitive
value types such as `Int`, `Double`, etc. to their ordered wrapper
types `Ordered[Int]`, `Ordered[Double]`.

Note that we can also define generic implicit conversion functions
that lift given implicit conversion functions for generic type
parameters to more complex types constructed from those type
parameters. For instance, we can declare an implicit conversion
function that converts pairs `(A,B)` over ordered types `A` and `B` to
ordered pairs:

```scala
implicit def toOrderedPair[A, B](implicit fa: A => Ordered[A], fb: B => Ordered[B]) =
  new Ordered[(A, B)] {
    override def compare(x: (A, B), y: (A, B)) = {
      val c1 = x._1.compare(y._1) < 0
      if (c1 != 0) c1 else y._1.compare(y._2)
    }
  }
```

We can then use `mergeSort`, e.g., to sort pairs of `Int` and `String`
values lexicographically:

```
mergeSort(List((3, "banana"), (1, "orange"), (1, "apple")))
res1: List[(Int, String)] = List((1, "apple"), (1, "orange"), (3, "banana"))
```

Again, for the type `Ordered[A]` many such generic conversion
functions for the common type constructors are already predefined in
the Scala API.

### View Bounds

In our implementation of `mergeSort` we declare the implicit
parameter `f: A => Ordered[A]` for the type conversion, but we never
actually use `f` explicitly in the body of `mergeSort`:

```scala
def mergeSort[A](xs: List[A])(implicit f: A => Ordered[A]): List[A] = {
  ...
        if (x < y) ...
  ...
  merge(mergeSort(ys), mergeSort(zs), Nil)
}
```

The compiler automatically inserts `f` whenever it is needed (the
conversion of `x` in the comparison and the recursive calls). For
these cases where an implicit conversion function is declared as a
parameter but never used explicitly in the body of a method or class,
Scala provides a more compact syntax that allows us to declare the
implicit conversion function by expressing a constraint on the generic
type parameter `A`:

```scala
def mergeSort[A <% Ordered[A]](xs: List[A]): List[A] = {
  ...
        if (x < y) ...
  ...
  merge(mergeSort(ys), mergeSort(zs), Nil)
}
```

The notation `A <% Ordered[A]` is called a *view bound*. It specifies
that, in the caller's context, there must exist an implicit conversion
function `A => Ordered[A]`. That is, values of type `A` *can be
viewed* as if they were values of type `Ordered[A]`. This is similar
to the upper bound constraint `A <: Ordered[A]` in our original
implementation. However, an upper bound expresses the stronger
constraint that `A` is a subtype of `Ordered[A]`, which means that `A`
values can be used directly as values of type `Ordered[A]` without the
need for a call to a conversion function (which e.g. wraps the `A`
value inside an `Ordered[A]` object).

The view bound `A <% Ordered[A]` is just syntactic sugar for the
declaration of an implicit parameter of type `A => Ordered[A]` that
provides the conversion function.

Implicit conversions are a powerful mechanism for enriching existing
types with additional methods without modifying the implementation of
those types. This is particularly useful for types that are provided
by third party libraries whose implementation cannot be modified,
allowing users to extend the library with additional functionality
that seamlessly integrates with the library's existing API. This
design pattern is therefore also referred to as the "*Pimp my Library
Pattern*".

If one carefully combines implicit conversion with operator
overloading, then this technique also enables the embedding of
domain-specific languages directly in the Scala language. For
instance, suppose you are writing a library for operations of sparse
matrices over a field `A`. Think about how could you use implicit
conversions to implement the multiplication of a matrix `m` by a
scalar value `lambda` of type `A` so that you could just write this as
`lambda * m` in your Scala program.

## Type Classes

We have seen how to we can write more flexible generic code by
wrapping objects of generic types `A` in other objects `C[A]` that
provide additional functionality on the values of type `A`. By
combining this approach with implicit type conversions `A => C[A]`,
the boiler-plate code for wrapping `A` objects in `C[A]` objects can
be hidden, giving us the illusion that we directly operate on the `A`
objects.

Rather than wrapping `A` objects in other objects that implement the
new functionality, an alternative approach is to bundle the additional
operations on `A` in a separate class and then pass an instance of
that class to the generic code that makes use of the additional
operations.

For instance, the Scala API provides the trait `Ordering[T]` which has
the following signature:

```scala
trait Ordering[A] {
  def compare(x: A, y: A): Int
  def lt(x: A, y: A): Boolean = compare(x, y) < 0
  def lteq(x: A, y: A): Boolean = compare(x, y) <= 0
  def gt(x: A, y: A): Boolean = compare(x, y) > 0
  def gteq(x: A, y: A): Boolean = compare(x, y) >= 0
  ...
}
```

This trait is similar to `Ordered[A]`. However, unlike `Ordered[A]`
which can be thought of as an `A` object extended with methods related
to the ordering on type `A`, an instance of `Ordered[A]` is simply a
container for the ordering-related operations but not a container for an
`A` instance itself. Hence, all the operations on `Ordered[A]` take
two parameters of type `A` instead of just one.

We refer to such a container class for related operations on a type as
a *type class*.

An alternative implementation of `mergeSort` using the `Ordering[A]`
type class would look as follows:

```scala
def mergeSort[A](xs: List[A])(implicit o: Ordering[A]): List[A] = {
  ...
        if (o.lt(x, y)) ...
  ...
  merge(mergeSort(ys), mergeSort(zs), Nil)
}
```

Note that this implementation does not need to wrap `x` into an
`Ordered[A]` to implement the comparison `x < y`. However, this comes
at the expense of slightly more clunky syntax `o.lt(x, y)`.

In certain cases, using type classes such as `Ordering[A]` is more
convenient than using wrapper classes such as `Ordered[A]`. However,
often the two approaches can be fruitfully combined. We explore this
using the example of *monoids*.

### Monoids

Formally, a monoid over a type `A` is a semigroup with a neutral
element. That is, `A` is equipped with a binary operation `|+|: (A,A)
=> A` and there is an element `zero: A` such that the following two
conditions hold:

1. `|+|` is associative, i.e. for all `a,b,c: A`: `(a |+| b) |+| c == a
    |+| (b |+| c)`
    
1. `zero` is the left and right neutral for `|+|`, i.e. for all `a:
   A`: `zero |+| a == a |+| zero == a`

Examples of monoids abound:

* Integers `Int` with addition `+` and neutral element `0`

* Integers `Int` with multiplication `*` and neutral element `1`

* Strings `String` with string concatenation `+` and neutral element `""`

* Lists `List[T]` with list concatenation `:::` and neutral element
  `Nil`
  
* Booleans `Boolean` with disjunction `||` and neutral element `false`

* Booleans `Boolean` with conjunction `&&` and neutral element `true`

* ...

Even though monoids are very simple structures, they play an important
role in computing. If we are given a sequence of values of the monoids
type `A`:

```a1, a2, a3, ..., an```

and we want to reduce these values to a single value by combining them
with the monoid operation `|+|`:

```a1 |+| a2 |+| a3 |+| ... |+| an```

then we can exploit the fact that `|+|` is associative. Namely,
associativity allows us to split the work into chunks of subsequences
of the input values that we can combine in parallel and then
recursively combine the intermediate results:

```
   a1   a2   a3     ...     an
    \   /     \   /     \   /
     |+|       |+|  ...  |+|
      \__    __/         /
         \  /           /
         |+    ...     /
          \____   ____/
               \ /
               |+|
```

This observation is the corner stone of the *map/reduce* framework for
efficient parallel processing of Big Data.

Suppose we want to implement a library that provides functionality for
automatically parallelizing such monoid-based reduce operations over
collections of data. We can follow the type class pattern to express
in Scala's type system that this library depends on the fact that it
is only used on collections of data that have a monoid defined on
them.

Thus, let us start with a generic type class for monoids over some
type `A`:

```scala
trait Monoid[A] {
  def combine(x: A, y: A): A
  val zero: A
}
```

Here the `combine` method stands for the associative `|+|` operation
of the monoid and `zero` is the neutral element.

Using our monoid type class `Monoid[A]`, we can now define some simple
methods that make use of the monoid operations:

```scala
object Concat {
  def concat[A](as: List[A])(implicit m: Monoid[A]): A = 
    as.foldLeft(m.zero)(m.combine(_, _))

  def mapConcat[A, B](as: List[B])(f: B => A)(implicit m: Monoid[A]): A = 
    as.foldLeft(m.zero)(m.combine(_, f(_)))
}
```

The method `concat` takes a list of `A` objects and reduces them to a
single `A` value using the associative monoid operation. We simply use
the `foldLeft` method of `List` to traverse the list and combine the
values, so there is no actual parallelization here. Though, this is
not the point of this exercise. We will see later how this computation
could be parallelized without much additional effort. Note that we
pass the monoid type class object as an implicit parameter `m`.

The method `mapConcat` is similar to `concat`. It can be used in cases
where the input list is over some type `B` that itself may not have a
monoid defined on it, but there is a conversion function `f: B => A`
to a type `A` which does have a monoid structure.

As an example, let us define an implicit monoid object `intMonoid`
that represents the monoid formed by addition on integers:

```scala
implicit val intMonoid = new Monoid[Int] {
  override def combine(x: Int, y: Int): Int = x + y
  override val zero: Int = 0
}
```

With this implicit `Monoid[Int]` value in scope, we can now compute
the sum of the elements in a list of integer values by using the
`Concat.concat[Int]` operation:

```scala
val l = List(1, 2, 3)
Concat.concat(l)
res1: Int = 6
```

Note that the implicit monoid object `intMonoid` is provided
automatically to the `concat` call by the compiler.

So far so good. Let us now see whether we can further improve our
implementation of the `Concat` object. First, note that the
implementation of the `concat` method does not really rely on the fact
that the argument `as` is a `List[A]`, and likewise for
`mapConcat`. In principle, any collection object that provides a
`foldLeft` method would do. 

The class hierarchy of Scala's collection API provides the trait
`TraversableOnce[A]` which describe objects that contain collections
of `A` objects and that can be traversed at least once. This trait
declares the `foldLeft` method. All of the concrete collection data
structures including `List[A]` are subtypes of `TraversableOnce[A]`.

Thus, we can make our implementation more generic by parameterizing
over the type of the traversable data structure whose elements we
concatenate:

```scala
object Concat {
  def concat[A: Monoid, C[T] <: TraversableOnce[T]](as: C[A]): A = 
    as.foldLeft(m.zero)(m.combine(_, _))

  def mapConcat[A, B, C[T] <: TraversableOnce[T]](as: C[B])(f: B => A)(implicit m: Monoid[A]): A = 
    as.foldLeft(m.zero)(m.combine(_, f(_)))
}
```

Here, the constraint `C[T] <: TraversableOnce[T]` expresses that
`concat` and `mapConcat` parameterize over a type constructor `C` that
is parametric in some type parameter `T` such that `C[T]` is a subtype
of `TraversableOnce[T]`. That is, the type constructor `C` is the
generic collection type and the type parameter `T` is the element type
of that collection type. This is an example of a so-called
*higher-kinded* generic method. We will look at higher-kinded types in
more detail later.

With this generalization, we can now also use the concatenation
methods to concatenate collections other than lists, including
sequences `Seq`, sets `Set`, maps `Maps`, etc.

Next, one annoyance in the implementation of the two concatenation
methods is that we have to refer to the type class object `m` to
access the monoid operations such as in `m.combine(_, _)`. Wouldn't it
be nice if we could write this expression using infix notation such as
`_ |+| _` where `|+|` now stands for the `combine` operation of the
monoid?

We can do this by using the type class `Monoid[A]` in combination with
wrapper objects that enrich the type `A` with the `|+|` infix
operator. Let's call the class for these wrapper objects
`MonoidOps`. That is, `MonoidOps` relates to `Monoid` in the
same way that `Ordered` relates to `Ordering` in our `mergeSort`
example.

Here is the implementation of `MonoidOps`:

```scala
class MonoidOps[A](v: A)(implicit m: Monoid[A]) {
  def |+|(other: A): A = m.combine(v, other)
}
```

We also add a companion object for `MonoidOps` that declares an
implicit conversion function `toMonoidOps` to convert objects of
type `A` to type `MonoidOps[A]`, provided there exists an implicit
`Monoid[A]` in scope:

```scala
object MonoidOps {
  implicit def toMonoidOps[A](v: A)(implicit m: Monoid[A]): MonoidOps[A] = new MonoidOps[A](v)
  def mzero[A](implicit m: Monoid[A]): A = m.zero
}
```

In addition, the method `MonoidOps.mzero[A]` gives us a handle on the
zero element of `Monoid[A]` while keeping the monoid type class object
implicit.

With these declarations in place, we can now further simplify our
implementation of `Concat`:

```scala
object Concat {
  import MonoidOps._

  def concat[A, C[T] <: TraversableOnce[T]](as: C[A])(implicit m: Monoid[A]): A = 
    as.foldLeft(mzero)(_ |+| _)

  def mapConcat[A, B, C[T] <: TraversableOnce[T]](as: C[B])(f: B => A)(implicit m: Monoid[A]): A = 
    as.foldLeft(mzero)(_ |+| f(_))
}
```

Here is what this code will be automatically expanded to by the compiler:

```scala
object Concat {
  import MonoidOps._

  def concat[A, C[T] <: TraversableOnce[T]](as: C[A])(implicit m: Monoid[A]): A = 
    as.foldLeft(mzero[A](m))((x, y) => toMonoidOps[A](x)(m).|+|(y)(m))

  def mapConcat[A, B, C[T] <: TraversableOnce[T]](as: C[B])(f: B => A)(implicit m: Monoid[A]): A = 
    as.foldLeft(mzero[A](m))((x, y) => toMonoidOps[A](x)(m).|+|(f(y))(m))
}
```

### Context Bounds

If we look again at the code for object `Concat` we observe that the
implicit value `m` passed to the functions `concat` and `mapConcat`
are never used explicitly in the body of these functions:

```scala
object Concat {
  import MonoidOps._

  def concat[A, C[T] <: TraversableOnce[T]](as: C[A])(implicit m: Monoid[A]): A = 
    as.foldLeft(mzero)(_ |+| _)

  def mapConcat[A, B, C[T] <: TraversableOnce[T]](as: C[B])(f: B => A)(implicit m: Monoid[A]): A = 
    as.foldLeft(mzero)(_ |+| f(_))
}
```

We are in a similar situation as in the example of `mergeSort` earlier
where we had an implicit conversion function that was never used
explicitly. In the earlier case, we used a view bound to specify the
requirement about the existence of the implicit conversion function in
the caller's scope. In this case, we can't use a view bound, but Scala
provides a similar short-hand notation to express the existence of
implicit type class objects such as `m: Monoid[A]` for a given type
`A`. This notation is referred to as a *context bound*. A context
bound takes the form `A: C` and expresses that for the generic type
parameter `A` there exists an implicit object of type `C[A]` in the
caller's scope and this object will be passed as implicit argument to
the generic method (or class) that is parameterized by `A`.

Here is how we can use a context bound to simplify our implementation
of `Concat`:

```scala
object Concat {
  import MonoidOps._

  def concat[A: Monoid, C[T] <: TraversableOnce[T]](as: C[A]): A = 
    as.foldLeft(mzero)(_ |+| _)

  def mapConcat[A: Monoid, B, C[T] <: TraversableOnce[T]](as: C[B])(f: B => A): A = 
    as.foldLeft(mzero)(_ |+| f(_))
}
```

Just like a view bound, a context bound is simply a syntactic
short-hand for declaring an implicit parameter of a particular type,
here a parameter of type `Monoid[A]`.

### Generic Implicit Type Class Construction

Similar to how we defined generic implicit conversion functions for
the `Ordered` trait, we can define generic methods that construct
monoids for more complex types from implicit monoids of simpler
types. For example, if we have two monoids `Monoid[A]` and `Monoid[B]`
then we can build the monoid product `Monoid[(A, B)]` on pairs of
values `(A, B)` by lifting all operations component-wise.

A good place for such implicit factory methods is the companion object
of the `Monoid` trait. In general, when the compiler searches for
implicit values of some type `T` it will automatically import
the declarations in the companion object of `T` in the scope of the
search.

Here is the implementation of our implicit monoid product factory method:

```scala
object Monoid {
  implicit def product[A: Monoid, B: Monoid] = new Monoid[(A, B)] {
    import MonoidOps._
  
    override def combine(x: (A, B), y: (A, B)) = (x._1 |+| y._1, x._2 |+| y._2)
    override val zero = (mzero[A], mzero[B])
  }
}
```

We can now use this implementation e.g. to calculate the average value
of all values stored in a list. To calculate the average value, we
need to compute two things: the number of values in the list (i.e. its
length), and the total sum of all those values. Using our implicit
monoid product, we can calculate these two values simultaneously and
quite elegantly with a single pass over the list using the method
`Concat.mapConcat`:

```scala
val l = List(1, 2, 3)

val (len, total) = Concat.mapConcat(l)((1, _))

val average = total.toDouble / len
```

It is instructive to manually expand the call `Concat.mapConcat(l)((1, _))`
they way the compiler will do it automatically for us:

```scala
Concat.mapConcat[(Int,Int), Int, List](l)((x: Int) => (1,x))(Monoid.product[Int, Int](intMonoid, intMonoid))
```

This demonstrates the true power of implicit parameters in simplifying
our code in the cases where we need to compose operations on simple
objects to operations on complex objects in order to make use of
simple but powerful generic library functions.
