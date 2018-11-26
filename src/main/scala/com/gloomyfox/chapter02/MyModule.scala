package com.gloomyfox.chapter02

/**
  * 몇 가지 세부적인 사항을 제외하고는 스칼라의 모든 값은 객체(Object)
  *
  * import 문을 이용하여 객체 이름 생략 가능
  * import MyModule.abs
  * abs(-42)
  *
  * 객체의 모든 nonprivate 멤버를 범위에 import
  * import MyModule._
  */
object MyModule {
  // 순수 함수
  def abs(n: Int): Int = {
    if (n < 0) -n
    else n
  }

  // 다른 함수를 인수로 받는 함수: 고차 함수(Higher-order function, HOF)
  def factorial(n: Int): Int = {
    // loop용 보조함수에는 go나 loop을 붙이는 것이 관례
    // 어떤 블록 내에서도 함수를 정의 가능
    // self-recusion을 검출해서, 재귀 호출이 tail position에서 일어나면
    // 반복문 처럼 동작하게 컴파일, 재귀의 문제인 StackOverflow를 방지
    // @annotation.tailrec을 통해 꼬리 호출에 대한 컴파일 시간에서의 검사가 가능
    @annotation.tailrec
    def go(n: Int, acc: Int): Int =
      if (n <= 0) acc
      else go(n-1, n*acc)

    go(n, 1)
  }

  // 순수 함수
  private def formatAbs(x: Int) = {
    val msg = "The absolute value of %d is %d."
    msg.format(x, abs(x))
  }

  private def formatFactorial(n: Int) = {
    val msg = "The factorial of %d is %d."
    msg.format(n, factorial(n))
  }

  // format~이 하는 일이 거의 동일하니 통합
  private def formatResult(name: String, n: Int, f: Int => Int) = {
    val msg = "The %s of %d is %d."
    msg.format(name, n, f(n))
  }

  // 절차, 불순 함수: Side effect 존재
  def main(args: Array[String]): Unit = {
    println(formatAbs(-42)) // Side effect
    println(factorial(7))

    println(formatResult("absolute value", -42, abs))
    println(formatResult("factorial", 7, factorial))
  }
}
