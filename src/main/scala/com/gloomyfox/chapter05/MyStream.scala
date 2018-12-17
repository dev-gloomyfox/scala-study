package com.gloomyfox
package chapter05

//sealed trait MyStream[+A]
case object Empty extends MyStream[Nothing]
case class Cons[+A](h: () => A, t: () => MyStream[A]) extends MyStream[A]

trait MyStream[+A] {

  // foldRight를 이용한 구현
  def headOption: Option[A] = this match {
    case Empty => None
    case Cons(h, t) => Some(h())
  }

  // Stream을 List로 변환하되 평가를 강제하여 REPL로 목록의 요소를 볼 수 있게하는 함수
  def toList: List[A] = {
    @annotation.tailrec
    def loop(s: MyStream[A], l: List[A]): List[A] = s match {
      // scala에서 list에 add, element::List, head에 요소 추가
      case Cons(h, t) => loop(t(), h() :: l)
      case _ => l
    }

    loop(this, List()).reverse
  }

  // 처음 n개의 요소 스트림을 반환
  def take(n: Int): MyStream[A] = this match {
    case Cons(h, t) if n > 1 => MyStream.cons(h(), t().take(n - 1))
    case Cons(h, _) if n == 1 => MyStream.cons(h(), MyStream.empty)
    case _ => MyStream.empty
  }

  // 처음 n개의 요소를 건너뛴 스트림 반환
  def drop(n: Int): MyStream[A] = this match {
    case Cons(_, t) if n > 0 => t().drop(n - 1)
    case _ => this
  }

  // 1. 주어진 술어를 만족하는 선행 요소를 모두 돌려주는 함수, 2. foldRight를 이용한 구현
  def takeWhile(p: A => Boolean): MyStream[A] = this match {
    case Cons(h, t) if p(h()) => MyStream.cons(h(), t().takeWhile(p))
    case _ => MyStream.empty
  }

  def exists(p: A => Boolean): Boolean = this match {
    case Cons(h, t) => p(h()) || t().exists(p)
    case _ => false
  }

  def foldRight[B](z: => B)(f: (A, => B) => B): B = {
    this match {
      case Cons(h, t) => f(h(), t().foldRight(z)(f))
      case _ => z
    }
  }

  @annotation.tailrec
  final def find(f: A => Boolean): Option[A] = this match {
    case Empty => None
    case Cons(h, t) => if (f(h())) Some(h()) else t().find(f)
  }

  // Stream의 모든 요소가 주어진 술어를 만족하는지 점검, 만족하지 않으면 즉시 종료
  def forAll(p: A => Boolean): Boolean = {
    foldRight(true)((a,b) => p(a) && b)
  }

  //
  def startsWith[B](s: Stream[B]): Boolean = ???

  // map, filter, append, flatMap 구현 foldRight 이용, append는 자신의 인수에 대해 엄격 X
}

object MyStream {
  def cons[A](hd: => A, tl: => MyStream[A]): MyStream[A] = {
    lazy val head = hd
    lazy val tail = tl
    Cons(() => head, () => tail)
  }

  def empty[A]: MyStream[A] ={
    Empty
  }

  val ones: MyStream[Int] = MyStream.cons(1, ones)

  // n에서 시작하여 n + 1, n + 2 이어지는 무한 정수 스트림 생성
  def from(n: Int): MyStream[Int] = {
    cons(n, from(n + 1))
  }

  // 스트림 구축 함수 unfold, 초기 상태 하나와 다음 상태 및 다음 값을 산출하는 함수하나 입력
  def unfold[A, S](z: S)(f: S => Option[(A, S)]): MyStream[A] = f(z) match {
    case Some((h,s)) => cons(h, unfold(s)(f))
    case None => empty
  }

  def apply[A](as: A*): MyStream[A] = {
    if(as.isEmpty) empty else cons(as.head, apply(as.tail: _*))
  }
}
