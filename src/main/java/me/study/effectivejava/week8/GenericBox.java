package me.study.effectivejava.week8;

public class GenericBox<T> {
    private T data;

    public T getData(){
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public double average(GenericBox<T> a){
        return ((Double)this.data + (Double)a.getData()) / 2;
    }
}
