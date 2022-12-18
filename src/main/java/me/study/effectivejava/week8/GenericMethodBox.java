package me.study.effectivejava.week8;

public class GenericMethodBox<T> {
    private T data;

    public T getData(){
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <K, V> boolean methodName(K key, V value){
        return true;
    }
}
