package com.gloomyfox.chapter03

sealed trait List[+A] // 공변과 불변
case object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]

// List Companion Object, 목록 생성과 조작을 위한 함수를 생성
object List {
  def sum(ints: List[Int]): Int = ints match {
    case Nil => 0
    case Cons(x, xs) => x + sum(xs)
  }

  def product(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case Cons(0.0, xs) => 0.0
    case Cons(x, xs) => x * product(xs)
  }

  // List의 첫 요소를 제거하는 함수 tail 구현
  def tail[A](as: List[A]): List[A] = as match {
    case Nil => Nil
    case Cons(_, xs) => xs
  }

  // List의 첫 요소를 다른 값으로 대체
  def setHead[A](a: A, as: List[A]): List[A] = as match {
    case Nil => Nil
    case Cons(_, xs) => Cons(a, xs)
  }

  // 처음 n개의 요소를 제거하는 함수 구현
  def drop[A](l: List[A], n: Int): List[A] = l match {
    case Nil => Nil
    case Cons(_, xs) => drop(xs, n - 1)
  }

  // 주어진 술어와 부합하는 List의 앞 요소들을 제거하는 함수 구현
  def dropWhile[A](l: List[A])(f: A => Boolean): List[A] = l match {
    case Cons(h, t) if f(h) => dropWhile(t)(f)
    case _ => l
  }

  def init[A](l: List[A]): List[A] = l match {
    case Nil => Nil
    case Cons(_, Nil) => Nil
    case Cons(h, t) => Cons(h, init(t))
  }

  def append[A](a1: List[A], a2: List[A]): List[A] = a1 match {
    case Nil => a2
    case Cons(h, t) => Cons(h, append(t, a2))
  }

  def foldRight[A, B](as: List[A], z: B)(f: (A, B) => B): B = as match {
    case Nil => z
    case Cons(x, xs) => f(x, foldRight(xs, z)(f))
  }

  def sum2(ns: List[Int]): Int = {
    foldRight(ns, 0)((x, y) => x + y)
  }

  def product2(ns: List[Double]): Double = {
    foldRight(ns, 1.0)(_ * _)
  }

  // 가변 인수 함수 구문
  def apply[A](as: A*): List[A] = {
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))
  }
}
