package me.study.effectivejava.week8;

public class NumberGenericBox<T extends Number> {
    private T data;

    public T getData(){
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public double average(NumberGenericBox<T> a){
        return (this.data.doubleValue() + a.getData().doubleValue()) / 2;
    }
}
