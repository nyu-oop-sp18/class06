import org.scalactic.Tolerance._
import org.scalatest.FlatSpec
import scala.collection.mutable.HashSet

class InstrumentedSetSpec extends FlatSpec {
  
  "InstrumentedSet" should "count the number of insertion attempts" in {

    val s = new InstrumentedSet[Int]
    
    s += 1
    s += 3
    s += 2
    
    assert(s.getCount == 3)
    
    val s2 = new InstrumentedSet[Int]
    
    s2 ++= List(1, 2)
    
    assert(s2.getCount == 2)
  }
  
}
