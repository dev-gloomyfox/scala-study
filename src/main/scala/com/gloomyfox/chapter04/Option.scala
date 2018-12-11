package com.gloomyfox.chapter04

//sealed trait Option[+A]
case class Some[+A](get: A) extends Option[A]
case object None extends Option[Nothing]

trait Option[+A] {
  // Option이 None이 아니면 f를 적용
  def map[B](f: A => B): Option[B] = this match {
    case None => None
    case Some(a) => Some(f(a))
  }

  def getOrElse[B >: A](default: => B): B  = this match { // B :> A는 B의 형식 매개변수가 반드시 A의 상위 형식이어 함을 명시
    case None => default
    case Some(a) => a
  }

  // Option이 None이 아니면 실패 가능한 f를 적용
  def flatMap[B](f: A => Option[B]): Option[B] = {
    map(f) getOrElse None
  }

  // 첫 Option이 정의되어 있으면 그것을 돌려주고 그렇지 않으면 두 번째 Option 반환
  def orElse[B >: A](ob: => Option[B]): Option[B] = {
    this map(Some(_)) getOrElse ob
  }

  // 값이 f를 만족하지 않으면 Some을 None로 변환
  def filter(f: A => Boolean): Option[A] = {
    flatMap(a => if(f(a)) Some(a) else None)
  }
}
