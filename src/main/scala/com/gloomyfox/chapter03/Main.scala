package com.gloomyfox.chapter03

object Main {
  def main(args: Array[String]): Unit = {
    println("Chapter 03 Test")

    val x = List(1, 2, 3, 4, 5) match {
      case Cons(x, Cons(2, Cons(4, _))) => x
      case Nil => 42
      case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y
      case Cons(h, t) => h + List.sum(t)
      case _ => 101
    }
    println("x is " + x)
    val a = List.apply(1, 2, 3, 4, 5)

    val tail = List.tail(a)
    println("tail result: " + tail)

    val head = List.setHead(6, a)
    println("setHead result: " + head)
  }
}
