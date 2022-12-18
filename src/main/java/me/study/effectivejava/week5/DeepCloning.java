package me.study.effectivejava.week5;

import java.util.Arrays;

public class DeepCloning {
    public static void main(String[] args) {
        //서로 영향을 끼치지 않는 깊은 복사
        number();
        str();

        //서로 영향을 끼치는 얕은 복사
        array();
    }

    private static void array() {
        int[] a = {1,2,3};
        int[] b = a;

        a[0] = 3;

        Arrays.stream(a).forEach(i -> System.out.println("i = " + i));
        Arrays.stream(b).forEach(i -> System.out.println("i = " + i));
    }

    private static void str() {
        String a = "hello";
        String b = a;

        a = "world";
        System.out.println("a = " + a);
        System.out.println("b = " + b);
    }

    private static void number() {
        int a = 1;
        int b = a;
        a = 3;

        System.out.println("a : " + a);
        System.out.println("b : "+ b);

        Fee fee = new Fee(2L, 3L);


    }
}
