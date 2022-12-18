## 아이템 27. 비검사 경고를 제거하라

- 비검사 형변환 경고
- 비검사 메서드 호출 경고
- 비검사 매개변수화 가변인수 타입 경고
- 비검사 변환 경고

~~~ java 
Set<Lark> exaltation = new HashSet();
~~~

- 해당 경우는 -Xlint:uncheck 옵션 추가
- 자바 7부터는 <> (다이아몬드 연산자)가 추론해준다. -> 이것만으로 제거를 완전히 해주지 못하는 경고가 존재

### 가능하면 경고를 제거하라

- 런타임에 ClassCastException이 발생하지 않는다.
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
- 하지만 위 어노테이션을 사용할때는 안전한 이유를 같이 주석으로 남겨어 주자.

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
    - 소거는 제네릭 지원 이전의 레거사와 함꼐 사용할 수 있도록 해주는 메커니즘이다.
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
        - 위를 방지하려면 1번을 컴파일 시점에 방지해야한다.
        - 위와 같은 E, List<E>, List<String>같은 타입을 <b>실체불가 타입 (non-reifable type)</b>
        - 실체 가능한 타입은 List<?> , Map<?,?> 과 같은 비한정적 와일드카드 타입뿐 (유용하게 쓰이는 일이 거의 없다)
- 제네릭 타입과 가변인수(?)를 같이 쓰면 나오는 경고를 @SafeVarargs 애너테이션으로 대체 가능.
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

    public E pop() {

    }
    //...
}
~~~

- 위 Stack 코드를 실행하면 실채불가한 타입으로 배열을 생성한 부분에서 에러가 발생한다.
    - 첫째. 제네렉 배열 생성을 금지하는 제역을 대놓고 형변환 -> 제네릭 배열로 형변환
        - (E[]) new Object[BLABLABLA]
        - 가독성이 두번쨰 방법보다 더 낫고, E[]로 선언함으로 E타입 인스터스만 받고 있음을 확실히 어필한다.
        - 컴파일 시점과 럼타임 타입이 달라, 힙 오염 (heap pollution)을 일으킨다.?
    - 둘쨰. elements 필드의 타입을 E[]에서 Object[]로 바꾸자
        - 배열에서 원소를 읽을때마다 형변환을 해주어야한다.
- 위 예시는 item 28과 모순되어 보이지만? 경우에 따른 선택이다.
    - Stack의 예시처럼 타입 안에서 리스트를 사용하는게 항상 가능하지도 , 꼭 더 좋은것도 아니다.

## 아이템 30. 이왕이면 제네릭 메서드로 만들라

~~~java
//컴파일 경고를 발생
public static Set union(Set s1,Set s2){
    Set result=new HashSet(s1);
    result.addAl1(s2);
    return result;
}
//경고없이 , 타입 안전하고, 쓰기도 쉽다.
public static <E> Set<E> union2(Set<E> s1, Set<E> s2){
        Set<E> result=new HashSet(s1);
        result.addAl1(s2);
        return result;
}
~~~
- 불변 객체를 여러 타입으로 활용할 수 있게 만들어야 하는 경우가 있다.
  - 제네릭은 런타임시점에 타입 소거 , 타입을 매개변수화하여 어떤 요청이든 객체의 타입을 바꿔주는 정적 팩터리를 만들어야하는데.. 
  -> 제네릭 싱글턴 팩터리
    ~~~java
    //코드 30-4 제네릭 싱글턴 팩터리 패턴
    private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;
    @SuppressWarnings ("unchecked")
    public static <T> UnaryOperator<T> identityFunction() {
    return (UnaryOperator<T>) IDENTITY_FN;
    }
    ~~~
    - T가 어떤 타입이든 UnaryOperator<T>를 사용해도 타입 안전
    - 상대적은로 드물지만, 자기 자신이 들어간 표현식을 사용하여 타입 매개변수의 허용 범위를 한정할 수 있다.
      -> 재귀적 타입 한정 (recursive type bound) (ex Comparable 인터페이스)
    - UnaryOperator<Number> 든 UnaryOperator<String>이든 
### 정리
- 입력 매개변수와 반환값을 명시적으로 형변환해야하는 메서드보다 제네릭 메서다가 더 안전하고 사용도 쉽다.
- 기존 메서드를 제너릭하게 ? 만들자