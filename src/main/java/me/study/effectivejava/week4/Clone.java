package me.study.effectivejava.week4;

public class Clone extends Object implements Cloneable {
    @Override
    protected PhoneNumber clone()  {
        try {
            return (PhoneNumber) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
