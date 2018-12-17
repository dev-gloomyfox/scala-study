package com.gloomyfox.chapter05

object Main {

  def main(args: Array[String]) = {
    val s = MyStream.apply(1, 1, 3, 5)
    val st = s.takeWhile(x => x == 1)
    println(st.toList)
  }
}
