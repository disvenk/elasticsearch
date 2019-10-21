package com.elasticsearch.controller;

import com.elasticsearch.mode.Student;

import java.util.Set;
import java.util.TreeSet;

public class Test {
    public static void main(String[] args) {
        Set set = new TreeSet<>();//treeset必须是爱你comparable接口,如果不重写equals方法，默认和compareTo比较字段一致
        Student stu1 = new Student("貂蝉", 19);
        Student stu2 = new Student("西施", 25);
        Student stu3 = new Student("王昭君", 22);
        Student stu4 = new Student("杨贵妃", 19);
        set.add(stu1);
        set.add(stu2);
        set.add(stu3);
        set.add(stu4);
        System.out.println(set);

        System.out.println(tigui(4));
    }

    public static int tigui(int n){
        if(n<=1){
            return 1;
        }
        return n*tigui(n-1);
    }
}
