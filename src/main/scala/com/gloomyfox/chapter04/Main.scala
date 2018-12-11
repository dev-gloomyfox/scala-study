package com.gloomyfox.chapter04

object Main {
  def main(args: Array[String]) = {

  }

  def mean(xs: Seq[Double]): Option[Double] = {
    if(xs.isEmpty) None
    else Some(xs.sum / xs.length)
  }

  def variance(xs: Seq[Double]): Option[Double] = {
    mean(xs) flatMap(m => mean(xs.map(x => math.pow(x - m, 2))))
  }

  def map2[A, B, C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] = {
    a flatMap(aa => b map (bb => f(aa, bb)))
  }

  // Option들의 목록을 받고 None이 하나라도 있으면 None, 그렇지 않으면 모든 값의 목록을 담은 Some
  def sequence[A](a: List[Option[A]]): Option[List[A]] = {
    ???
  }

  def traverse[A, B](a: List[A])(f: A => Option[B]): Option[List[B]] = {
    ???
  }

//  def mean(xs: IndexedSeq[Double]): Either[String, Double] = {
//    if(xs.isEmpty)
//      Left("mean of empty list!")
//    else
//      Right(xs.sum / xs.length)
//  }
//
//  def safeDiv(x: Int, y: Int): Either[Exception, Int] = {
//    try Right(x / y)
//    catch { case e: Exception => Left(e) }
//  }
//
//  def Try[A](a: => A): Either[Exception, A] = {
//    try Right(a)
//    catch { case e: Exception => Left(e) }
//  }
}
