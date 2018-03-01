implicit val offset = 4.0
implicit val offset2 = 2.0

def adjust(x: Double)(implicit offset: Double) = 
  x + offset

adjust(1.0)(offset)

