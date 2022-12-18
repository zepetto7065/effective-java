package me.study.effectivejava.week5;

import java.awt.*;

public class PointAndDimention
{
    static Point point;
    static Dimension dimension;

    public static void main(String[] args) {
        pointAndDimensionChecking();
    }

    public static void pointAndDimensionChecking(){
        point = new Point();
        point.x = 5;
        point.y = 2;

        point.setLocation(3,2);
        point.x = 6;
        System.out.println(point.x);

        String abs = "test";
        String abs2 = abs + "test2";

        StringBuilder builder = new StringBuilder("test");
        builder.append("test2");
    }
}
