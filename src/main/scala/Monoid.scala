import scala.language.implicitConversions
import scala.language.higherKinds

trait Monoid[T] {
  def combine(x: T, y: T): T
  val zero: T
}

object Monoid {
  implicit def productMonoid[A: Monoid, B: Monoid] = new Monoid[(A, B)] {
    import MonoidOps._
  
    override def combine(x: (A, B), y: (A, B)) = (x._1 |+| y._1, x._2 |+| y._2)
    override val zero = (mzero[A], mzero[B])
  }
}

class MonoidOps[T](v: T)(implicit m: Monoid[T]) {
  def |+|(other: T): T = m.combine(v, other)
}

object MonoidOps {
  implicit def toMonoidOps[T: Monoid](v: T): MonoidOps[T] = new MonoidOps[T](v)
  def mzero[T](implicit m: Monoid[T]): T = m.zero
}


object Concat {
  import MonoidOps._
  
  def mapConcat[A: Monoid, B, F[B] <: TraversableOnce[B]](as: F[B])(f: B => A): A = 
    as.foldLeft(mzero[A])(_ |+| f(_))
  
  def concat[A: Monoid, F[A] <: TraversableOnce[A]](as: F[A]): A = 
    as.foldLeft(mzero[A])(_ |+| _)
}


object MonoidTest extends App {
 
  implicit val intMonoid = new Monoid[Int] {
    override def combine(x: Int, y: Int): Int = x + y
    override val zero: Int = 0
  }
  
  val l = List(1, 2, 3)
  
  println(Concat.concat(l))
  
  // Calculate average of the values in a List[Int]
  val (len, total) = Concat.mapConcat(l)((1, _))
  
  println(s"Average: ${total.toDouble / len}")
  
}