object Sorting extends App {
  
  def mergeSort[T](xs: List[T])(f: T => Ordered[T]): List[T] = {
    // merge the sorted partitions
    def merge(xs: List[T], ys: List[T], zs: List[T]): List[T] = {
      (xs, ys) match {
        case (Nil, _) => zs.reverse ::: ys
        case (_, Nil) => zs.reverse ::: xs
        case (x :: xs1, y :: ys1) =>
          if (f(x) < y) merge(xs1, ys, x :: zs)
          else merge(xs, ys1, y :: zs)
      }
    }
    
    if (xs.length <= 1) xs else {
      // partition xs into ys and zs
      val (ys, zs) = xs.foldLeft(Nil: List[T], Nil: List[T]) { case ((ys, zs), x) => (x :: zs, ys) }
      
      // sort ys and zs recursively and merge
      merge(mergeSort(ys)(f), mergeSort(zs)(f), Nil)
    }
  }
  
  val xs = List(1, 5, -2, 12)
  
  def IntToOrdered(v: Int): Ordered[Int] = v - _

  println(mergeSort(xs)(IntToOrdered))
  
  def isSorted(xs: List[Int]): Boolean = {
    xs match {
      case x :: xs1 => xs1.foldLeft((true, x)){ case ((b, x), y) => (b && x <= y, y) }._1
      case Nil => true
    }
  }
  
  println(isSorted(List(1, 2, 3, 4)))
  
  
  
  //println(isSorted(List("banana", "apple").sorted))
  //println(isSorted(List("banana", "apple").sorted))
}