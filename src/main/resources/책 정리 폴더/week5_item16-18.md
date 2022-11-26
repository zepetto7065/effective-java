## item14. public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라.

- 퇴보한 클래스
- 필드 직접 접근 가능

```java
class Point {
    public double x;
    public double y;
}
```

- 모두 private으로 바꾸고 getter를 추가하자.
- public한 클래스라면 접근자를 제공하는게 맞다. (package, private 은 하등의 문제가 없다. 단, 테스트의 용의성도 고려)

- (잘못된예시) 자바 java.awt.package Point , Dimention (PointAndDimention.java 참고)
    - 외부로 노출되었기 떄문에 성능상의 문제를 해결하기 어렵다 (누가 컨트롤 하는지 찾기 힘들다)

### [결론]

- public 클래스는 절대 가변 필드를 직접 노출하지 말자.

<hr>

## item15. 변경 가능성을 최소화하라.

- 불편클래스? 내부 값을 수정할 수 없는 클래스 ( ex. String, BigInteger, BigDecimal )
    - 5가지 규칙
        - 객체의 상태를 변경하는 메서드(변경자)를 제공하지 않는다.
        - 클래스를 확장할 수 없도록 한다.
            - 이를 태면 상속을 막는다
        - 모든 필드를 final로 선언한다.
        - 모든 필드를 private 선언을 한다.
        - 자신 외에는 내부의 가변 컴포넌트에 접근할 수 없도록 한다.
            - 클라이언트 입장에서 해당 객체를 참조할 수 없도록 한다.
            - 객체 참조가 아닌 생성자 또는 접급자, readObject 메서드를 사용하여 방어적 복사를 수행하라.

<br>

### [장점] - 불변 클래스

```java
//코드 17-1
public final class Complex {
    public final double re;
    public final double im;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public Complex plus(Complex c) {
        return new Complex(re + c.re, im + c.im);
    }
}
```

- 사칙연산 메서드들이 인스턴스 자신은 수정하지 않고 새로운 Complex를 만들어낸다.
    - 피연산자 자체(re,im)는 그대로 이고 변화된 Complex를 반환 --> 함수형 프로그래밍
        - 반대로 절차 혹은 명령형 프로그래밍
        - 참고이지만 plus와 같은 전치사를 naming에 사용했다 (BigInteger, BigDecimal은 그렇지 않다는?)
    - 함수형 프로그래밍 방식은 불편이 되는 영역 비율을 높일 수 있다는 장점이 있다.
- 불변 객체는 생성 시점의 상태를 파괴될 떄까지 그대로 간직한다.
- 불변 객체는 스레드 안전하여 따로 동기화할 필요가 없다.
    - 여러 스레드가 동시에 사용해도 절대 훼손되지 않는다. -> 불변클래스니까 -> 불변 객체는 안심하고 공유 가능
    - 한번 만든 인스턴스를 최대한 재사용하자
      - 가장 쉬운 재활용 방법 (상수 이용)
  ```java
    public static final Complex ZERO = new Complex(0,0);
  ```

- 불변 클래스는 인스턴스를 캐싱하여 같은 인스턴스를 중복 생성하지 않게 해주는 정적 팩터리(item1)를 제공 가능.
  - 박싱된 기본 타입 클래스와 BigInteger
    - heap 메모리 사용량이 줄어드므로 gc 비용 감소
    - 새로운 클래스 생성시 public 생성자 대신 , private static 정적팩터리를 이용하면 클라이언트 수정없이 캐싱 기능을 사용하는것과 같다.
  
- 불변객체를 자유롭게 공유할 수 있다는 건 방어적 복사도 필요없다.
  - 아무리 복사해보았자 원본과 필요없으니까.
  - clone이나 복사 생성자를 제공하지 않는게 좋다.
    - (잘못된 예시) String clone 
      - ex. DeepCloning.java
- 불변객체는 내부 데이터 공유가 가능하다. 
  - BigInteger
    - 부호는 int 변수 , 크기에 int 배열
    - negate 메서드는 크기가 같고 부호는 반대인 새로운 BigInteger 생성
    - 크기는 공유가 가능하다
- 객체를 만들때 다른 불변 객체들을 구성요소로 사용하면 이점이 많다.
  - 맵 키와 집합의 원소로 쓰기에 좋다. ( 내부 값이 허물어 지지 않는다. )
- 불변 객체는 그 자체로 실패 원자성을 제공 
  - 절대로 불일치하지 않는다랑 같은 말

### [단점]
- 값이 다르면 반드시 독립된 객체로 만들어야 한다.
  - 귀찮다
  - jackson 에 이런 방식 방안이 될지도?
    - instance = instance.setColor(red);
  - 가변 동반 클래스가 이를 보완해준다
    - String의 가변 동반 클래스 StringBuilder
  ```java
  //1. StringBuider 인스턴스 생성
  //2. String을 하나씩 append
  //3. toString 메서드를 통해 String 결과
  //반면 String은 append시 독립된 객체를 생성
  @Override
  public StringBuilder append(Object obj) {
  return append(String.valueOf(obj));
  }
  
  @Override
  @HotSpotIntrinsicCandidate
  public StringBuilder append(String str) {
  super.append(str);
  return this;
  }
  
  @Override
  @HotSpotIntrinsicCandidate
  public String toString() {
  return isLatin1() ? StringLatin1.newString(value, 0, count)
  : StringUTF16.newString(value, 0, count);
  }
  ```
### [불편 클래스의 설계 방법]
1. 모든 생성자를 private 혹은 package-private으로 만들고 public 정적 메서드 제공
   ```java
    //코드 17-1
    public final class Complex {
    public final double re;
    public final double im;

    private Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public static Complex valueOf(double re, double im) {
        return new Complex(re, im);
    }
    }
   ```
- final이며 확장 불가, 객체 캐싱 기능으로 성능 향상
- BigInteger, BigDecimal을 인수로 받는 다면 주의하자 (final class 아님)
  - 진짜 원본인지 확인할 필요가 있다.
  
### [정리]
- 클래스는 꼭 필요한게 아니라면 불변으로 만들자
- 불변으로 만들수 없는 클래스라도 변경할 수 있는 부분은 최소한으로 두자
- (왠만하면)필드는 private final로 두자
- 생성자는 불변식 설정이 완료된 객체를 생성하자 (함수형 프로그래밍)

<hr>

## item18. 상속보다는 컴포지션을 사용하라.
- 상속이 항상 최선은 아니다.

<br>

- 메서드 호출과 달리 상속은 캡슐화를 떨어트린다.
  - 상위클래스는 릴리스마다 내부 구현이 달라 질수 있다. ( 오동작의 가능성 )
  - 대표적 예시 -> HashSet 
    ```java
      public class InstrumentedHashSet<E> extends HashSet<E>{
        ...
      }
     
    InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
    s.addAll(List.of("탁", "탁탁", "펑"));
    ```
  - getAddCount시 3이 아닌 6을 반환 -> 자기사용 여부에 따라 고쳐질수 있지만 자바플랫폼 전반적인 정책은 아니다
- 상위 클래스 새로운 메서드 추가시 , 하위 메서드를 역시 새로운 메서드를 추가햐야한다.
  - 수정사항 발생

### 대안-컴포지션
- 컴포지션? 기존 클래스가 새로운 클래스의 구성요소로 쓰인다
- 기본 확장 대신 대응하는 클래스 호출로 확장하자 
  - forwarding 
  - forwarding method

- 래퍼 클래스 (forwarding 받은 , InstrumentedSet)
  - set을 감싸사고(wrap) 있는 InstrumentedSet
    - ForwardingSet<E>
    - InstrumentedSet은 HashSet 모든 기능을 정의한 Set 인터페이스 활용
    - 다른 Set 계층을 덧씌운다하여 데코레이터 패턴 (Decorator Pattern)
    - 컨포지션과 전달의 조합은 위임 (delegation)이라한다.
    - 단점으로는 콜백 프레임워크와는 어울리지 않는다.
      - 호출(콜백)시, 자신을 감싸고 있는 래퍼의 존재를 모른다.
        - 실전에서는 사실 성능상 그렇게 차이 나는건 아니다?

- 상속은 반드시 하위클래스가 상위클래스를 따라야할 때 쓰여야 한다.
  - is-A
  - 아니라면 상위클래스를 인스턴스로 두자 (그 순간 필수 구성요소가 아닌 구현방법의 요소가 된다. 명언이다.)
  - 확장하려는 클래스의 API에 아무런 결함이 없는가? 그 결함이 API까지 전달이 되도 되는가? 상속은 그 결함까지 그대로 승계한다.

### [정리]
- 상속은 캡슐화를 해친다.
- is-a 일때만 사용하자
- 상속의 취약점을 피하려면 컴포지션을 사용하자