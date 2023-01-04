# 아이템 31. 한정적 와일드카드를 사용해 API 유연성을 높이라

- 매개변수화 타입은 **불공변(invariant)**임. 즉, 서로 다른 타입 Type1과 Type2가 있을 때 List<Type1>은 List<Type2>의 하위 타입도 상위 타입도 아님.
    - List<String>은 List<Object>의 하위 타입도 상위 타입도 아니라는 의미.

- 예제 - 와일드카드 타입을 사용하지 않은 pushAll 메서드 - 결함이 있음.
    
    ```java
    public class Stack<E> {
        public Stack();
        public void push(E e);
        public E pop();
        public boolean isEmpty();
    }
    
    // Stack 클래스에 일련의 원소를 스택에 넣는 메서드 추가
    public void pushAll(Iterable<E> src) {
        for (E e : src)
            push(e);
    }
    ```
    
    - Iterable src의 원소 타입이 스택의 원소 타입과 일치하면 잘 작동함.
    - 하지만 Strack<Number>로 선언한 후 pushAll(intVal)을 호출하면 Integer는 Number의 하위 타입이니 잘 동작해야 할 것 같지만 오류가 발생함.
        
        ```java
        Strack<Number> numberStack = new Stack<>();
        Iterable<Integer> intergers = ...;
        numberStack.pushAll(integers);
        
        // 오류 메세지
        StackTest.java:7: error: incompatible types: Iterable<Integer>
        cannot be converted to Iterable<Number>
            numberStack.pushAll(integers);
                                ^
        ```
        
        - 매개변수화 타입이 불공변이기 때문.
    - 자바는 이런 상황에 대처할 수 있는 **한정적 와일드카드 타입**을 지원함.
        - **E 생산자 매개변수에 와일드카드 타입 적용**
            
            ```java
            public void pushAll(**Iterable<? extends E>** src) {
                for (E e : src)
                    push(e);
            }
            ```
            
            - 이렇게 수정하면 말끔히 컴파일 됨.
        
- 예제 - 와일드카드 타입을 사용하지 않은 popAll 메서드 - 결함이 있음.
    
    ```java
    // popAll 메서드는 Stack 안의 모든 원소를 주어진 컬렉션으로 옮겨 담음.
    public void popAll(Collection<? super E> dst) {
        while (!isEmpty())
        dst.add(pop())
    }
    ```
    
    - Strack<Number>의 원소를 Object용 컬렉션으로 옮기려 한다고 해보면 컴파일과 동작 모두 문제가 없을 것 같지만 오류가 발생함.
        
        ```java
        Strack<Number> numberStack = new Stack<>();
        Iterable<Object> objects = ...;
        numberStack.pushAll(objects);
        ```
        
        - 이 클라이언트 코드를 앞의 popAll 코드와 함께 컴파일하면 "Collection<Object>는 Collection<Number>의 하위 타입이 아니다"라는 pushAll을 사용했을 때와 비슷한 오류가 발생함.
        - **E 소비자 매개변수에 와일드카드 타입 적용**
            
            ```java
            public void popAll(Collection<? super E> dst) {
                when (!isEmpty())
                dst.add(pop())
            }
            ```
            
            - 이제 Stack과 클라이언트 코드 모두 말끔히 컴파일됨.

- **유연성을 극대화하려면 원소의 생산자나 소비자용 입력 매개변수에 와일드카드 타입을 사용하라.**
- 한편, 입력 매개변수가 생산자와 소비자 역할을 동시에 한다면 와일드카드 타입을 써도 좋을 게 없음.
    - 타입을 정확히 지정해야 하는 상황으로, 이때는 와일드카드 타입을 쓰지 말아야 함.

### 팩스(PECS)

- `팩스(PECS) : producer-extends, consumer-super`
- 매개변수화 타입 T가 **생산자라면 <? extends T>**를 사용.
- 매개변수화 타입 T가 **소비자라면 <? super T>**를 사용.
- Stack 예
    - pushAll은 Stack이 사용할 E 인스턴스를 생산하고 popAll은 Stack이 E 인스턴스를 소비하는 쪽에 속함.
    - pushAll - Iterable<? extends E>
    - popAll - Collection<? super E>
    - PECS 공식은 와일드카드 타입을 사용하는 기본원칙

- Chooser 생성자
    
    ```java
    public Chooser(Collections<T> choices)
    
    ⬇︎ 수정
    
    public Chooser(Collection<? extends T> choices)
    ```
    
    - 이 생성자로 넘겨지는 choices 컬렉션은 T 타입의 값을 생산하기만 하니, T를 확장하는 와일드카드 타입을 사용해 선언해야 함.
    - 이렇게 수정하면 생기는 실질적인 차이
        - Chooser의 생성자에 List를 넘기고 싶을 때, 수정 전 생성자로는 컴파일조차 되지 않겠지만, 한정적 와일드카드 타입으로 선언한 수정 후 생성자에서는 문제가 사라짐.

- 예제 - union 메서드
    
    ```java
    public static <E> Set<E> union(Set<E> s1, Set<E> s2)
    
    ⬇︎ 수정
    
    public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2)
    ```
    
    - s1과 s2 모두 E의 생산자이니 PECS 공식에 따라 선언해야 함.
    - 여기서 반환 타입은 여전히 임에 주목.
        - **반환 타입에는 한정적 와일드카드 타입을 사용하면 안됨.**
            - 유연성을 높여주기는 커녕 클라이언트 코드에서도 와일드카드 타입을 써야하기 때문.
    - 수정한 선언을 사용하면 다음 코드도 말끔히 컴파일됨.
        
        ```java
        Set<Integer> integers = Set.of(1, 3, 5);
        Set<Double> doubles = Set.of(2.0, 4.0, 6.0);
        Set<Number> numbers = union(integers, doubles);
        ```
        

### 타입 매개변수와 와일드카드

- 타입 매개변수와 와일드카드에는 공통되는 부분이 있어서, 메서드를 정의할 때 둘 중 어느 것을 사용해도 괜찮을 때가 많음.
    - 다음 코드에서는 첫 번째는 비한정적 타입 매개변수를 사용했고 두 번째는 비한정적 와일드카드를 사용함.
        
        ```java
        public static <E> void swap(List<E> list, int i, int j);
        public static void swap(List<?> list, int i, int j);
        ```
        
        - **메서드 선언에 타입 매개변수가 한 번만 나오면 와일드카드로 대체하라.**
            - 이때 비한정적 타입 매개변수라면 비한정적 와일드카드로 바꾸고, 한정적 타입 매개변수라면 한정적 와일드카드로 바꾸면 됨.

### 정리

- 조금 복잡하더라도 와일드카드 타입을 적용하면 API가 훨씬 유연해짐.
- 널리 쓰일 라이브러리를 작성한다면 반드시 와일드카드 타입을 적절히 사용해줘야 함.
- PECS 공식
    - 생산자(producer)는 extends를 소비자(consumer)는 super를 사용.

<br>

# 아이템 32. 제네릭과 가변인수를 함께 쓸 때는 신중하라

### 가변인수(varargs) 메서드와 제네릭

- 가변인수 메서드를 호출하면 가변인수를 담기 위한 배열이 자동으로 하나 만들어짐.
    - 이 방식은 내부로 감춰야 했을 배열이 클라이언트에 노출되는 것이 문제.
    - 그 결과 **가변인수 매개변수에 제네릭이나 매개변수화 타입이 포함되면 알기 어려운 컴파일 경고가 발생**함.
- 거의 모든 제네릭과 매개변수화 타입은 실체화되지 않음.
    - 가변인수 메서드를 호출할 때도 실체화 불가 타입으로 추론되면, 그 호출에 대해서도 경고를 냄.
        - 경고 형태
            
            ```java
            warning: [unchecked] Possible heap pollution from
            parameterized vararg type List<String>
            ```
            
    - 매개변수화 타입의 변수가 다른 객체를 참조하면 힙 오염이 발생함.
- 제네릭과 가변인자를 혼용하면 타입 안정성이 깨짐.
    
    ```java
    static void dangerous(List<String>... stringLists) {
        List<Integer> intList = List.of(42);
        Object[] objets = stringLists;
        objects[0] = intList; // 힙 오염 발생
        String s = stringLists[0].get(0); // ClassCastException
    ```
    
    - 마지막 줄에 컴파일러가 생성한 (보이지 않는) 형변환이 숨어있기 때문에 ClassCastException 발생.
        - 이처럼 타입 안정성이 깨지게 됨으로 **제네릭 가변인자 배열 매개변수에 값을 저장하는 것은 안전하지 않음.**
- 자바 7에서는 @SafeVarargs 애너테이션이 추가되어 제네릭 가변인수 메서드 작성자가 클라이언트 측에서 발생하는 경고를 숨길 수 있게 됨.
    - @SafeVarargs 애너테이션은 메서드 작성자가 그 메서드가 타입 안전함을 보장하는 장치임.

- 가변인수로 넘어온 매개변수들을 배열에 담아 반환하는 제네릭 메서드
    
    ```java
    static <T> T[] toArray(T... args) {
        return args;
    }
    ```
    
    - 이 메서드가 반환하는 배열의 타입은 이 메서드에 인수를 넘기는 컴파일타임에 결정되는데, 그 시점에는 컴파일러에게 충분한 정보가 주어지지 않아 타입을 잘못 판단할 수 있음.
    - 따라서 자신의 가변인자 매개변수 배열을 그대로 반환하면 힙 오염을 이 메서드를 호출한 쪽의 콜스택으로까지 전이하는 결과를 낳을 수 있음.

- 제네릭 가변인자 매개변수 배열에 다른 메서드가 접근하도록 허용하며 안전하지 않는데, 단 예외가 두 가지 있음.
    1. @SafeVarargs로 제대로 애노테이트된 또 다른 가변인자 메서드에 넘기는 것은 안전함.
    2. 그저 이 배열 내용의 일부 함수를 호출만 하는 일반 메서드에 넘기는 것도 안전함.

- 제네릭 varargs 매개변수를 안전하게 사용하는 전형적인 예
    
    ```java
    @SafeVarargs
    static <T> List<T> flattern(List<? extends T>... lists) {
        List<T> result = new ArrayList<>();
        for (List<? extends T> list : lists)
            result.addAll(list);
        return result;
    }
    ```
    
    - flattern 메서드는 임의 개수의 리스트를 인수로 받아, 받은 순서대로 그 안의 모든 원소를 하나의 리스트로 옮겨 담아 반환함.
    - 이 메서드에는 @SafeVarargs 애너테이션이 달려 있으니 선언하는 쪽과 사용하는 쪽 모두에서 경고를 내지 않음.

- @SafeVarargs 애너테이션은 재정의할 수 없는 메서드에만 달아야 함.
    - 재정의한 메서드도 안전할지는 보장할 수 없기 때문.
    - 자바 8에서 이 애너테이션은 오직 정적 메서드와 final 인스턴스 메서드에만 붙일 수 있고, 자바 9부터는 private 인스턴스 메서드에도 허용됨.

### 정리

- 가변인수 기능은 배열을 노출하여 추상화가 완벽하지 못하고, 배열과 제네릭의 타입 규칙이 서로 다르기 때문.
- 제네릭 가변인자 매개변수는 타입 안전하지는 않지만 허용됨.
- 메서드에 제네릭 혹은 매개변수화된 가변인자 매개변수를 사용하고자 한다면, 타입 안전한지 확인한 다음 @SafeVarargs 애너테이션을 달아 사용하는 데 불편함이 없게끔 해야함.

<br>

# 아이템 33. 타입 안전 이종 컨테이너를 고려하라

- 하나의 컨테이너에서 매개변수화할 수 있는 타입의 수가 제한됨.
    - 예컨대 Set에는 원소의 타입을 뜻하는 단 하나의 타입 매개변수만 있으면 되며, Map에는 키와 값의 타입을 뜻하는 2개만 필요한 식.
- **타입 안전 이종 컨테이너 패턴**(type safe heterogeneous container pattern)
    - 컨테이너 대신 키를 매개변수화한 다음, 컨테이너에 값을 넣거나 뺄 때 매개변수화한 키를 함께 제공하는 설계 방식.
        - 이렇게 하면 제네릭 타입 시스템이 값의 타입이 키와 같음을 보장해줌.

- Favorites 클래스의 API
    - 클라이언트는 즐겨찾기를 저장하거나 얻어올 때 Class 객체를 알려주면 됨.
    
    ```java
    public class Favorites {
        public <T> void putFavorite(Class<T> type, T instance);
        public <T> T getFavorite(Class<T> type);
    }
    ```
    
    ```java
    public static void main(String[] args) {
        Favorites f = new Favorites();
    
        f.putFavorite(String.class, "Java");
        f.putFavorite(Integer.class, 0xcafebabe);
        f.putFavorite(Class.class, Favorites.class);
    
        String favoritesString = f.getFavorite(String.class);
        int favoriteInteger = f.getFavorite(Integer.class);
        Class<?> favoriteClass = f.getFavorite(Class.class);
    
        Sytstem.out.printf("%s %x %s%n", favoriteString, favoriteInteger, favoriteClass.getName());
    }
    ```
    
    - String을 요청했는데 Integer를 반환하는 일은 절대 없기 때문에 Favorites 인스턴스는 타입 안전함.
    - 모든 키의 타입이 제각각이라, 일반적인 맵과 달리 여러 가지 타입의 원소를 담을 수 있음.
    - Favorites는 타입 안전 이종(heterogeneous) 컨테이너라 할 만함.

```java
public class Favorites {
    private Map<Class<?>, Object> favorites = new HashMap<>();

    public <T> void putFavorite(Class<T> type, T instance) {
        favorites.put(Objects.requireNull(type), instance);
    }

    public <T> T getFavorite(Class<T> type) {
        return type.cast(favorites.get(type));
    }
}
```

- Favorites가 사용하는 private 맵 변수인 favorites의 타입은 Map<Class<?>, Object>
- 맵이 아니라 키가 와일드카드 타입이기 때문에 모든 키가 서로 다른 매개변수화 타입일 수 있다는 뜻으로 첫 번째는 Class<String>, 두 번째는 Class<Integer> 식으로 될 수 있음.
- getFavorite 구현
    - 주어진 Class 객체에 해당하는 값을 favorites 맵에서 꺼냄.
    - 이 객체의 타입은 Object이나, 이것을 T로 바꿔 반환해야 함.
        - getFavorite 구현은 Class의 cast 메서드를 사용해 이 객체 참조를 Class 객체가 가리키는 타입으로 동적 형변환함.
            
            ```java
            public class Class<T> {
                T cast(Object obj);
            }
            ```
            

- Favorites의 제약 두 가지
    - **Class 객체를 (제네릭이 아닌) 로우 타입으로 넘기면 Favorites 인스턴스의 타입 안정성이 쉽게 깨짐.**
    - **실체화 불가 타입에는 사용할 수 없음.**
        - String이나 String[] 은 저장할 수 있어도 즐겨 찾는 List<String>은 저장할 수 없음.
    
- **한정적 타입 토큰** : 단순히 한정적 타입 매개변수나 한정적 와일드카드를 사용하여 표현 가능한 타입을 제한하는 타입 토큰.
    - 예제
        
        ```java
        public <T extends Annotation> T getAnnotation(Class<T> annotationType);
        ```
        
        - 여기서 annotationType 인수는 애너테이션 타입을 뜻하는 한정적 타입임.
            - 즉, 애너테이션된 요소는 그 키가 애너테이션 타입인, 타입 안전 이종 컨테이너인 것임.
    

### 정리

- 컬렉션 API로 대표되는 일반적인 제네릭 형태에서는 한 컨테이너가 다룰 수 있는 타입 매개변수의 수가 고정되어 있음.
- **하지만 컨테이너 자체가 아닌 키를 타입 매개변수로 바꾸면 이런 제약이 없는 타입 안전 이종 컨테이너를 만들 수 있음.**
- 타입 안전 이종 컨테이너는 Class를 키로 쓰며, 이런 식으로 쓰이는 Class 객체를 타입 토큰이라 함.
    - 또한, 직접 구현한 키 타입도 쓸 수 있음.
- 데이터베이스의 행을 표현한 DatabaseRow 타입에는 제네릭 타입인 Column<T>를 키로 사용할 수 있음.
