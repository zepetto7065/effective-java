package me.study.effectivejava.week2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapKeySetExample {

    public static void mapTest() {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");

        Set<String> key1 = map.keySet();
        Set<String> key2 = map.keySet();

        System.out.println("key1 set size : " + key1.size());
        System.out.println("key2 set size : " + key2.size());

        key1.remove("key1");

        System.out.println("key1 set size : " + key1.size());
        System.out.println("key2 set size : " + key2.size());
        /**
         * key1 set size : 3
         * key2 set size : 3
         * key1 set size : 2
         * key2 set size : 2
         */
    }

    public static void main(String[] args) {
        mapTest();
    }
}
