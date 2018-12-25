import com.gloomyfox.chapter05.MyStream

def square(x: Double): Double = x * x

//square(sys.error("failure"))

true || sys.error("failure")

List(1, 2, 3, 4).map(_ + 10).filter(_ % 2 == 0).map(_ * 3)

def if2[A](cond: Boolean, onTrue: () => A, onFalse: () => A): A = {
  if(cond) onTrue() else onFalse()
}

val a = 10

if2(a < 22,
  () => println("a"),
  () => println("b"))

def if3[A](cond: Boolean, onTrue: => A, onFalse: => A): A = {
  if(cond) onTrue else onFalse
}

if3(false, sys.error("fail"), 3)

def maybeTwice(b: Boolean, i: => Int) = if(b) i + i else 0
val x = maybeTwice(true, { println("hi"); 1 + 41 })

def maybeTwice2(b: Boolean, i: => Int) = {
  lazy val j = i
  if(b) j + j else 0
}
val x2 = maybeTwice2(true, { println("hi"); 1 + 41 })