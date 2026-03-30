package org.kry;


public class Main {
    public static void main(String[] args) {
        // 1. x XOR K (k,0)
        int test = SPN.x ^ SPN.roundKeys[0];
        System.out.println(Integer.toBinaryString(test));
    }
}