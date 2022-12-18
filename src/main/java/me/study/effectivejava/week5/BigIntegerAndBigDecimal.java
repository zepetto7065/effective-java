package me.study.effectivejava.week5;

import java.math.BigInteger;

public class BigIntegerAndBigDecimal {
    public static void main(String[] args) {
        namingCheck();
    }
    public static void namingCheck(){
        BigInteger amount = new BigInteger("10000");
        BigInteger anotherAmount = new BigInteger("10001");

        BigInteger sum = amount.add(anotherAmount);
        System.out.println(sum);

        Complex.plus();

    }
}
