# 아이템 13. ****clone 재정의는 주의해서 진행해라****

- Cloneable은 복제해도 되는 클래스임을 알리기 위한 인터페이스.
    - 하지만 의도한 목적을 제대로 이루지는 못함.
        - 그 이유는 clone 메서드가 선언된 곳이 Cloneable이 아닌 Object이고, 그마저도 protected라는데 있음.
        - 그래서 Cloneable을 구현하는 것만으로는 외부 객체에서 clone 메서드를 호출할 수 없음.
    - clone() 메서드는 따로 오버라이딩해줘야 하며, 접근제한자가 protected라서 같은 패키지에서만 접근가능함.
- Cloneable 인터페이스는 Object의 protected 메서드인 clone의 동작 방식을 결정함.
    - Cloneable을 구현한 클래스의 인스턴스에서 clone을 호출하면 그 객체의 필드들을 하나하나 복사한 객체를 반환하며, 그렇지 않은 클래스의 인스턴스에서 호출하면 CloneNotSupportedException을 던짐.
- clone 메서드가 생성자를 호출해 얻은 인스턴스를 반환해도 컴파일러는 문제없으나 super.clone을 호출한다면 잘못된 클래스의 객체가 만들어져, 결국 하위 클래스의 clone 메서드가 제대로 동작하지 않게 됨.
    - clone을 재정의한 클래스가 final이라면 걱정해야 할 하위 클래스가 없으니 무시해도 안전함.
    - 하지만 final 클래스의 clone 메서드가 super.clone을 호출하지 않는다면 Cloneable을 구현할 이유도 없음.

- ****가변 상태를 참조하는 클래스용 clone 메서드****
    
    ```java
    @Override 
    public Stack clone() {
        try {
             Stack result = (Stack) super.clone();
             result.elements = elements.clone();
             return result;
        } catch (ClassNotSupportedException e){
            throw new AssertionError();
        }
    }
    ```
    
    - 배열의 clone은 런타임 타입과 컴파일타임 타입 모두가 원본 배열과 똑같은 배열을 반환함.
        - 따라서 **배열을 복제할 때는 배열의 clone 메서드를 사용하라고 권장**함.
        - 사실, 배열은 clone 기능을 제대로 사용하는 유일한 예라 할 수 있음.
    - final 필드에는 새로운 값을 할당할 수 없기 때문에 elements 필드가 final이었다면 앞서의 방식은 작동하지 않음.
        - 이는 근본적인 문제로, 직렬화와 마찬가지로 Cloneable 아키텍처는 '가변 객체를 참조하는 필드는 final로 선언하라'는 일반용법과 충돌함.

- Cloneable
    - Cloneable을 구현한 스레드 안전 클래스를 작성할 때는 clone 메서드 역시 적절히 동기화해줘야 함.
    - Cloneable을 구현하는 모든 클래스는 clone을 재정의해야 함.
        - 이때 접근 제한자는 public으로, 반환 타입은 클래스 자신으로 변경함.
        - 이 메서드는 가장 먼저 super.clone을 호출한 후 필요한 필드를 전부 적절히 수정함.
        - 이 말은 그 객체의 내부 '깊은 구조'에 숨어 있는 모든 가변 객체를 복사하고, 복사본이 가진 객체 참조 모두가 복사된 객체들을 가리키게 함을 뜻함.
            - 이러한 내부 복사는 주로 clone을 재귀적으로 호출해 구현하지만, 이 방식이 항상 최선인 것은 아님.
        - 기본 타입 필드와 불변 객체 참조만 갖는 클래스라면 아무 필드도 수정할 필요가 없음.
    

## ****정리****

- Cloneable이 몰고 온 모든 문제를 되짚어 봤을 때, 새로운 인터페이스를 만들 때는 **절대 Cloneable을 확장해서는 안 되며**, **새로운 클래스도 이를 구현해서는 안됨.**
- final 클래스라면 Cloneable을 구현해도 위험이 크지 않지만, 성능 최적화 관점에서 검토한 후 별다른 문제가 없을 때만 드물게 허용해야 함.
- **복제 기능은 생성자와 팩터리를 이용하는 게 제일 좋음.**
- 단, 배열만은 clone 메서드 방식이 가장 깔끔한, 이 규칙의 합당한 예외라 할 수 있음.

<br>

# 아이템 14. Comparable을 구현할지 고려하라

## compareTo

- 알파벳, 숫자, 연대 같이 순서가 명확한 값 클래스를 작성한다면 반드시 Comparable 인터페이스를 구현해야 함.
    
    ```java
    public interface Comparable<T> {
        int compareTo(T t);
    }
    ```
    
- compareTo 규약
    - 첫 번째 규약은 두 객체 참조의 순서를 바꿔 비교해도 예상한 결과가 나와야 한다는 것.
    - 두 번째 규약은 첫 번째가 두 번째보다 크고 두 번째가 세 번째보다 크면, 첫 번째는 세 번째보다 커야 한다는 뜻.
    - 세 번째 규약은 크기가 같은 객체들끼리는 어떤 객체와 비교하더라도 항상 같아야 한다는 뜻.
- 이상의 세 규약은 compareTo 메서드로 수행하는 동치성 검사도 equals 규약과 똑같이 **반사성, 대칭성, 추이성을 충족**해야 함을 뜻함.

## compareTo 메서드 작성 요령

- Comparable은 타입을 인수로 받는 제네릭 인터페이스이므로 compareTo 메서드의 인수타입은 컴파일 타임에 정해짐.
    - 입력 인수의 타입을 확인하거나 형변환할 필요가 없다는 의미.
- compareTo 메서드는 각 필드가 동치인지를 비교하는 게 아니라 그 순서를 비교함.
- 객체 참조 필드를 비교하려면 compareTo 메서드를 재귀적으로 호출함.
- 기본 타입 필드가 여럿일 때의 비교자
    
    ```java
    public int compareTo(PhoneNumber pn) {
        int result = Short.compare(areaCode, pn.areaCode);  // 가장 중요한 필드
        if (result == 0) {
            result = Short.compare(prefix, pn.prefix);  // 두 번째로 중요한 필드
            if (result == 0)
                result = Short.compare(lineNum, pn.lineNum) // 세 번째로 중요한 필드
        }
        return result;
    }
    ```
    
    - 클래스 핵심 필드가 여러 개라면 가장 핵심적인 필드부터 비교해나감.
    - 비교 결과가 0이 아니라면 거기서 끝이고, 그 결과를 곧장 반환함.
    - 가장 핵심이 되는 필드가 똑같다면, 똑같지 않은 필드를 찾을 때까지 그 다음으로 중요한 필드를 비교해나감.
- 방법 1 - 정적 compare 메서드를 활용한 비교자
    
    ```java
    static Comparator<Object> hashCodeOrder = new Comparator<>() {
        public int compare(Object o1, Object o2) {
            return Integer.compare(o1.hashCode, o2.hashCode());
        }
    }
    ```
    
- 방법 2 - 비교자 생성 메서드를 활용한 비교자
    
    ```java
    static Comparator<Object> hashCodeOrder = Comparator.comparingInt(o-> o.hashCode());
    ```
    

## 정리

- 순서를 고려해야 하는 값 클래스를 작성한다면 꼭 Compareble 인터페이스를 구현하여, 그 인스턴스들을 쉽게 정렬하고, 검색하고, 비교 기능을 제공하는 컬렉션과 어우러지도록 해야함.
- compareTo 메서드에서 필드 값을 비교할 때 <와 >연산자는 쓰지 말고 그 대신 박싱된 기본 타입 클래스가 제공하는 정적 compare 메서드나 Comparator 인터페이스가 제공하는 비교자 생성 메서드를 사용해야 함.

<br>

# 아이템 15. 클래스와 멤버의 접근 권한을 최소화하라

- 잘 설계된 컴포넌트는 잘 설계된 컴포넌트는 모든 내부 구현을 완벽히 숨겨, 구현과 API를 깔끔하게 분리함.

## **정보 은닉의 장점**

- **시스템 개발 속도를 높임.**
    - 여러 컴포넌트를 병렬로 개발할 수 있기 때문.
- **시스템 관리 비용을 낮춤.**
    - 각 컴포넌트를 더 빨리 파악하여 디버깅할 수 있고, 다른 컴포넌트에 영향을 주지 않고 해당 컴포넌트만 최적화할 수 있기 때문.
- **소프트웨어 재사용성을 높임.**
    - 외부에 거의 의존하지 않고 독자적으로 동작할 수 있는 컴포넌트라면 그 컴포넌트와 함께 개발되지 않은 낯선 환경에서도 유용하게 쓰일 가능성이 크기 때문.
- **성능 최적화에 도움을 줌.**
    - 완성된 시스템을 프로파일링해 최적화할 컴포넌트를 정한 다음, 다른 컴포넌트에 영향을 주지 않고 해당 컴포넌트만 최적화할 수 있기 때문.
- **소프트웨어 재사용성을 높임.**
    - 외부에 거의 의존하지 않고 독자적으로 동작할 수 있는 컴포넌트라면 그 컴포넌트와 함께 개발되지 않은 낯선 환경에서도 유용하게 쓰일 가능성이 크기 때문.
- **큰 시스템을 제작하는 난이도를 낮춰줌.**
    - 시스템 전체가 아직 완성되지 않은 상태에서도 개별 컴포넌트의 동작을 검증할 수 있기 때문.

- 자바에서 **접근 제한자**를 활용하는 것이 정보은닉의 핵심임.
    - **모든 클래스와 멤버의 접근성을 가능한 한 좁혀야 함.**
    - public일 필요가 없는 클래스의 접근 수준을 package-private 톱레벨 클래스로 좁히는 일이 중요함.

## 접근 범위

- private : 멤버를 선언한 톱 레벨 클래스에서만 접근할 수 있음.
- package-private : 멤버가 소속된 패키지 안의 모든 클래스에서 접근할 수 있음.
- protected : package-private의 접근 범위를 포함하며 이 멤버를 선언한 클래스의 하위 클래스에서도 접근할 수 있음.
- public : 모든 곳에서 접근할 수 있음.
- 접근 범위 설정
    1. 클래스의 공개 API외의 모든 멤버는 private로 만듦.
    2. 오직 같은 패키지의 다른 클래스가 접근해야 하는 멤버에 한하여 package-private으로 풀어줌.

## **멤버 접근성 제약**

- 상위 클래스의 메서드를 재정의할 때는 그 접근 수준을 **상위 클래스에서보다 좁게 설정할 수 없음.**
    - 이 제약은 상위 클래스의 인스턴스는 하위 클래스의 인스턴스로 대체해 사용할 수 있어야 한다는 규칙(리스코프 치환 원칙)을 지키기 위해 필요함.

## **public 클래스와 인스턴스 필드**

- **public 클래스의 인스턴스 필**드가 가변 객체를 참조하거나, final이 아닌 인스턴스 필드를 public으로 선언하면 그 필드에 담을 수 있는 값을 제한할 힘을 잃게 된다.
- 필드가 수정될 때 (락 획득 같은) 다른 작업을 할 수 없게 되므로 public 가변 필드를 갖는 클래스는 일반적으로 **스레드 안전하지 않음.**
- 추상 개념을 완성하는 데 꼭 필요한 구성요소로써의 상수라면 public static final 필드로 공개해도 좋음.
    - 이런 필드는 반드시 기본 타입 값이나 불변 객체를 참조해야 함.
    - 가변 객체를 참조하면 final이 아닌 필드에 적용되는 모든 불이익이 그대로 적용됨.

## **배열**

- 클래스에서 public static final 배열 필드를 두거나 이 필드를 반환하는 접근자 메서드를 제공하면 안됨.
    - 이런 필드나 접근자를 제공한다면 클라이언트에서 그 배열의 내용을 수정할 수 있게 됨.
    
    ```java
    // 보안 허점이 숨어 있다.
    public static final Thing[] VALUES = {...};
    ```
    
    - 첫 번째 해결 방법은 앞 코드의 public 배열을 private으로 만들고 public 불변 리스트를 추가하는 것.
        
        ```java
        private static final Thing[] PRIVATE_VALUES = { ... };
        public static final List<Thing> VALUES = Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
        ```
        
    - 두 번째 해결 방법은 배열을 private으로 만들고 그 복사본을 반환하는 public 메서드를 추가하는 방법(방어적 복사).
        
        ```java
        private static final Thing[] PRIVATE_VALUES = { ... };
        public static final Thing[] values() {
            return PRIVATE_VALUES.clone();
        }
        ```
        

## 정리

- 프로그램 요소의 접근성은 가능한 한 **최소한**으로 하라.
- 꼭 필요한 것만 골라 최소한의 public API를 설계하자.
- public 클래스는 상수용 public static final 필드 외에는 어떠한 public 필드도 가져서는 안됨.
