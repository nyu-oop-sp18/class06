object Sorting extends App {
  
  def mergeSort[A <% Ordered[A]](xs: List[A]): List[A] = {
    // merge the sorted partitions
    def merge(xs: List[A], ys: List[A], zs: List[A]): List[A] = {
      (xs, ys) match {
        case (Nil, _) => zs.reverse ::: ys
        case (_, Nil) => zs.reverse ::: xs
        case (x :: xs1, y :: ys1) =>
          if (x < y) merge(xs1, ys, x :: zs)
          else merge(xs, ys1, y :: zs)
      }
    }
    
    if (xs.length <= 1) xs 
    else {
      // partition xs into ys and zs
      val (ys, zs) = xs.foldLeft(Nil: List[A], Nil: List[A]) { case ((ys, zs), x) => (x :: zs, ys) }
      
      // sort ys and zs recursively and merge
      merge(mergeSort(ys), mergeSort(zs), Nil)
    }
  }
  
  /*
  // The following is already declared in the Scala API
  trait Ordered[A] {
    def compare(that: A): Int
    def <(that: A): Boolean = compare(that) < 0
    def <=(that: A) = compare(that) <= 0
    def >(that: A) = compare(that) > 0
    def >=(that: A) = compare(that) >= 0
  }
  
  implicit def IntToOrdered(v: Int): Ordered[Int] = new Ordered[Int] {
    def compare(that: Int) = v - that
  }
  */
  
  val xs = List(1, 5, -2, 12)

  println(mergeSort(xs))
  
  val ss = List(("orange", 3), ("banana", -1), ("orange", -1))
  
  println(mergeSort(ss))
}