package com.gome.rocketmq.example.ttx;

/**
 * @author tantexian
 * @since 2016/6/14
 */
public class SpringAndMybatisTest {
    public static void main(String[] args) {
        System.out.println("1-------------");
        try {
            SpringAndMybatisInit.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("2-------------");
    }
}
