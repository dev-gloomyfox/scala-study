def fib(n: Int): Int = {
  @annotation.tailrec
  def go(n: Int, prev: Int, cur: Int): Int = {
    if (n == 1) prev
    else go(n-1, cur, cur+prev)
  }
  go(n, 0, 1)
}

fib(7)

// 임의의 형식에 대해 작동하는 함수를 작성하는 경우
// 세부 사항이 비슷한 경우 임의의 주어진 타입 A에 대해 작동하게 일반화 가능
// Generic Function이라고 부르는 다형적 함수의 예시
// 타입에 대한 추상화
def findFirst[A](as: Array[A], p: A => Boolean): Int = {
  @annotation.tailrec
  def loop(n: Int): Int = {
    if (n >= as.length) -1
    else if (p(as(n))) n
    else loop(n+1)
  }

  loop(0)
}

def isSorted[A](as: Array[A], ordered: (A, A) => Boolean): Boolean = {
  @annotation.tailrec
  def loop(n: Int): Boolean = {
    if (n >= as.length-1) true
    else if(ordered(as(n), as(n+1))) loop(n+1)
    else false
  }

  loop(0)
}

// 익명 함수, 함수 리터럴
// 함수를 이름을 가진 메소드로 정의하는 대신, 간편한 구문으로 정의 가능
findFirst(Array(7, 9, 3), (x: Int) => x == 9)

// 함수 입력의 형식을 유추 가능한 경우 타입 생략도 가능
(x: Int, y: Int) => x == y

// 함수가 어떤 형식 A에 대해 다형적이면,
// A에 대해서는 인수들로서 함수에 전달된 연산들만 수행 가능
// 주어진 다형적 타입에 대해 단 하나의 구현만 가능해질 정도로 가능성의 공간이 축소
// 부분 적용(partial application)
// 단 하나의 구현만이 가능
// A라는 형식이 있고 C를 산출하는 함수가 있으면, B만으로 C를 산출하는 함수 획득
// 형식을 따라가다 보면 정확한 구현이 완성
def partial1[A, B, C](a: A, f: (A, B) => C): B => C = {
  b => f(a, b)
}

// currying: 인수가 두 개인 f를 인수 하나를 받고 그것으로 f를 부분 적용하는 함수로 변환
def curry[A, B, C](f: (A, B) => C): A => (B => C) = {
  a => b => f(a, b)
}

def uncurry[A, B, C](f: A => B => C): (A, B) => C = {
  (a, b) => f(a)(b)
}

def compose[A, B, C](f: B => C, g: A => B) : A => C = {
  a => f(g(a))
}

// 함수형 프로그래밍에서는 합성이 일상적이기 때문에 Function1(인수 하나를 받는 함수들에 대한 인터페이스)의 compose 메소드 제공
// f andThen g == g compose f
val f = (x: Double) => math.Pi / 2 - x
val cos = f andThen(math.sin)

// 다형적인 고차 함수들은 적용 범위가 넓은 경우가 많은데, 함수가 특정 문제 영역을 해결하려는 요구를 가진 것이 아니라
// 다수의 문맥에서 발생하는 공통의 패턴을 추상화