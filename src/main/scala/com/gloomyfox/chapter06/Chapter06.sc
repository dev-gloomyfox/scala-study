val rng = new scala.util.Random

rng.nextDouble
rng.nextDouble
rng.nextInt
rng.nextInt(10)

def rollDie: Int = {
  val rng = new scala.util.Random
  rng.nextInt(6) // 0 ~ 5의 난수 반환
}

def rollDie2(rng: scala.util.Random): Int = rng.nextInt(6)

import com.gloomyfox.chapter06.RNG
import com.gloomyfox.chapter06.RNG.Rand

val rng2 = RNG.SimpleRNG(42)
val (n1, rng3) = rng2.nextInt
val (n2, rng4) = rng3.nextInt

RNG.nonNegativeInt(RNG.SimpleRNG(42))

RNG.double(RNG.SimpleRNG(42))

RNG.intDouble(RNG.SimpleRNG(42))

RNG.doubleInt(RNG.SimpleRNG(42))

RNG.double3(RNG.SimpleRNG(42))

RNG.ints(3)(RNG.SimpleRNG(42))

//val ns: Rand[List[Int]] =
//  int.flatMap(x =>
//    int.flatMap(y =>
//      ints(x).map(xs =>
//        xs.map(_ % y))))

//val ns: Rand[List[Int]] = for {
//  x <- int
//  y <- int
//  xs <- ints(x)
//} yield xs.map(_ % y)