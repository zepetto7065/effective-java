## 아이템 27. 비검사 경고를 제거하라

- 비검사 형변환 경고
- 비검사 메서드 호출 경고
- 비검사 매개변수화 가변인수 타입 경고
- 비검사 변환 경고

~~~ java
//- 해당 경우는 -Xlint:uncheck 옵션 추가 
Set<Lark> exaltation = new HashSet();
~~~

- 자바 7부터는 <> (다이아몬드 연산자)가 대부분 추론해준다. -> 이것만으로 제거를 완전히 해주지 못하는 경고가 존재

### 가능하면 경고를 제거하라

- 런타임에 ClassCastException이 발생하지 않게 하고 싶다면?
- 경고를 제거할 수 없고 타입의 안정성을 보장하고 싶다면, @SuppressWarnings("unchecked")로 경고를 숨기자
    - 하지만 여전히 Runtime시에 ClassCastException을 던질수 있다.

~~~ java
//ArrayList에 toArray 메서드 
public <T> T[] toArray(T[] a){
    if(a.lenght < size)
        @SuppressWarnins("unchecked")
        return (T[]) Arrays.copyOf(elements, size, a.getClass())
    System.arraycopy(element, 0, a, 0, size);
    if(a.length > size)
        a[size] = null;
    return a;
}
~~~ 

- uncheck cast Exception 발생하고, return 문에는 @SuppressWarnings를 붙일 수 없으니 선언에 달도록 하자.
- 하지만 위 어노테이션을 사용할때는 안전한 이유를 같이 주석으로 꼭 남겨 주자.

## 아이템 28. 배열보다는 리스트를 사용하라

### 배열은 공변이다

- 공변(covariant) - 함께 변한다.
- Super(상위) > Sub(하위) => Super[] > Sub[]
- 반면 제네릭은 불공변이다
    - List<Super> ??? List<Sub>

~~~ java
Object[] objectArray = new Long[1];
objectArray[0] = "타입이 달라서 넣을수 없다" //ArrayStoreException을 던진다.

List<Obejct> oi = new ArrayList<Long>(); // 호환이 되지 않는다.
oi.add("타입이 달라야한다")
~~~ 

- 배열은 위와 같은 실수를 런타임 시점, 리스트는 컴파일 시점에 알수 있다.

### 배열은 실체화(reify)

- 제네릭은 타입 정보가 런타임에 소거된다. (erasure)
    - 즉 원소타입은 컴파일 타임에 검사하고, 런타임 시점에 알수가 없다 
    - 소거는 제네릭 지원 이전의 레거시와 함꼐 사용할 수 있도록 해주는 메커니즘이다.
- 배열은 제네릭 타입, 매개변수화 타입, 타입 매개변수로 사용할 수 없다
    - new List<E>[], new List<String>[], newE[] -> 컴파일시 제네릭 배열 생성 오류
        - 왜 제네릭 배열을 만들지 못하게 했을까 -> 타입이 안전하지 않기 때문 --> 컴파일러가 자동 생성한 형반환 코드에서 ClassCastException이 발생하게 된다.
      ~~~ java
      List<String>[] stringLists = new List<String>[]; // (1)이 된다고 가정
      List<Integer> intList = List.of(42); // (2)
      Obejct[] objects = stringLists; // (3) , 배열은 공변이니 가능
      objects[0] = inteList; // (4) , 
      String s = stringList[0].get(0); // (5) stringList[0]를 꺼냈는데, List<String> 이 아닌 List<Integer> ClassCastException 
      ~~~ 
        - 즉 위를 방지하려면 1번을 컴파일 시점에 방지해야한다.
        - 위와 같은 E, List\<E>, List\<String> 같은 <b>타입을 실체화 불가 타입 (non-reifable type)</b>
          >실체화되지 않아서 런 타임에는 컴파일 타임보다 타입 정보를 적게 가지는 타입
        - 실체 가능한 타입은 List\<?> , Map\<?,?> 과 같은 비한정적 와일드카드 타입뿐 (유용하게 쓰이는 일이 거의 없다)
    
- 제네릭 타입과 가변인수(?)를 같이 쓰면 나오는 경고를 @SafeVarargs 애너테이션으로 대체 가능.
- 배열대신 리스트를 사용하자
  - 배열로 형변환시에는 E[] 대신 컬렉션인 List<E>를 사용하면 대부분의 오류들은 해결된다.
  - 조금 복잡해지고 성능상 안좋을지 몰라도, 타입 안정성 및 경고 문구가 생성되지 않는다.

~~~java
public class Chooser<T> {
    private final T[] choiceArray;

    public Chooser(Collection<T> choices) {
        choiceArray = choices.toArray();
    }

    public Object choose() {
        Random rnd = TreadLocalRndom.current();
        return choiceArray[rnd.nextInt(choiceArray.length)];
    }
}

// 제네릭 선언
public class Chooser<T> {
    private final T[] choiceArray;

    public Chooser(Collection<T> choices) {
        choiceArray = (T[]) choices.toArray(); // 형변환
    }
    //choose 메서드는 그대로다.
}
~~~

- T가 무슨 타입인지 알수 없어 안전을 보장할 수 없으나, 동작을 한다. ( 단지 안전을 보장하지 못한다는 경고만 나타난다 )
  - 컴파일 시점에 안전을 보장을 못한다는게 문제점
- 애너테이션을 추가해도 좋지만.. 아래 방법 추천

~~~java
public class Chooser<T> {
    private final List<T> choiceList;

    public Chooser(Collection<T> choices) {
        choiceList = new ArrayList<>(choisces); // 형변환
    }

    public Object choose() {
        Random rnd = TreadLocalRndom.current();
        return choiceList.get(rnd.nextInt(choiceArray.length);
    }
}
~~~

## 아이템 29. 이왕이면 제네릭 타입으로 만들라

### 일반 클래스를 제네릭 클래스로 변경하는 단계적 설명

1. 클래스 선언에 타입 매개 변수를 추가하라.

~~~java
public class Stack {
    private Object[] elements;

    public Object pop() {

    }
    //...
}

public class Stack<E> {
    private E[] elements;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    public Stack(){
        elements = new E[DEFAULT_INITIAL_CAPACITY]; // 오류 발생
    }
    
    public E pop() {
        E result = elements[--size];
    }
    //...
}
~~~

- 위 Stack 코드를 실행하면 실채불가한 타입으로는 배열을 만들수 없다.
- 생성한 부분에서 에러가 발생한다. (아이템 28)
    - 해결책
      - 첫째. 제네릭 배열 생성을 금지하는 제약을 대놓고 형변환 
      - Object 배열 생성후 제네릭 배열로 형변환
          - (E[]) new Object[BLABLABLA]
          - element는 private에 저장되고 다른 메서드에 전달되는 일이 없다.
          - 가독성이 두번쨰 방법보다 더 낫고, E[]로 선언함으로 E타입 인스터스만 받고 있음을 확실히 어필한다.
          - 컴파일 시점과 런타임 타입이 달라, 힙 오염 (heap pollution)을 일으킨다...?
      - 둘째. elements 필드의 타입을 E[]에서 Object[]로 바꾸자
        - E result = elements[--size] 오류
        - E로 형변환 해도 오류 -> @SuppressWarning("unchecked")로 해당부분을 숨길수 있다. 
        - 배열에서 원소를 읽을때마다 형변환을 해주어야한다.
- 위 예시는 item 28과 모순되어 보이지만? 경우에 따른 선택이다. (예외도 존재한다)
    - Stack의 예시처럼 타입 안에서 리스트를 사용하는게 항상 가능하지도 , 꼭 더 좋은것도 아니다.

## 아이템 30. 이왕이면 제네릭 메서드로 만들라

~~~java
//컴파일 경고를 발생
//결고를 없애려면 타입을 안전하게 만들어라.
public static Set union(Set s1,Set s2){
    Set result=new HashSet(s1);
    result.addAl1(s2);
    return result;
}
//경고없이 , 타입 안전하고, 쓰기도 쉽다. 타입매개변수 목록은 메서드의 제한자와 반환타입 사이에 온다.
//한정적 와일드카드 타입을 사용하여 유연하게 개선가능
public static <E> Set<E> union2(Set<E> s1, Set<E> s2){
        Set<E> result=new HashSet(s1);
        result.addAl1(s2);
        return result;
}
~~~
- 불변 객체를 여러 타입으로 활용할 수 있게 만들어야 하는 경우가 있다.
  - 제네릭은 런타임시점에 타입 소거 , 타입을 매개변수화하여 어떤 요청이든 객체의 타입을 바꿔주는 정적 팩터리를 만들어야하는데.. 
  - 매번 그타입에 맞게 변경 -> 제네릭 싱글턴 팩터리
    ~~~java
    //코드 30-4 제네릭 싱글턴 팩터리 패턴
    private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;
    
    @SuppressWarnings ("unchecked")
    public static <T> UnaryOperator<T> identityFunction() {
    return (UnaryOperator<T>) IDENTITY_FN;
    }
    ~~~
    - T가 어떤 타입이든 UnaryOperator<T>는 UnarayOperator<Object>가 아니다.
    - 위 함수는 T가 무엇이든 UnaryOperator<T>를 반환하는 항등함수 이므로 @SupressWarning을 통한 안전보장
      - 상대적dm로 드물지만, 자기 자신이 들어간 표현식을 사용하여 타입 매개변수의 허용 범위를 한정할 수 있다.
        -> 재귀적 타입 한정 (recursive type bound)
        - UnaryOperator<String> , UnaryOperator<Number> ... -> 자기과 같은 원소만 비교가능하다.
        - ex Comparable<T>
    ~~~java
    //코드 30-6 재귀적 타입 한정을 이용한 상호 비교
    //모든 타입 E는 자신과 비교할 수 있다.
    public static <E extends Comparable<E>> E max(Comparable<E> c) {
        if(c.isEmpty()) throw new IllegalArgumentException("컬렉션이 비어 있습니다");
        ...
    }
    ~~~
    - 컬렉션에 담긴 원소의 순서 대로 계산
    - 훨씬 복잡해질 가능성이 있지만 , 와일드카드를 사용한 변형 (item31), 시뮬레이트한 셀프타입 관용구(아이템2)를 이해하면 무리없이 이해가능?????
### 정리
- 입력 매개변수와 반환값을 명시적으로 형변환해야하는 메서드보다 제네릭 메서다가 더 안전하고 사용도 쉽다.
- 기존 메서드를 제너릭하게 ? 만들자