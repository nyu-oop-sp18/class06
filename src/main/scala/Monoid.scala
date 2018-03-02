import scala.language.implicitConversions
import scala.language.higherKinds

trait Monoid[A] {
  def combine(x: A, y: A): A
  val zero: A
}

object Monoid {
  implicit def productMonoid[A: Monoid, B: Monoid] = new Monoid[(A, B)] {
    import MonoidOps._
  
    override def combine(x: (A, B), y: (A, B)) = (x._1 |+| y._1, x._2 |+| y._2)
    override val zero = (mzero[A], mzero[B])
  }
}

class MonoidOps[A](v: A)(implicit m: Monoid[A]) {
  def |+|(that: A): A = m.combine(v, that)
}

object MonoidOps {
  implicit def toMonoidOps[T: Monoid](v: T): MonoidOps[T] = new MonoidOps[T](v)
  def mzero[T](implicit m: Monoid[T]): T = m.zero
}


object Concat {
  import MonoidOps._
  
  def mapConcat[A: Monoid, B, C[T] <: TraversableOnce[T]](as: C[B])(f: B => A): A = 
    as.foldLeft(mzero[A])(_ |+| f(_))
  
  def concat[A: Monoid, C[T] <: TraversableOnce[T]](as: C[A]): A = 
    as.foldLeft(mzero[A])(_ |+| _)
}


object MonoidTest extends App {
 
  implicit val intMonoid = new Monoid[Int] {
    override def combine(x: Int, y: Int): Int = x + y
    override val zero: Int = 0
  }
  
  val l = List(1, 2, 3)
  
  // Calculate sum of the values in l
  println(Concat.concat(l))
  
  // Calculate average of the values in l
  val (len, total) = Concat.mapConcat(l)((1, _))
  
  println(s"Average: ${total.toDouble / len}")
  
}