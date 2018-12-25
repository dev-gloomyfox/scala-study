def failingFn(i: Int): Int = {
  val y: Int = throw new Exception("fail")
  try {
    val x = 42 + 5
    x + y
  }
  catch { case e: Exception => 43 }
}

failingFn(12)

def failingFn2(i: Int): Int = {
  try {
    val x = 42 + 5
    x + ((throw new Exception("fail!")): Int)
  }
  catch { case e: Exception => 43}
}

failingFn2(12)

def mean(xs: Seq[Double]): Double = {
  if(xs.isEmpty) {
    throw new ArithmeticException("mean of empty list!")
  } else {
    xs.sum / xs.length
  }
}

def mean_1(xs: IndexedSeq[Double], onEmpty: Double): Double = {
  if(xs.isEmpty) onEmpty
  else xs.sum / xs.length
}

//lookupByName("Joe").map(_.department).getOrElse("Default Dept.")

//val dept: String =
//  lookupByName("Joe").
//  map(_.dept).
//  filter(_ != "Accounting").
//  getOrElse("Default Dept")

//def lift[A, B](f: A => B): Option[A] => Option[B] = _ map f

//def abs0: Option[Double] => Option[Double] = lift(math.abs)