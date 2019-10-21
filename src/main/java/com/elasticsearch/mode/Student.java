package com.elasticsearch.mode;

public class Student implements Comparable<Student> {

    private String name;
    private int age;
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

//    @Override
//    public int compareTo(Student stu) {
//        return -(this.age - stu.age);
//    }

    @Override
    public int compareTo(Student stu) {
        return this.name .compareTo(stu.name);
    }

//    public int hashCode(){
//        return this.name.hashCode() * this.age    ; // 自己定义一个公式，如上所写。
//    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        Student other = (Student) obj;
//        if (age != other.age)
//            return false;
////        if (name == null) {
////            if (other.name != null)
////                return false;
////        } else if (!name.equals(other.name))
////            return false;
//        return true;
//    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
