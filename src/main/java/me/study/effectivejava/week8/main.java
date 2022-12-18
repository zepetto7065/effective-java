package me.study.effectivejava.week8;

public class main {
    public static void main(String[] args) {
//        simpleBox();

//        genericBox();

//        numberGenericBox();

        boolean result = GenericMethod();

    }

    private static boolean GenericMethod() {
//        return GenericMethodBox.<String, Integer>methodName("key", 3);
        return GenericMethodBox.methodName("key", 3);
    }

    private static void numberGenericBox() {
        NumberGenericBox<Double> box1 = new NumberGenericBox<>();
        box1.setData(5.0);

        NumberGenericBox<Double> box2 = new NumberGenericBox<>();
        box2.setData(8.0);

        System.out.println(box1.average(box2));
    }

    private static void genericBox() {
        GenericBox<String> genericBox = new GenericBox<>();
        genericBox.setData("문자열");

        //컴파일 안됨
//        Integer data = (Integer) genericBox.getData();
//        int i = data + 1;
//        System.out.println(i);
    }

    private static void simpleBox() {
        SimpleBox simpleBox = new SimpleBox();
        simpleBox.setData("문자열");

        Integer data = (Integer) simpleBox.getData();
        int i = data + 1;
        System.out.println(i);
    }
}
