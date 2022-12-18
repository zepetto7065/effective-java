package me.study.effectivejava.week5;

public class Complex {
    private final double re;
    private final double im;

    private Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public static Complex plus() {

        return new Complex(2.0, 3.0);
    }


}
