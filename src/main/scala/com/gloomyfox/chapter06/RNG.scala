package com.gloomyfox.chapter06

import com.gloomyfox.chapter06.RNG.{Rand, SimpleRNG}

trait RNG {
  def nextInt: (Int, RNG)
}


object RNG {

  case class SimpleRNG(seed: Long) extends RNG {
    override def nextInt: (Int, RNG) = {
      val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
      val nextRNG = SimpleRNG(newSeed)
      val n = (newSeed >>> 16).toInt
      (n, nextRNG)
    }
  }

  type Rand[+A] = RNG => (A, RNG)

  val int: Rand[Int] = _.nextInt

  // double을 map을 이용하여 좀 더 우아한 방식으로 구현
  val doubleM: Rand[Double] = {
    map(nonNegativeInt)(i => i.toDouble / (Int.MaxValue.toDouble + 1))
  }

  val randIntDouble: Rand[(Int, Double)] = both(int, doubleM)
  val randDoubleInt: Rand[(Double, Int)] = both(doubleM, int)

  def unit[A](a: A): Rand[A] = {
    rng => (a, rng)
  }

  def map[A, B](s: Rand[A])(f: A => B): Rand[B] = {
    rng => {
      val (a, rng2) = s(rng)
      (f(a), rng2)
    }
  }

  // 두 상태 동작 ra, rb와 이들의 결과를 조합하는 함수 f를 받고 두 동작을 조합한 새 동작을 반환
  def map2[A, B, C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] = {
    rng => {
      val (a, rng2) = ra(rng)
      val (b, rng3) = rb(rng2)

      (f(a, b), rng3)
    }
  }

  def flatMap[A, B](f: Rand[A])(g: A => Rand[B]): Rand[B] = {
    rng => {
      val (v, rng2) = f(rng)
      g(v)(rng2)
    }
  }

  def both[A, B](ra: Rand[A], rb: Rand[B]): Rand[(A, B)] = {
    map2(ra, rb)((_, _))
  }

  // RNG.nextInt를 이용하여 0 이상, Int.MaxValue 이하의 난수 정수 생성
  // nextInt가 Int.MinValue를 돌려주는 구석진 경우도 확실하게 처리
  def nonNegativeInt(rng: RNG): (Int, RNG) = {
    val (number, rng2) = rng.nextInt
    ((if(number < 0) -(number + 1) else number), rng2)
  }

  // 0이상 1미만의 Double 난수를 발생하는 함수 작성
  def double(rng: RNG): (Double, RNG) = {
    val (number, rng2) = nonNegativeInt(rng)
    (number.toDouble / (Int.MaxValue.toDouble + 1), rng2)
  }

  // 난수쌍 발생 함수 작성, 앞에서 작성한 함수들을 재사용
  def intDouble(rng: RNG): ((Int, Double), RNG) = {
    val (i, rng2) = rng.nextInt
    val (d, rng3) = double(rng2)
    ((i, d), rng3)
  }

  def doubleInt(rng: RNG): ((Double, Int), RNG) = {
    val ((i, d), rng2) = intDouble(rng)
    ((d, i), rng2)
  }

  def double3(rng: RNG): ((Double, Double, Double), RNG) = {
    val (d1, rng2) = double(rng)
    val (d2, rng3) = double(rng2)
    val (d3, rng4) = double(rng3)

    ((d1, d2, d3), rng4)
  }

  // 정수 난수 목록을 생성하는 함수 작성
  def ints(count: Int)(rng: RNG): (List[Int], RNG) = {
    if(count == 0)
      (List.empty, rng)
    else {
      val (i, rng2) = rng.nextInt
      val (is, rng3) = ints(count - 1)(rng2)
      (i :: is, rng3)
    }
  }

  def nonNegativeEven: Rand[Int] = {
    map(nonNegativeInt)(i => i - 1 % 2)
  }

  // 상태 전이들의 목록 전체를 조합하는 것도 가능, 상태 전이들의 List를 하나의 상태 전이로 조합하는 함수 sequence 구현
  // 이 함수를 이용하여 이전에 작성한 ints 함수를 다시 구현, ints 함수의 구현에서 x가 n번 되풀이되는 목록을 만들 일이 있으면 List.fill(n)(x) 사용
  def sequence[A](fs: List[Rand[A]]): Rand[List[A]] = {
    ???
  }

  def nonNegativeLessThan(n: Int): Rand[Int] = {
    flatMap(nonNegativeInt){
      i => {
        val mod = i % n
        if (i + (n - 1) - mod >= 0) unit(mod) else nonNegativeLessThan(n)
      }
    }
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val zero = rollDie3(SimpleRNG(5))._1
    println(zero)
  }

  def rollDie3: Rand[Int] = RNG.nonNegativeLessThan(6)
}