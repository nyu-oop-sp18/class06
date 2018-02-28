import scala.collection.mutable._

class InstrumentedSet[A] extends HashSet[A] {
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





/*
class InstrumentedSet[A](val v: Set[A]) extends AbstractSet[A] {
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
*/