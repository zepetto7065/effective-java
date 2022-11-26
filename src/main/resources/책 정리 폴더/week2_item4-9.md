## 2주차 - 박광태

### 아이템 4 - 인스턴스화를 막으려거든 private 생성자를 사용하라

- java.lang.Math 나 java.util.Arrays 처럼 객체지향에는 맞지 않지만 정적 메서드나 정적 필드만을 담은 클래스를 만들 때가 있다.
- 이럴 때 private 생성자를 생성해놓지 않으면 컴파일러가 자동으로 public 기본생성자를 만드릭 때문에 의도하지 않은 현상이 발생할 수 있다.

```java
private class UtilityClass {
    // 인스턴스화 방지용 private 생성자
    private UtiltyClass() {
        ...
    }
}
```

- 생성자가 존재하는데 호출할 수 없는 구조라 보기에 좋지는 않으므로 주석을 잘 달아주ㅏ

### 아이템 5 - 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

- 잘못된 예제들

```java
/***
 * 정적 유틸리티를 잘못 사용한 예 - 유연하지 않고 테스트하기 어려움
 * SpellChecker spellChecker = new SpellChecker() 로 만들어서 쓰면 무조건 한국어 사전으로만 사용 가능해 유연하지 못하다
 */
public class SpellChecker {
    private static final Korean dictionary = ...;
    
    private SpellChecker() {} // 객체 생성자
    
    public static boolean isValid(String word) { ... }
    public static List<String> suggestions(String type) { ... }
}
```

```java
/***
 * 싱글턴을 잘못 사용한 예
 * 마찬가지로 SpellChecker.INSTANCE를 해도 private 생성자에 설정된 고정된 객체로 생성된다.
 */
public class SpellChecker {
    private static final Korean dictionary = ...;

    private SpellChecker(...) {} 
    public static SpellChecker INSTANCE = new SpellChecker(...);

    public static boolean isValid(String word) { ... }
    public static List<String> suggestions(String type) { ... }
}
```

- 위와 같이 맞춤법 검사기를 쓰면 여러가지 맞춤법 검사를 하지 못하고 정해진 하나의 사전 맞춤법 검사만 가능해서 유연하지 못함
- 이쁘게 만들려면 맞춤법 검사기가 여러개의 사전을 사용할 수 있도록 변경해야 한다
- 제일 쉬운 방법으로는 final을 빼버리고 사전을 교체하는 메서드를 넣으면 된다 -> setter처럼
- 근데 오류날 가능성이 있고 멀티쓰레드 환경에 적합하지 않다.
- 결국 사용하는 자원에 따라 (여기선 사전) 동작이 달라지는 클래스의 경우에는 `final`을 사용하는 정적 유틸클래스나 싱글턴이 적합하지 않다.

<hr/>

- 이런 경우엔 생성자로 맞춤법 검사기를 생성할 때 생성자에 필요한 자원을 넘겨주는 방식으로 구성해야 한다. -> 흔히 말하는 DI 생성자 주입 방식

```java
/**
 * 생성자를 만들 때 사전을 주입한다.
 * 예제에선 위의 방식과 동일하게 Lexicon이란 사전을 주입받게 돼있는데 좀 포괄적인 인터페이스나 추상클래스를 주입받는게 더 맞는거 같음 -> Language 타입 dictionary
 */
public class SpellChecker() {
    private final Language dictionary;
    
    public SpellChecker(Language dictionary) {
        this.dictionary = dictionary;
    }

    public boolean isValid(String word) { ... }
    public List<String> suggestions(String type) { ... }
}
```

- 위의 패턴은 많이 사용하고, 심지어 불변이라 안정적이기 까지하다. -> 스프링에서 생성자 주입을 추천하는 이유기도 함
- 의존 객체 주입 방식은 코드가 굉장히 유연해지고 테스트할 때도 용의하지만, 프로젝트 사이즈가 커지면 파악하기 어려운 단점도 있다.
- 이런 의존관계를 직접 관리하면 어려우니까 쓰는게 스프링 같은 프레임워크

### 아이템 6 - 불필요한 객체 생성을 피하라

- 똑같은 기능의 객체를 매번 생성하는거 보단 객체 하나를 재사용하는게 보통 낫다.
- 아래는 안좋은 객체 생성 예시

```java
Stirng s = new String("이펙티브 자바");
```

- 이렇게 만들면 매번 스트링 객체를 새로만든다

```java
String s = "이펙티브 자바";
```

- 이렇게 만들면 매번 인스턴스를 만드는 대신 하나의 String 인스턴스를 사용한다.
- 생성자 대신 정적 팩토리 메소드를 제공하는 불변클래스는 정적 팩토리 메소드를 사용한다.

```java
Integer.valueOf(123);
```

- 생성자는 매번 새로운 객체를 만들지만 팩토리 메소드는 그렇지 않음
- 특히 생성비용이 비싼 객체들이 있는데, 이런 애들은 캐싱해서 재사용 해야 한다.

```java
static boolean isRomanNumberal(String s) {
    return s.matches("^(.... 정규식 ... ");
}
```

- 위 예제는 String.matches를 쓰는데, 반복해서 사용하면 성능이 떨어진다.
- 이런 경우 정규표현식용 Patterns 인스턴스를 사용해서 캐싱한다

```java
import java.util.regex.Pattern;

public class RomanNumerals {
    private static final Pattern ROMAN = Pattern.compile(
            ... 정규식
    );
    
    static booelan isRomanNumeral(String s) {
        return ROMAN.matcher(s).matches();
    }
}
```

- 이렇게 짜면 의미도 명확해지고 성능도 향상된다
- 만약 isRomanNumeral이란 메소드가 생성후에 한번도 호출을 안하면 안쓰는 객체를 무조건 초기화하는 낭비가 발생할 수 있다.
- 이럴 때 지연 초기화를 쓸수도 있는데 복잡해서 추천하진 않는다

<hr />

- 객체가 불변이면 재사용 해도 안전하긴 한데 덜 명확하거나 비 직관적이게 될 수도 있다.
- 예를 들어 Map의 keySet 메소드는 Map 객체 안의 키를 담은 Set뷰를 반환한다.
- 매번 같은 Set 인스턴스를 반환하게 되는데 결국 같은 Set을 반환하므로 하나를 바꾸면 다 바뀐다.

<hr /> 

- 그렇다고 객체를 생성하는게 무조건 비싸니 매번 재사용가능한 불변객체를 쓰자 라는건 아님
- 요즘은 컴퓨터 성능도 좋고 JVM에서 가비지 컬렉팅을 통해 안쓰는건 날려주고 그런다
- 오히려 너무 무겁지 않은 객체들을 pool로 관리하면 코드만 복잡해지고 메모리 사용량을 늘리기도 한다.
- 적절히 잘 사용하자

### 아이템 7 - 다 쓴 객체 참조를 해제하라

- 보통 자바는 GC가 있어 C나 C++처럼 직접 메모리 해제를 해줄 필요가 없다. (malloc.. calloc...)
- 근데 그렇다고 마구잡이로 메모리 관리를 신경쓰지 말라는건 아님

```java
import java.util.Arrays;
import java.util.EmptyStackException;

public class Stack {
    private Object[] elements;
    private int size = 0;

    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        return elements[--size];
    }

    /**
     * 원소를 위한 공간을 적어도 하나 이상 확보
     * 배열 크기를 늘려야 할 때마다 대략 두 배씩 늘린다
     */
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2*size + 1);
        }
    }
}
```

- 위의 스택 코드는 잘 작동하긴 한다.
- 그런데 많이 사용하다보면 메모리 누수로 프로그램이 터져버릴 수도 있음
- 스택이 커졌다 줄어졌다 할 때 꺼내진 객체들을 GC에서 처리하질 않는다.
- 실제로 그 객체들을 사용하진 않는데 다 쓴 참조를 여전히 갖고 있는다. -> 자세한건 GC를 보자
- 이런식으로 의도치 않게 성능이 안좋아 질 수 있다
- 처리 방법은 간단하게 null처리를 해주면 됨

```java
public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        Object result = elements[--size];
        elements[size] = null; // 참조 해제
        return result;
    }
```

- 근데 당연히 이런 구조는 보기도 앉좋고 너무 지저분해진다
- 다 쓴 참조를 해제하는 가장 좋은 방법은 그 참조를 담은 변수를 유효범위 밖으로 밀어내는 것
- null처리가 필요한 경우는 위에 처럼 자기 메모리를 직접 관리하는 경우.
- 객체 자체가 아닌 객체 참조를 담는 elements 배열을 저장소 풀로 만들어서 생기는 문제 (그럼 객체로 만들어서 관리하면 될까? 하나의 객체가 다른 객체를 참조해서 GC가 자동으로 처리해 주는지 확인이 필요할듯. 헷갈린다)
- 캐시 같은 경우도 비슷한 문제를 발생시킴
- 결국 자기 메모리를 직접 관리하는 클래스라면 메모리 누수를 조심해야 한다.

### 아이템 8 - finalizer와 cleaner 사용을 피하라

- 자바는 두 가지 객체 소멸자를 제공한다
- finalizer는 예측이 안되고 상황에 따라 위험해서 보통 불필요함
- 기본적으론 쓰면안됨
- cleaner는 좀 덜 위험한대 그래도 쓰면 안됨
- 전반적인 설명은 썼을 때의 위험함을 알려줌
- 간단히 읽고 넘어가면 될 것 같다

### 아이템 9 - try-finally 보다는 try-with-resources를 사용하라

- 자바 라이브러리엔 close를 직접 해줘야 되는 라이브러리들이 많음

```java
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine();    
    } finally {
        br.close();    
    }
}
```

- 보통 이렇게들 많이 사용해 왔는데 닫아야 할 게 여러개면 계속 finally가 추가 되는 문제가 생긴다.
- 그리고 실제로 자바 라이브러리들에서 close를 안하는 경우도 아주 많다. 
- 이런 문제를 해결하려고 나온게 `try-with resources`
- `try-with resources`를 사용하려면 AuthoClosable을 구현해야 한다

```java
public interface Closeable extends AutoCloseable {
    ...
}
```

- 이런식으로
- 이제 위의 코드를 바꿔보면

```java
static String firstLineOfFile(String path) throws IOException {
    try(BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine();    
    } 
}
```

- 읽기도 편하고 안전하게 닫힘도 보장한다