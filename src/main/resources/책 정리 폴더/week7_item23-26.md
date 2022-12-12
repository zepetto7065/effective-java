## 아이템 23 태그 달린 클래스보다는 클래스 계층구조를 활용하라 
태그달린 클래스 
- 두 가지 이상의 의미를 표현할 수 있으며, 그중 현재 표현하는 의미를 태그 값으로 알려주는 클래스

~~~ java 
public class Figure {
    enum Shape { RECTANGLE, CIRCLE };

    // 태그 필드 - 현재 모양을 나타낸다.
    final Shape shape;

    // 다음 필드들은 모양이 사각형(RECTANGLE)일 때만 쓰인다.
    double length;
    double width;

    // 다음 필드는 모양이 원(CIRCLE)일 때만 쓰인다.
    double radius;

    // 원용 생성자
    Figure(double radius) {
        shape = Shape.CIRCLE;
        this.radius = radius;
    }

    // 사각형용 생성자
    Figure(double length, double width) {
        shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }

    double area() {
        switch (shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new AssertionError(shape);
        }
    }
}
~~~

단점이 많다 

- 열거 타입 선언, 태그 필드, switch 문 등 불필요한 코드가 많다
- 여러 구현이 한 클래스에 혼합되어 가독성도 나쁘다
- 다른 의미를 위한 코드도 언제나 함께 하기 때문에 메모리도 많이 사용한다
- 필드들을 final로 선언하기 위해 해당 의미에 쓰이지 않는 필드들까지 생성자에서 초기화해야 한다
- 다른 타입을 추가하려면 코드를 추가해야한다. 
- ...등등 태그달린 클래스는 장황하고, 오류를 내기 쉽고, 비효율적이다. 

- 단점들을 극복하기 위해서는 클래스 계층 구조를 사용 

~~~ java 
abstract class Figure {
    abstract double area();
}

class Circle extends Figure {
    final double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    double area() {
        return Math.PI * (radius * radius);
    }
}

class Rectangle extends Figure {
    final double length;
    final double width;

    public Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    double area() {
        return length * width;
    }
}
~~~

다음 순서에 따라 클래스 계층 구조로 바꿀 수 있다
1. 계층구조의 루트가 될 추상 클래스를 정의하고, 태그 값에 따라 동작이 달라지는 메서드들(여기선 area)을 루트 클래스의 추상 메서드로 선언한다
2. 태그 값에 상관없이 동작이 일정한 메서드들을 루트 클래스에 일반 메서드로 추가하고, 공통으로 사용하는 데이터 필드들도 전부 루트 클래스로 올린다(여기선 없다)
3. 루트 클래스를 확장한 구체 클래스를 의미별로 하나씩 정의한다


## 아이템 24 멤버클래스는 되도록 static으로 만들라
## 중첩 클래스 : 다른 클래스 안에 클래스가 있는 것 
### 정적 멤버 클래스
- 다른 클래스 안에 선언되고 바깥 클래스의 private 멤버에 접근할 수 있다는 점에서 일반 클래스와 차이
- 바깥 클래스와 함께 쓰일 때만 유용한 public 도우미 클래스로 쓰인다
~~~
public class Calculator {

    // 중첩된 enum 타입은 암시적으로 static으로 선언되므로 명시적으로 static 키워드를 붙이지 않아도 됨
    public enum Operation {
        PLUS, MINUS;
    }
}

class Client {

    public static void main(String[] args) {
        Calculator.Operation operation = Calculator.Operation.PLUS; 
    }
}
~~~

<br>
<br>

### (비정적) 멤버 클래스   : 내부 클래스
- 비정적 멤버 클래스의 인스턴스는 바깥 클래스의 인스턴스와 암묵적으로 연결
- 비정적 멤버 클래스의 인스턴스 메서드에서 정규화된 this를 사용해 바깥 인스턴스를 호출하거나 바깥 인스턴스의 참조를 가져올 수 있다
  - 바깥 클래스가 외부에서 사용되지 않더라도 비정적 멤버 클래스가 바깥 클래스의 인스턴스를 참조할 수 있기 때문에 메모리 누수가 발생할 수 있다
- 정적 멤버 클래스의 인스턴스는 바깥 클래스와 독립적으로 생성할 수 있는 반면, 비정적 멤버 클래스의 인스턴스는 바깥 클래스의 인스턴스를 생성한 후 해당 참조를 이용해 생성해줘야 한다
  - 따라서 메모리 공간을 추가적으로 사용하며, 생성시간도 더 걸린다
- 따라서 개념상 중첩 클래스의 인스턴스가 바깥 인스턴스와 독립적으로 존재할 수 있다면 정적 멤버 클래스로 만들어야 한다
- Map구현체는 엔트리 개게들을 가지고 있고, 엔트리가 맵과 연관되어 있지만, 엔트리들의 메서드(getKey, getValue, setValue)은 맵을 집적 사용하지 않는다.

~~~ java 
    /**
     * Basic hash bin node, used for most entries.  (See below for
     * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
     */
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }
        
~~~
### 익명 클래스          : 내부 클래스
- 이름이 없다. 
- 람다를 지원하기 전에는 즉석에서 작은 함수 개체나 처리 객체를 만드는 데 익명클래스를 주로 사용했다. 

### 지역 클래스          : 내부 클래스
- 가장 드물게 사용된다.
- 쉽게 말해서, 메서드 안에 정의된 클래스 

<br>
<br>

# 아이템 25 톱레벨 클래스는 한 파일에 하나만 담으라 
- 소스 파일 하나에 여러 톱레벨 클래스를 담으면 한 클래스를 여러 가지로 정의할 수 있으며, 컴파일 순서에 따라 결과가 달라질 수 있다
- 컴파일 에러가 나지 않기 떄문에 위험하다 

~~~ java 
public class Main {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }
}
~~~

~~~ java 
Utensil.java
class Utemsil {
    static final String NAME = "pan";
}

class Dessert {
    static final String NAME = "cake";
}
~~~

~~~ java 
Dessert.java
class Utemsil {
    static final String NAME = "pot";
}

class Dessert {
    static final String NAME = "pie";
}
~~~ 

- javac Main.java Dessert.java 명령으로 컴파일  -> 컴파일 오류
  - Main.java 컴파일 -> Utensil.java 살핌 ->  Main.java 컴파일 끝 -> Dessert.java 컴파일 시작 -> 같은 클래스 있다고 오류 
- javac Main.java , javac Main.java Utensil.java -> pencake 출력
- javac Dessert.java Main.java -> potpie 출력

- 따라서 소스 파일 하나에는 반드시 톱레벨 클래스(혹은 톱레벨 인터페이스)를 하나만 담는다


#5장 제네릭
- 자바 5부터 사용 
- 장정 : 제네릭을 사용하면 컬렉션이 담을 수 있는 타입을 컴파일러에게 알려주게 된다.
  - 컴파일러는 알아서 형변환 코드를 추가할 수 있게 되고, 엉뚱한 타입의 객체를 넣으려는 시도를 컴파일 과정에서 차단하여 안전한 프로그램을 만들어준다.
- 단점 : 코드가 복잡해진다.

 
 
## 아이템 26 로 타입을 사용하지 말라 
- 제네릭 타입 : 제네릭 클래스 or 제네릭 인터페이스 :선언에 타입 매개변수가 쓰이는 클래스와 인터페이스
- raw type : 제네릭 타입에서 타입 매개변수를 전혀 사용하지 않을 때. 
  - List<E> 의 raw type => List
  - 문제점 
    - raw type은 타입 선언에서 제네릭 타입 정보가 전부 지워진 것 처럼 동작한다. 
      - 컴파일 시점에서 타입을 체크하지 않고 런타임 시점에서 타입을 체크 ->  오류는 가능한 발생 즉시, 컴파일할 떄 발견하는 것이 좋다. 
    - raw type을 쓰면 제네릭이 안겨주는 안정성과 표현력을 모두 잃게 된다
      - 안정성) 컴파일 시점에서 타입을 체크한다
      - 표현력) 특정 타입의 인스턴스를 사용한다는 정보가 주석이 아닌 타입 선언 자체에서 명시된다(List<String>)  
  - 로 타입은 제네릭 이전 코드들과의 호환성을 위해서만 사용한다
  
- 대안 
  - 임의 객체를 허용하는 매개변수화 타입 (List< Object >)
    - 모든 타입을 허용한다는 것을 컴파일러에게 명확하게 전달. 
  - 와일드 카드 사용 
    - 제네릭 타입은 쓰고 싶지만 실제 타입 매개변수가 무엇인지 신경 쓰고 싶지 않은 경우 
    - ? 사용 (List<?>)
    - raw 타입은 아무 원소나 넣을 수 있지만 Collection<?>에서는 null 이외엔 어떤 원소도 넣을 수 없다. 
  
  
- raw 타입을 사용해도 되는 경우
  - Class 리터럴에는 raw 타입으로 써야한다. 
    - 자바 명세에서 class 리터럴에 매개변수화 타입을 사용하지 못하게 했다(배열과 기본 타입은 허용)
    - List.class, String[].class, int.class는 허용한다
    - List<String>.class, List<?>.class는 허용하지 않는다
  - instanceof연산자 
    - 로 타입이든 비한정적 와일드카드 타입이든 instanceof는 완전히 똑같이 동작함
    - 오히려 로 타입을 쓰는 편이 더욱 깔끔함

~~~ java 
if (o instanceof Set) {         // 로타입
    Set<?> s = (Set<?>) o;      // 와일드카드 타입
    ...
} 
~~~
