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

        System.out.println(point.x);
    }
}
