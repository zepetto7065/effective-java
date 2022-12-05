## item19. 상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라

- 상속용 클래스는 재정의할 수 있는 메소드들을 내붲ㄱ으로 어떻게 이용하는지 문서로 남겨야 한다
- 호출되는 메소드들이 어떤 순서로 호출되는지, 각각의 호출 결과로 얻어지는 결과 등을 적어놔야 한다
- @implSpec 태그를 사용하면 자바독 도구가 생성해줌
- protected로 공개를 잘 선별해서 해야한다
- 상속용 클래스를 결국 잘 만들어야 하는데, 시험하는 방법은 직접 하위 클래스를 만들어보는 방법이 `유일`하다
- 3개정도 만들면 좋다고 추천함
- 또 다른 제약 조건으로는 상속용 클래스의 생성자는 직접적이든 간접적이든 재정의 가능 메소드를 호출해서는 안된다.

```java
import java.time.Instant;

public class Super {
    // 재정의 가능 메소드를 생성자에서 호출
    public Super() {
        overrideMe();
    }

    public void overrideMe() {
        
    }
}

public final class Sub extends Super {
    private final Instant instant;

    Sub() {
        instant = Instant.now();
    }
    
    @Override
    public void overrideMe() {
        System.out.println(instant);
    }

    public static void main(String[] args) {
        Sub sub = new Sub();
        sub.overrideMe();
    }
}

출력 결과 

null 2022-12-05T08:00:33.807Z
```

- 상위 클래스 생성자에서 Sub 객체내 필드인 instant 객체가 생성이 안 된상태로 무조건 overrideMe()를 호출해서 null이 한번 호출됨

<hr />

- Cloneable과 Serializable 은 상속하지 않는 것을 추천
- clone이나 readResolve나 writeReplace 등을 재정의 하는건 위험이 많고 예상하지 못한 오작동을 초래한다

<hr />

- 이런식으로 상속은 매우 까다롭다
- 일반적인 구체 클래스를 잘못 상속하면 문제가 발생할 수 있다
- 이걸 해결할 가장 좋은 방법은 상속용으로 설계하지 않은 클래스는 상속을 금지하는 것
- 두 가지 방법이 있다
  - 클래스를 final로 선언
  - 모든 생성자를 private이나 protected로 선언하고 public 정적 팩토리로 생성
- 근데 이 방법은 문제가 있을 수도 있는게 이미 기존 코드들이 마구 상속을 해왔음
- 결국 상속을 허용할 거면 클래스 내부에는 재정의 기능 메소드를 사용하지 않게 만들고 문서로 남겨야 한다
- 재정의 가능 메소드를 호출하는 자기 사용 코드를 완벽히 제거하면 된다

## item20. 추상 클래스보다는 인터페이스를 우선하라

- 자바에는 추상 클래스와 인터페이스가 있다
- 둘의 차이는 추상 클래스가 정의한 타입을 구현하는 클래스는 반드시 추상 클래스의 하위 클래스가 되어야 한다는 점
- 근데 추상클래스는 하나만 상속이 가능해서 사용에 제한이 생김
- 기존 클래스에서도 손쉽게 인터페이스 추가가 가능
- 구현 안된 인터페이스 정의 메소드들을 구현하면 된다
- 근데 추상클래스는 추가하기가 어려움
- 인터페이스는 믹스인 정의에 좋다
- 예를들어 Comparable 같은 인터페이스는 자신을 구현한 클래스의 인스턴스끼리는 순서를 정할 수 있다고 선언한 믹스인 인터페이스 인데, 이런 식으로 대상 타입의 주된 기능에 선택적 기능을 혼합한다고 해서 믹스인이라 부름
- 즉 기능을 섞어서 넣기 좋다는 말
- 추상 클래스는 역시 이게 안됨

<hr />

- 인터페이스로는 계층구조가 없는 타입 프레임워크를 만들 수 있다
- 실제로 현실을 정의하다 보면 계층을 구분하기 애매한 경우들이 많다

```java
import java.applet.AudioClip;

public interface Singer {
    AudioClip sing(Song s);
}

public interface SongWriter {
    Song compose(int chartPosition);
}

public interface SingerSongWriter extends Singer, SongWriter {
    AudioClip strum();
    void actSensitive();
}
```

- 예를 들어 작곡하는 가수들이 많은데, 이런식으로 인터페이스로 정의하면 가수 클래스가 Singer와 SongWriter 모두를 구현해도 전혀 문제되지 않는다.
- 즉 가수이면서 작곡가면 두 개의 계층이 같다.
- 인터페이스는 이런게 가능함
- 굉장히 유연하고 유용하다
- 인터페이스는 기능을 향상시키는 가장 안전하고 강력한 수단임
- 추상 클래스는 상속으로만 가능하기 떄문에 활용도도 낮고 꺠지기도 쉬움

<hr />

- 인터페이스 메소드 중 구현 방법이 명백한 구현이 있으면 default 메소드로 제공할 수 있다.
- 디폴트 메소드를 제공할 때는 @implSpec 자바독 태그를 붙여 문서화 해주는게 좋다
- 당연히 제약도 있는데, equals나 hashCode 같은건 디폴트 메소드로 만들면 안된다
- 또 인터페이스는 인스턴스 필드를 가질 수 없고 public이 아닌 정적 메소드 멤버도 가질 수 없다.
- 또 내가 만든게 아니면 디폴트 메소드 추가가 안된다

<hr />

- 인터페이스와 추상 골격 구현 클래스를 함께 제공해 인터페이스와 추상 클래스의 장점을 모두 취하는 방법도 있음
- 인터페이스로는 타입을 정의하고, 필요하면 디폴트 메소드 몇 개도 함께 제공한다
- 템플릿 메소드 패턴이 이런 방법
- 관례상 인터페이스 이름이 `Interface` 라고 하면 클래스 이름은 `AbstractInterface`로 짓는다.
- Collection 프레임워크의 AbstractCollection 같은게 좋은 예

```java
static List<Integer> intArrayAsList(int[] a) {
    Objects.requireNonNull(a);
    
    return new AbstractList<>() {
        @Override 
        public Integer get(int i) {
            return a[i];
        }
        
        ... 생략
    }
}
```

- 이렇게 어떤걸 미리 구현해야 할지 골격 구현을 활용해 구체화 할 수 있다.
- 골격 구현의 아름다움은 추상 클래스처럼 구현을 도와주면서 추상클래스로 타입을 정의할 때 따라오는 귀찮은 제약은 피할 수 있다는 점이다
- 골격 구현은 기본적으로 상속해서 사용하는걸 가정하므로 아이템 19에서 이야기한 설계 및 문서화 지침을 모두 따라야 한다.

## item 21. 인터페이스는 구현하는 쪽을 생각해 설계하라

- 자바 8 전에는 기존 구현체를 깨뜨리지 않고는 인터페이스에 메소드를 추가 할 수 없었다.
- 인터페이스에 메소드를 추가하면 보통 컴파일 오류가 나는데, 추가된 메소드가 우연히 기존 구현체에 이미 존재할 가능성이 아주 낮기 떄문 (근데 있어도 @Override 없으면 에러나지 않나..)
- 디폴트 메소드가 추가되긴 했지만 이러한 위험이 완전히 사라진건 아님
- 디폴트 메소드를 선언하면, 그 인터페이스를 구현한 후 디폴트 메소드를 재정의 하지 않은 모든 클래스에서 디폴트 구현이 쓰여버림
- 인터페이스에 메소드 추가가 비교적 자유로워졌지만 매끄럽게 연동된다는 보장은 없다
- 자바 8에서 컬렉션 인터페이스들에 람다를 위해 다수의 디폴트 메소드가 추가됨
- 대부분 잘 작동하긴 하지만 언제 어떤 문제가 터질지 모른다
- Collection에 추가된 removeIf 예시

```java
default boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    boolean removed = false;
    final Iterator<E> each = iterator();
    while (each.hasNext()) {
        if (filter.test(each.next())) {
            each.remove();
            removed = true;
        }
    }
    return removed;
}
```

- 대부분은 잘 작동하지만, 아파치 커먼에서 만든 SynchronizedCollection에선 문제가 발생한다. (동기화 락 문제)
- 이렇듯 많은 사람들이 사용하는 라이브러리가 기존의 Collection을 구현했으면, 해당 라이브러리는 디폴트 메소드때매 문제가 발생할 수 있다
- 디폴트 메소드는 컴파일 오류가 발생하지 않아도 런타임 오류가 발생할 수 있다.
- 기존 인터페이스에 디폴트 메소드를 추가하는건 조심해야 한다

## item 22. 인터페이스는 타입을 정의하는 용도로만 사용하라

- 인터페이스는 자신을 구현한 클래스의 인스턴스를 참조할 수 있는 타입 역할을 한다.
- 즉 어떤 클래스가 인터페이스를 구현한다는 것은 자신의 인스턴스로 무엇을 할 수 있는지를 클라이언트에게 알려주는 것
- 상수 인터페이스 같은 static final 로만 가득한 인터페이스는 안티패턴이다

```java
public interface PhysicalConstants {
    static final double AVOGADROS_NUMBERS = 6.022...;
}
```

- 클래스 내부에서 사용하는 상수는 외부 인터페이스가 아니라 내부 구현에 해당된다.
- 이런걸 인터페이스로 노출해버리는건 아주 별로다
- 상수를 공개할 목적이면 합당한 선택지가 있는데, 특정 클래스나 인터페이스와 강하게 연관된 상수인 경우엔 클래스나 인터페이스에 추가해야 한다.
- Integer.MAX_VALUE 같은거
- 이런거도 아니면 그냥 유틸리티 클래스로 생성하자

```java
public class PhysicalConstants {
    private PhysicalConstants() { } // 인스턴스화 방지 프라이빗 생성자

    static final double AVOGADROS_NUMBERS = 6.022...;
}
```