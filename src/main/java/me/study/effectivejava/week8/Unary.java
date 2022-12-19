package me.study.effectivejava.week8;

import java.util.function.UnaryOperator;

public class Unary {
    private static UnaryOperator<Object> IDENTITY_FN = (t) -> t;

    public static void main(String[] args) {
        String[] strings = {"삼베", "대마", "나일론"};
        UnaryOperator<String> sameString = identityFunction();
        for (String string : strings) {
            System.out.println(sameString.apply(string));
        }
    }

    private static <T> UnaryOperator<T> identityFunction() {
        return (UnaryOperator<T>) IDENTITY_FN;
    }
}
