package org.kry;


import java.util.Arrays;

public class Main {

    public static final String cypherText =
            "00000100110100100000101110111000000000101000111110001110011111110110000001010001010000111010000000010011011001110010101110110000";

    public static void main(String[] args) {

        int r = 4;
        int n = 4;
        int m = 4;
        int s = 32;
        int[] sBox = {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7};
        int[] bitPermutation = {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};
        int key = 0b0011_1010_1001_0100_1101_0110_0011_1111;
        SPN spn = new SPN(r, n, m, s, sBox, bitPermutation, key);
        System.out.println(spn.decrypt(cypherText));
    }
}