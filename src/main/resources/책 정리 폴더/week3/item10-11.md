# 3장 모든 객체의 공통 메서드 
- Object에서 final이 아닌 메서드(equals, hashCode,, toString, clone, finalize)는 모두 재정의(overriding)을 염두에 두고 설계된 것
- 따라서 모든 클래스는 이 메서드들을 일반 규약에 맞게 재정의 해야 한다. 
  - 일반규약 -> Object의 각 메서드 javadoc으로 확인 가능 
 <br>
  
## item10 equals는 일반 규약을 지켜 재정의하라
- 필요하지 않다면 재정의하지 않는 것이 최선
- 재정의가 필요하지 않은 상황 
  - 각 인스턴스가 본질적으로 고유할때
    - 값 표현 객체(VO를 말하는 듯)가 아닌 동작하는 개체를 표현하는 클래스를 말함 
    - ex) Thread, Controller, Serviec 등 
  - 인스턴스의 '논리적 동치성(local equality)'을 검사할 일이 없다. 
    - Pattern처럼 정규표현식을 나타내는지 검사한다면 값을 따로 비교할 필요가 없다. 
  - 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다. 
    - Set구현체는 AbstractSet이 구현한 equals를 상속받아서 사용 
  - 클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없다. 
    - equals호출될 일이 없어도 실수로 호출되는 것을 막고싶다면 아래처럼 구현
```java
@Override public boolean equals(Object o) {
	throw new AssertionError(); // 호출 금지! 
}
```
     
 <br>

- 재정의가 필요한 상황
  - 논리적 동치성을 확인해야 하는데, 상위 클래스의 equals가 논리적으로 동치성을 비교하도록 재정의 되지 않았을 때 
    - 객체의 식별성(object identity: 두 객체가 물리적으로 같은가)이 아닌 논리적 동치성 확인 시에 
    - 주로 값 클래스 
      - ex) Integer, String 
      - 값 클래스 예외 : 값이 같은 인스턴스가 둘 이상 만들어지지 않음을 보장하는 인스턴스 통제 클래스(아이템1)라면 euqlas를 재정의하지 않아도 된다. 
        - ex) Enum, 싱글턴 객체 
 <br> <br>


- Equals의 일반규약 
  - <img width="661" alt="image" src="https://user-images.githubusercontent.com/52403454/201531073-1690a483-82b9-4d88-8a26-b78e06806644.png">
```html
equals 메서드는 동치관계(equivalence relation)를 구현하며, 다음을 만족한다.

반사성(reflexivity) : null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true다.
대칭성(symmetry) : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)가 true면 y.equals(x)도 true다.
추이성(transitivity) : null이 아닌 모든 참조 값 x, y, z에 대해, x.equals(y)가 true이고 y.equals(z)도 true면 x.equals(z)도 true다.
일관성(consistency) : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)를 반복해서 호출하면 항상 true를 반환하거나 항상 false를 반환한다.
null-아님 : null이 아닌 모든 참조 값 x에 대해, x.equals(null)은 false다.
```
  - 반사성 : 객체가 자기 자신과 같아야 한다.
  - 대칭성 : A객체의 equals가 대소문자를 구분하고, B객체의 equals는 대소문자를 구분하지 않으면 어길 수 있다.
```java
A a = new A("ABC");
B b = new B("abc");
a.equals(b) == false;
b.eqauls(a) == true;
```
  - 추이성 : equals에 영향을 주는 정보들을 추가하면 깨짐. 구체 클래스를 확장해 새로운 값을 추가하면서 equals규약을 만족시킬 방법은 존재하지 않는다.
```java
Point p = new Point(1, 2);
ColorPoint cp = new ColorPoint(1, 2, Color.RED);
// p.equals(cp) = true
// cp.equals(p) = false
=> 대칭성 위배
=> 이를 해결하기 위해 ColorPoint.equals가 Point와 비교할 때는 색상을 무시하도록 하면?


@Override public boolean equals(Object o) {
	if (!(o instanceof Point))
    	return false;
        
   	// o가 일반 Point면 색상을 무시하고 비교한다.
    if (!(o instanceof ColorPoint))
    	return o.equals(this);
        
  	// o가 ColorPoint면 색상까지 비교한다.
    return super.equals(o) && ((ColorPoint) o).color == color;
}

ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
Point p2 = new Point(1, 2);
ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);

// p1.equals(p2) == true
// p2.equals(p3) == true
// p1.equals(p3) == false
=> 추이성 위배 
```


   -우회방법 : 아이템 18 : 상속 대신 컴포지션을 사용하라 
      - ex) Timestamp : 잘못된 설계
  - 일관성 : 두 객체가 같다면 앞으로도 영원히 같아야 한다. 
    - 클래스가 불변이든 가변이든 equals판단에 신뢰할 수 없는 자원이 끼어서는 안된다. 
      - ex) java.net.URL의 equals -> 네트워크에 영향을 받아서 문제가 생김 : 잘못된 설계   
  - null-아님 : 모든 객체가 null과 같지 않아야 한다는 뜻. 
    - x.equals(null)은 항상 false를 반환 
    - 명시적 null검사는 필요 없음. 대신 묵시적 null검사 사용하자
```java
//명시적 null검사
@Override 
public boolean equals(Object o) {
  if(o == null) return false; //불필요
  return this.x == o.x;
}
```
 ```java   
 //묵시적 null 검사 
 @Override
public boolean equals(Object o) {
  if(!(o instanceof MyClass)) return false; //묵시적 null검사
  MyClass clazz = (MyClass) o;
  return this.x == clazz.x;
}
```
   - instanceof는 첫번째 피연선자가 null이면 false를 반환하기 때문에 저절로 null검사가 됨

 <br> <br> <br>




- 올바른 equals작성법
  1. ==연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.
  2. instanceof 연산자로 입력이 올바른 타입인지 확인한다.
  3. 입력을 올바를 타입으로 형변환한다. 
  4. 입력 객체와 자기 자신의 대응되는 '핵심'필드들이 모두 일치하는지 하나씩 확인한다. 

 <br> <br>


- 주의사항
  - 비교 방법
    - 기본 타입 : ==연산자
      - double, float제외 : 특수한 부동소수 값 등을 다뤄야 하기 때문 
    - 참조 타입 : equals메서드로
    - float, double필드 : Float.compare(..), Double.compaer(..)메서드 사용

  - null이 의심되는 필드는 Objects.equals(obj, obj)를 이용해 NullPointerException을 예방하자
  - 성능을 올리고자 한다면
      - 다를 확률이 높은 필드부터 비교한다.
      - 비교하는 비용(시간복잡도)이 적은 비교를 먼저 수행
   - equals 구현 후 대칭적, 추이성, 일관적 체크 - 테스트코드를 작성해서 체크해보자
   - euqlas를 재정의할 땐 hashCode도 재정의하자 (아이템 11)
   - 너무 복잡하게 해결하려들지 말자 
   - Object외의 타입을 매개변수로 받는 equals 메서드는 선언하지 말자. 
      - @Override 어노테이션을 일관되게 사용하면 컴파일 되지 않기 때문에 예방이 가능하다!
    - AutoValue
      - 클래스에 어노테이션을 작성하면 알아서 메서드들을 만들어줌 
      - AutoValue를 사용해도 테스트코드는 작성해야한다.
      - Lombok의 @EqualsAndHashCode도 있다.



<br><br><br><br>
## item11 equals를 재정의하려거든 hashCode도 재정의하라

- equals를 재정의한 클래스 모두에서 hashCode도 정의해야한다. 
  - 그렇지 않으면 hashCode 일반규약을 위반하여 hashMap,hashSet같은 컬렉션 원소로 쓸 때 문제가 생긴다
  - <img width="658" alt="image" src="https://user-images.githubusercontent.com/52403454/201534556-74d46a82-268a-4bc3-a2cf-f335f4f204ab.png">
```java
- equals비교에 사용되는 정보가 변경되지 않았다면, 객체의 hashcode 메서드는 몇번을 호출해도 항상 일관된 값을 반환해야 한다.
(단, Application을 다시 실행한다면 값이 달라져도 상관없다. (메모리 소가 달라지기 때문))

- equals메서드 통해 두 개의 객체가 같다고 판단했다면, 두 객체는 똑같은 hashcode 값을 반환해야 한다.

- equals메서드가 두 개의 객체를 다르다고 판단했다 하더라도, 두 객체의 hashcode가 서로 다른 값을 가질 필요는 없다. (Hash Collision)
단, 다른 객체에 대해서는 다른 값을 반환해야 해시테이블의 성능이 좋아진다.
```
  - 두번째 조항 중요! - equals는 물리적으로 다른 객체도 논리적으로 같다고 할 수 있지만, hashCode는 둘이 전혀 다르다고 판단. 
  - 문제 상황 ex
```java
           HashMap<PhoneNumber, String> map = new HashMap<>();
        map.put(new PhoneNumber(707, 867, 5307), "제니");
        map.get(new PhoneNumber(707, 867, 5307))
```
  - PhoneNumber클래스는 재정의하지 않았기 때문에 논리적인 동치인 두 객체가 서로 다른 해시코드를 반환하여 두번째 규약을 지키지 못한다. 

<br> <br>

- 최악의 hashCode
```java
@Override
public int hashCode() {
  return 42;
}
```
  - 모든 객체에게 똑같은 값을 내어주어서 속도가 저하되고, 객체가 많아지면 쓸 수 없다.
  - 좋은 해시 함수는 서로 다른 인스턴스에 다른 해시코드를 반환해야한다. 
  <br> <br>
- 좋은 hashCode 작성
  1. result를 선언 후 c로 초기화 (c는 다음 순서에서)
  2. 객체의 나머지 핵심필드 f각각에 대해 다음 작업을 수행한다.
    1.해당 필드의 해시코드 c를 계산 : 기본타입, 참조타입, 배열에 따라서 각각 계산 방법이 다름
    2.c로 result를 갱신한다.
      - result = 31 * result + c;
      - 31곱하는 이유는 31이 홀수이면서 소수이기 때문. 짝수를 곱하는 것은 shift연산과 같은 결과가 나기 떄문에 비추 
  3.result로 반환한다. 

- hashCode를 구현했다면 동치인 인스턴스에 대해 똑같은 해시 코드를 반환할지 자문해보자 -> 단위테스트 필수

```java
@Override
public int hashCode() {
    int result = Short.hashCode(areaCode);

    // 기본타입 : Type.hashCode()를 실행한다
    result = c * result + Integer.hashCode(secondNumber);

    //참조타입 : 참조타입에 대한 hashcode 함수를 호출 한다.
    result = c * result + address == null ? 0 : address.hashCode();

    // 필드타입 : 핵심 원소를 각각 필드처럼 다룬다.
    for (String element : array) {
      result = c * result + elem == null ? 0 : elem.hashCode();
    }

    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);

    return result;
}
```

 <br> <br>
 
- Objects클래스의 hash메서드로도 해시코드 생성 가능
  - 해시코드를 계산해주는 정적 메서드
  - 속도가 느리니 성능이 민감하지 않은 상황에서만 사용
   
 <br> <br>
 
- 주의사항
  - 클래스가 불변이고 해시코드를 계산하는 비용이 크다면, 캐싱하는 방식 고려
    - 객체가 주로 해시의 키로 사용될 것 같다면 해시의 키 미리 계산해둬야한다.
    - lazy loading전략도 좋다. 하지만 thread-safe하게 작동하도록 신경써야한다.
 ```java
private int hashCode;

@Override
public int hashCode() {
      	int result = hashCode; // 초기값 0을 가진다.
        if(result == 0) {
        int result = Integer.hashCode(areaCode);
        result = 31 * result + Integer.hashCode(areaCode);
        result = 31 * result + Integer.hashCode(areaCode);
        hashCode = result;
        }
        return result;
}
 ```
 
  - 해시코드를 계산할 때 핵심 필드를 생략해서는 안된다.
    - 속도는 빨라지지만, 해시 품질이 나빠져 해시 테이블의 성능이 심각하게 떨어질 수 있다.
  - hashCode가 반환하는 값의 생성 규칙을 API사용자에게 자세히 알리면 안된다.
    - 그래야 클라이언트가 이 값에 의지하지 않게 되고, 추후에 계산 방식을 바꿀 수 있다. 
