1주차
item1 생성자 대신 정적 팩터리 메서드를 고려하라
- 한 객체가 하나의 역할 -> 결합도를 낮게 한다.
- Factory란 GoF factory 에서 유래 , 마치 공장에서 찍어내는듯함?
- 일반적인 생성자가 아닌 생성을 위한 Method

[장점]
1. 이름을 가질 수 있다
2. 호출될때 인스턴스를 새로 생성하지 않아도 된다
= 생성에 책임을 지지 않는다
= 불변 클래스 ( immutable class )
= 싱글턴 패턴 보장
3. 변환타입의 하위 타입 객체를 반환할 수 있는 능력이 있다

4. 입력 매개 변수에 따라 매번 다른 클래스의 객체를 반환가능

5. 정적 팩토리 메서드를 작성하는 시점에서 반환할 객체의 클래스가 존재하지 않아도 된다.


[단점]
1. public 이나 protected 생성자 필요하므로 정적 팩터리메서드만 제공하면 하위 클래스를 만들수 없다.
= 해당 클래스는 상속을 위한 public , protected 상속자가 필요하다.
= 상속에 의한 확장 불가

2. 프로그래머가 찾기 어렵다
= 보기 힘들다..

[그외]
from, of, valueOf 같은 네이밍 컨벤션 존재

item2 생성자에 매개변수가 많다면 빌더를 고려하라
JavaBeans Pattern (= setter)
- 메서드 여러개 호출해야함
- 객체 완성 전까지 일관성이 무너진다

빌더 패턴
- 연쇄적 호출 가능하여 'freezing' 가능

단점으로는 굳이 꼽자면 성능? 4개이상이 될때 사용하자
빌더 성능 ? static

item3 private 생성자나 열거 타입으로 싱글턴임을 보증하라
싱글턴 - 인스턴스를 오직 하나만 생성할 수 있는 클래스
한계
1. private 생성자를 가지고 있기에 상속 불가
2. 테스트 불편
3. 서버환경에서는 싱글턴이 하나만 만들어지는것을 보장하지 못한다
3. 전역상태를 만들 수 있기에 객체지향 프로그래밍에 바람직하지 못하다
( 단 static 필드와 메소드로만 구성된 클래스라면 사용 권장 )
장점
- 메모리 낭비를 방지 , 왜? 재사용가능하니까
- 다른 객체와의 공유가 쉽다

싱글턴을 만드는 방식 2가지
1. public static 멤버가 final 필드인 방식
public class Test{
    public static final Test INSTANCE = new Test();
    private Test(){...}
}
private 생성자는 public static final 필드가 초기화될떄 딱한번 호출된다. private이나 protected
생성자가 없으므로 Test는 인스턴스 전체 시스템에서 하나뿐임이 보장

장점
1. 싱글턴이다
2. 간결하다

주의
리플렉션 API, AccessibleObject.setAccessible을 이용하여 private 생성자를 호출 가능
-> 두번 객체가 생성되려할떄 예외를 던져라

2. 정적 팩터리 메서드를 Public static 멤버로 제공
public class Test{
    private static final Test INSTANCE = new Test();
    private Test(){...}
    public static Test getInstance(){ return INSTANCE; }
}
장점
1. API를 바꾸지 않아도 싱글턴이 아니게 변경할 수 있다. ( getInstance 내부에서 새인스턴스 생성 )
2. 제네릭 싱글턴 팩터리?로 만들수 있다
3. 정적 팩터리 메서드 참조를 공급자(supplier)로 사용가능하다.

문제 ( 이해 못함 )
직렬화 : 객체를 직렬화 한다는건 객체->바이트 스트림 java.io.Serializable 인터페이스를 구현

각 클래스는 직렬화를 하고 새로운 인스턴스를 만들어서 반환한다. 역질렬화는 기본 생성자를 호출하지 않고
값을 복사해서 새로운 인스턴스 반환 readResolve로 방지 가능
-> 역직렬화시 반드시 readResolve 메소드를 싱글턴을 리턴하도록 수정, 역직렬화시 새로운 인스턴스가 만들어지므로!!!!
private Object readResolve(){
    return INSTANCE;
}


3. 원소가 하나인 열거 타입을 선언하는 방식
public enum Test{
    INSTANCE;
    public String getName(){
        return "Test";
    }
}
String name = Test.INSTANCE.getName()
복잡한 직렬화 상황이나 리플레션 공격에도 제2의 인스턴스가 생기는 일을 완벽히 막아준다.
단 다른 상위 클래스를 상속해야한다면 이 방법은 사용 불가하다.
















