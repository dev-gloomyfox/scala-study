// 1. 자료 형식과 함수의 선택
// 통상적인 왼쪽 접기로 정수들의 목록에 있는 정수들의 합 계산
//def sum(ints: Seq[Int]): Int = {
//  ints.foldLeft(0)((a, b) => a + b)
//}
//
//sum(Seq(1, 2, 3, 4, 5))
//
// 순차적으로 접는 대신, 분할 정복 알고리즘 적용
//def dndSum(ints: IndexedSeq[Int]): Int = {
//  if(ints.size <= 1) {
//    ints.headOption getOrElse 0
//  } else {
//    val (l, r) = ints.splitAt(ints.length / 2)
//    dndSum(l) + dndSum(r)
//  }
//}
//
//dndSum(IndexedSeq(1, 2, 3, 4, 5))
//
// foldLeft 기반 구현과는 달리 병렬화 가능
//
// 표현식 sum(l) + sum(r)을 보면, 병렬 계산을 나타내는 자료 형식이 하나의 결과를 담아야 한다는 점 파악 가능
// 해당 결과는 어떤 의미있는 형식이어야 하고 결과를 추출하는 수단도 필요
// 결과를 담을 컨테이너 형식 Par[A] 창안, 형식에 필요한 함수는 다음을 참조
//
// 평가되지 않은 A를 받고, 그것을 개별적인 스레드에서 평가할 수 있는 계산을 반환
// 함수의 이름이 unit인 것은 이 함수가 하나의 값을 감싸는 병렬성의 한 단위(unit)를 생성한다고 생각할 수 있기 때문
//def unit[A](a: => A): Par[A]
//
// 병렬 계산에서 결과 값을 추출
//def get[A](a: Par[A]): A
//
// 병렬적으로 정수를 합하는 계산
//def parSum(ints: IndexedSeq[Int]): Int = {
//  if(ints.size <= 1) {
//    ints.headOption getOrElse 0
//  } else {
//    val (l, r) = ints.splitAt(ints.length / 2)
//    val sumL: Par[Int] = Par.unit(parSum(l))
//    val sumR: Par[Int] = Par.unit(parSum(r))
//    Par.get(sumL) + Par.get(sumR)
//  }
//}
//
// 두 재귀적 sum 호출을 unit으로 감싸고, 두 부분 계산 결과들을 get을 이용하여 추출
//
// unit은 주어진 인수를 개별적인 스레드(논리적 스레드)에서 즉시 평가하거나 인수를 가지고 있다가 get이 호출되면 평가 시작도 가능
// 현 예제에서 병렬성의 이점을 취하기 위해서는 unit이 인수의 동시적 평가를 시작한 후 즉시 반환 필요
// - unit이 get이 호출될 때까지 실행을 지연시키면, 첫 병렬 계산의 실행이 끝나야 두 번째 계산이 시작
// 만약 unit이 인수들의 평가를 동시에 시작한다면 get 호출에서 참조 투명성이 깨질 가능성 존재
// sumL, sumR을 정의로 치환해보면 명백해지는 결과, 결과는 같지만 프로그램이 병렬 실행 불가
//Par.get(Par.unit(parSum(l))) + Par.get(Par.unit(parSum(r)))
//
// unit이 자신의 인수를 즉시 평가하기 시작하면, 그 다음은 get이 평가의 완료를 기다리는 것
// 단순히 sumL 변수와 sumR 변수를 나열하면 + 기호의 양변은 병렬로 실행 불가
// unit에 한정적인 부수 효과가 존재, 그 부수 효과는 get에만 관련
//
// unit은 비동기 계산을 나타내는 Par[Int]를 반환, 그런데 Par를 get으로 넘겨주는 즉시 get의 완료까지 실행이 차단된다는 부수 효과 발생
// 따라서 get을 호출하지 않거나, 적어도 지연 호출 필요, 즉 비동기 계산들을 그 완료를 기다리지 않고도 조합 가능
//
// unit과 get의 문제점 회피 방법
// get을 호출하지 않으면 parSum 함수는 반드시 Par[Int]를 반환
//
//def parSum2(ints: IndexedSeq[Int]): Par[Int] = {
//  if(ints.size <= 1) {
//    Par.unit(ints.headOption getOrElse 0)
//  } else {
//    val (l, r) = ints.splitAt(ints.length / 2)
//    Par.map2(parSum2(l), parSum2(r))(_ + _)
//  }
//}
//
// 재귀의 경우에 unit을 비호출
// unit의 인수가 게으른 인수인지도 불명확, 현재 예제에서는 인수를 게으르게 받는 것은 이득 X, 항상 그런지는 불확실
//
// map2는 계산의 양변에 동등한 실행 기회를 주어서 양변이 병렬로 계산되는 것이 합당
// 인수의 순서는 중요하지 않고, 결합되는 두 계산이 독립적이고 병렬로 실행 가능한 것을 표현하는 것이 중요
// map2의 두 인수가 엄격하게 평가된다고 할 때, 재귀를 만날 때마다 왼쪽에서 오른쪽으로 평가 필요
// 합산 트리의 왼쪽이 완전히 구축한 후에야 오른쪽이 엄격하게 구축되는 바람직하지 않은 결과
// 인수들을 병렬로 평가한다면 계산의 오른쪽 절반의 구축을 시작하기도 전에 계산의 왼쪽 절반이 실행되기 시작
//
// map2를 엄격하게 유지하되 실행이 즉시 실행되지 않게 하면 Par 값이 병렬로 계산해야 할 것의 서술을 구축하는 것을 의미
// 그 서술을 평가(get 등을 이용)하기 전 까지는 아무 일도 발생 X
// 서술을 엄격하게 구축한다면, 서술을 나타내는 객체가 무거운 객체
// 서술은 수행할 연산들의 전체 트리를 포함
//
// map2를 게으르게 만들고 양변을 병렬로 즉시 실행하는 것이 좋을 듯
//
// map2의 두 인수를 병렬로 평가하는 것이 항상 바람직한 것인지에 대한 의문
//Par.map2(Par.unit(1), Par.unit(1))(_ + _)
//
// 위와 같은 예에서 결합하고자 하는 두 계산은 아주 빠르게 완료될 것이고, 굳이 개별적인 논리적 스레드 필요 X
// 현재 API에서는 이런 정보 제공 수단 X
// 이러한 분기가 일어나는 지점을 구체적, 명시적으로 표현
//def fork[A](a: => Par[A]): Par[A]
//
// 이 함수는 주어진 Par가 개별 논리적 스레드에서 실행되어야 함을 명시적으로 지정하는 용도
//def parSum3(ints: IndexedSeq[Int]): Par[Int] = {
//  if(ints.size <= 1) {
//    Par.unit(ints.headOption getOrElse 0)
//  } else {
//    val (l, r) = ints.splitAt(ints.length / 2)
//    Par.map2(Par.fork(parSum3(l)), Par.fork(parSum3(r)))(_ + _)
//  }
//}
//
// fork로 인해서 map2를 엄격한 함수로 만들고, 인수들을 감싸는 것은 개발자 의도에 위임
// fork 같은 함수는 병렬 계산들을 너무 엄격하게 인스턴스화 하는 문제를 해결
// 좀 더 근본적으로는 병렬성을 명시적으로 프로그래머의 통제하에 두는 역할
// 여기서 다루는 관심사는 두 가지
// 하나는 두 병렬 과제의 결과들이 조합되어야 함을 지정하는 수단이 필요
// 다른 하나는 특정 과제를 비동기적으로 수행할지 아닐지를 선택하는수단
// 이 관심사를 분리했기 때문에 map2나 기타 조합기들이 병렬성에 관한 어떤 전역 방침도 내장할 필요 X
//
// fork가 있으니 unit을 엄격하게 만들어도 표현력 감소 X, 이 함수의 비엄격 버전은 unit과 fork로 간단히 구현 가능
//def lazyUnit[A](a: => A): Par[A] = fork(unit(a))
//
// lazyUnit 함수는 unit 같은 기본 조합기가 아닌 파생된 조합기의 간단한 예
// lazyUnit은 다른 연산들을 이용해서 정의 가능
// Par의 구체적인 표현을 선택할 때, lazyUnit은 그 표현에 대해 아무것도 알 필요 X
// 단지 Par가 Par에 대해 정의된 연산 fork와 unit을 거치게 된다는 점만 인지, 표현에 대해 많이 알 필요가 없다는 점은 연산이 일반적이라는 것을 암시
//
// fork는 인수들을 개별 논리적 스레드에게 평가되게 하는 수단
// 평가가 호출 즉시 일어나게 할 것인지, get 같은 어떤 함수에 의해 계산이 강제될 때까지 평가를 미룰지는 결정 X
// 평가가 fork의 책임인지 get의 책임인지 문제의 결정 필요
// API를 설계하면서 한 함수에 어떤 의미를 부여할지 확실하지 않은 상황에 처해도, 설계 공정을 진행하는 것은 항상 가능
// 여러 의미를 가진 fork와 get의 구현에 어떤 정보가 필요한지 생각하는 것으로 시작
//
// fork가 인수를 즉시 병렬로 평가한다면, 그 구현은 스레드를 생성하는 방법이나 과제를 일종의 스레드 풀에 제출하는 방법을 직간접적으로 인지 필요
// 스레드 풀은 반드시 접근 가능한(전역적으로) 자원이어야 하고, fork를 호출하는 시점에서 이미 적절히 초기화되어 있어야 함을 의미
// 해당 조건을 만족하려면 프로그램의 여러 부분에서 쓰이는 병렬성 전략을 개발자가 임의로 제어할 수 있는 능력 포기
// 구현이 무엇을 언제 사용할 것인지를 더 세밀하게 제어할 수 있으면 좋을 것
// 따라서 스레드 생성과 실행 과제 제출의 책임을 get에 부여하는 것이 적합
//
// fork가 인수의 평가를 뒤로 미루면, fork는 병렬성 구현을 위한 매커니즘에 접근 필요 X
// fork는 평가되지 않은 Par 인수를 받고 그 인수에 동시적 평가가 필요하다는 점을 표시
// Par 자체는 병렬성의 구체적인 구현을 알 필요 X, Par는 나중에 get 함수 같은 무언가에 의해 해석될 병렬 계산에 관한 서술
// 실행 가능한 일급 프로그램에 좀 더 근접, get 함수의 이름을 run으로 변경 후 병렬성이 실제로 구현되는 지점이 run 함수임을 명시
//def run[A](a: Par[A]): A
//
// run은 병렬성을 구현하는 어떤 수단이 필요
//
//
// 2. 표현의 선택
//def map2[A,B,C](a: Par[A], b: Par[B])(f: (A,B) => C): Par[C]
//def fork[A](a: => Par[A]): Par[A]
//def unit[A](a: A): Par[A]
//def lazyUnit[A](a: => A): Par[A] = fork(unit(a))
//def run[A](a: Par[A]): A
//
// unit은 상수 값을 병렬 계산으로 승격
// map2는 두 병렬 계산의 결과들을 이항 함수로 조합
// fork는 주어진 인수가 동시적으로 평가될 계산임을 표시, 그 평가는 run에 강제되어야 실행
// lazyUnit은 평가되지 않은 인수를 Par로 감싸고, 그것을 병렬 평가 대상으로 표시
// run은 계산을 실제로 실행해서 Par로부터 값을 추출
//
// ExecutorService와 Future를 사용
//def run[A](s: ExecutorService)(a: Par[A]): A
//
// Par[A]의 간단한 모형은 ExecutorService => A
// 계산 완료까지 대기 시간이나 취소 여부를 run의 호출자가 결정할 수 있게 하면 더 좋을 듯
// Par[A]를 ExecutorService => Future[A]로 두고, run은 그냥 Future를 반환
//type Par[A] = ExecutorService => Future[A]
//def run[A](s: ExecutorService)(a: Par[A]): Future[A] = a(s)
//
//
// 3.API의 정련
// Future의 인터페이스가 순수 함수적 X
// Future 메소드들이 부수 효과에 의존하나, Par API 자체는 순수
// Future의 내부 작동 방식은 오직 사용자가 run을 호출해서 구현이 ExecutorService를 받게되어야 표출
// 구현이 언젠가는 효과에 의존하나, 사용자는 항상 순수한 인터페이스 사용
// API가 순수하므로 그 효과들은 사실상 부수 효과 X
//
// 기존의 조합기들로 다른 것을 표현
// 하나의 List[Int]를 산출하는 병렬 계산을 나타내는 Par[List[Int]], 이것을 결과가 정렬된 Par[List[Int]]로 변환
//def sortPar(parList: Par[List[Int]]): Par[List[Int]]
// Par에 run을 적용하고, 결과 목록을 정렬하고, 정렬된 목록을 unit을 이용해 다시 Par로 꾸릴 수 있으나 run 호출을 회피
// unit 외에 Par의 값을 조작할 수 있는 조합기는 map2 뿐이라고 가정
//def sortPar(parList: Par[List[Int]]): Par[List[Int]] = {
//  map2(parList, unit(()))((a, _) => a.sorted)
//}
//
// 좀 더 일반화
//def map[A, B](pa: Par[A])(f: A => B): Par[B] = {
//  map2(pa, unit(()))((a, _) => f(a))
//}
//
//def sortPar(parList: Par[List[Int]]) = map(parList)(_.sorted)
//
// 기본 수단이라고 생각했던 함수를 좀 더 근본적인 기본 수단들로 표현 가능
//
// 4. API의 대수
// 원하는 연산의 서명만 작성한 후 그 형식을 따라가다 보면 구현에 도달하는 경우가 다수
// 구체적인 문제 영역을 완전히 잊어버리고 형식들이 잘 맞아떨어지게 하는 데에만 집중 가능
// 대수 방정식을 단순화할 때 하는 추론과 비슷한 추론 방식
// API를 하나의 대수(algebra), 즉 일단의 법칙 또는 참이라고 가정하는 속성(property)들을 가진 추상적인 연산 집합으로 간주
// 그 대수에 정의된 규칙에 따라 그냥 형식적으로 기호를 조작하면서 문제 해결
//
// API가 준수하리라 기대하는 법칙들을 공식화하면 유용
// 그것들을 실제로 적어서 정밀하게 다듬으면 비공식적인 추론으로는 명백히 드러나지 않았을 설계상의 선택들이 더 명확
//
// 법칙의 선택에는 결과 생성
// 연산에 부여할 수 있는 의미에 제약이 생기고, 선택 가능한 구현 방식 결정, 참일 수 있는 다른 속성들에도 영향
// 다음은 라이브러리를 위한 검사 코드를 작성할 때 하나의 검례로 사용할 코드
//map(unit(1))(_ + 1) == unit(2)
//
// unit(1)에 _ + 1 함수를 사상한 것이 어떤 의미로는 unit(2)와 동등함을 의미
// 임의의 유효한 ExecutorService에 대해 두 Par 객체의 Future 결과가 서로 같다면 두 Par 객체가 동등
//def equal[A](e: ExecutorService)(p: Par[A], p2: Par[A]): Boolean = {
//  p(e).get == p2(e).get
//}
//
// 법칙도 일반화 가능
//map(unit(x))(f) == unit(f(x))
//
// 이는 이 법칙이 1과 _ + 1 함수뿐만 아니라 임의의 x와 f에 대해 성립함을 의미
// 이 점은 구현에 일정한 제약을 추가
// unit을 구현할 때 unit에 주어진 입력을 조사해서 그 값이 1이면 병렬 계산의 결과로 42를 산출하게 구현은 불가
// unit은 자신이 받은 것을 넘겨주기만 하는 역할
//
// ExecutorService도 Callable 객체를 실행을 위해 제출할 때, 받은 값에 어떤 가정을 두거나 행동 방식을 변경 X
//
// 구체적으로 이 법칙은 map과 unit의 구현에서 하향 캐스팅(down casting)이나 isInstanceOf 점검(타입캐스팅)을 혀용 X
//
// 어떤 법칙을 정의할 때에는 한 가지 사실만 말하는 더 간단한 법칙들을 이용해서 정의 가능
//map(unit(x))(f) == unit(f(x))
//map(unit(x))(id) == unit(id(x)) // f를 항등 함수로 치환
//map(unit(x))(id) == unit(x)
//map(y)(id) = y
//
// 간단한 새 법칙은 map에 관해서만 언급, unit의 언급은 군더더기
// map이 할 수 없는 것은 함수를 결과에 적용하기 전에 예외를 던지고 계산을 망치는 것 등
// map은 단지 함수 f를 y의 결과에 적용할 뿐
// map(y)(id) == y라고 할 때 반대 방향의 치환을 통해서 원래의 좀 더 복잡한 법칙으로 복귀 가능
//
// fork가 병렬 계산의 결과에 영향을 미치지 말아야 한다는 속성 점검
//fork(x) == x
//
// fork(x)는 x와 동일한 일을 수행하되, 개별적인 논리적 스레드에서 비동기적으로 수행
// 구현이 매우 강하게 강제
//
// fork의 구현들 대부분에서 발생하는 미묘한 문제점 존재
// 고정된 크기의 스레드 풀을 사용하는 ExecutorService 구현은 교착 가능
//def fork[A](a: => Par[A]): Par[A] = {
//  es => es.submit(new Callable[A] {
//    def call = a(es).get
//  })
//}
//
// 이 코드는 먼저 Callable을 제출하고, 그 Callable 안에서 또 다른 Callable을 ExecutorService에 제출, 그 결과가 나올 떄까지 실행 차단
// 스레드 풀 크기가 1이면 문제 발생
//
// 법칙이 성립하도록 구현을 고치거나, 법칙이 성립하는 조건들을 좀 더 명시적으로 밝히도록 법칙을 정련(스레드 풀이 무한이 자랄 수 있는 조건 추가)
//
// 고정 크기 스레드 풀에 대해 잘 작동하도록 수정
//def fork[A](fa: Par[A]): Par[A] = {
//  es => fa(es)
//}
//
// 교착이 방지되는데 개별적인 논리적 스레드를 띄워서 fa를 평가 X
// fork의 호출로 피하고자 했던 상황, 해당 fork 구현도 계산의 인스턴스화를 필요한 시점까지 미루는 용도로 유용하게 사용 가능
// 이름을 delay로 변경
//def delay[A](fa: Par[A]): Par[A] = {
//  es => fa(es)
//}
//
// 고정 크기 스레드 풀로도 잘 작동하고, 전혀 차단되지 않는 방식의 Par 구현 개발
// 현재 문제는 Future의 get 메소드를 호출하지 않고서는 Future에서 값을 꺼낼 수 없는데, 그 메소드를 호출하면 현재 스레드(호출 스레드)의 실행이 차단
// Par의 표현이 이런 식으로 자원을 흘리지 않도록 하려면 반드시 비차단 방식, 현재 스레드를 차단하는 메소드(Future.get 등)을 절대 호출 X
//
// 기본 착안
// Par를 Future로 바꾸어서 값을 꺼내는 대신, 적당한 때에 호출되는 콜백을 등록할 수 있는 새로운 Future를 도입
// 관점의 전환에 해당
//sead trait Future[A] {
//  private[parallelism] def apply(k: A => Unit): Unit
//}
//type Par[+A] = ExecutorService => Future[A]
//
// 새 Par 형식은 이전과 동일하나 직접 만든 새로운 Future 사용
// 새 Future는 A 형식의 결과를 산출하는 함수 k를 받고 그 결과를 이용해서 어떠한 효과를 수행하는 apply 메소드 제공, 이러한 함수를 callback
// apply 메소드에는 private[parallelism]이 지정되어 있고, 라이브러리 사용자에게 노출 X
// 이렇게 해야 API가 순수성을 유지하고, 법칙들의 성립이 보장
//
// run 함수의 구현을 Par[A]를 받고, A를 돌려주는 형태로 변경
// unit, fork 함수의 구현을 변경해보면 java.util.concurrent의 저수준 기본수단만으로는 정확한 비차단 구현이 난해(추후 추가 서술)
// actor라고 불리는 비차단 동시성 기본 수단 사용
// Actor는 하나의 동시적 프로세스로 스레드를 계속해서 차지 않는다는 점이 특징
// 메시지를 받았을 때에만 스레드를 점유, 여러 스레드가 동시에 하나의 행위자에 메시지를 보낼 수 있지만 행위자는 메시지를 오직 한 번에 하나씩만 처리
// 여러 스레드가 접근해야 하는 까다로운 코드를 작성할 때 유용
// 해당 구현을 이용하면(추후 기술) 아무리 복잡한 Par 값들이라도 스레드 고갈을 걱정하지 않고 실행 가능
//
// 법칙들이 중요함을 보여주는 것이 목적
// 법칙들은 문제를 다른 각도에서 인지
//
//
// 5. 조합기들을 가장 일반적인 형태로 정련
// 새로운 조합기를 필요로 하는 시나리오를 만났을 때, 바로 구현하는 것보다 그 조합기를 가장 일반적인 형태로 정련할 수 있는 지 살펴보는 것이 바람직
// 새 조합기가 아닌 일반적인 조합기의 특별한 경우가 필요했던 것일 수 있기 때문
//
// 두 분기 계산 중 하나를 초기 계산의 결과에 기초해서 선택하는 함수의 예
//def choice[A](cond: Par[Boolean])(t: Par[A], f: Par[A]): Par[A]
//
// 간단한 차단식 구현
//def choice[A](code: Par[Boolean])(t: Par[A], f: Par[A]): Par[A] = {
//  es => {
//    if (run(es)(cond).get) t(es)
//    else f(es)
//  }
//}
//
// 만족하고 넘어가는 대신 더 일반적인 함수 생성 가능
// n을 실행하고, 그 결과에 기초해서 choices의 병렬 계산 중 하나를 선택
//def choiceN(n: Par[Int])(choices: List[Par[A]]): Par[A]
//
// 더 일반적인 형태로 정련 가능
//def choiceMap[K, V](key: Par[K])(choices: Map[K, Par[V]]): Par[V]
//
// Map도 필요 이상으로 구체적 A => Par[B]의 형식의 함수를 제공하는 데이만 쓰일 뿐
// 이들을 모두 통합하면
//def chooser[A, B](pa: Par[A])(choices: A => Par[B]): Par[B]
//
// 첫 계산의 결과가 준비되기 전에 둘째 계산이 반드시 존재해야 할 필요 X
// 일반적으로 bind나 flatMap이라고 후칭
// 두 단계로 분해 가능
// Par[A]에 f: A => Par[B]를 사상해서 Par[Par[B]]를 생성, Par[Par[B]]를 평탄화해서 Par[B]를 만드는 것
//def join[A](a: Par[Par[A]]): Par[A]
//
// 기본적인 조합기들은 다소 까다로운 논리를 캡슐화하고 있는 경우가 많으며, 그런 것들을 재사용하면 까다로운 논리를 되풀이해서 다룰 필요 X