package org.kry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SPNTest {

    @Test
    void encrypt() {
        String x = "Lorem ipsum dolor sit amet consectetur adipiscing elit quisque faucibus.";
        int key = 0b0011_1010_1001_0100_1101_0110_0011_1111;
        SPN spn = createSPN(key);

        String encrypted = spn.encrypt(x);
        assertEquals(x, spn.decrypt(encrypted));
    }

    @Test
    void decrypt() {
        String x = "00000100110100100000101110111000000000101000111110001110011111110110000001010001010000111010000000010011011001110010101110110000";
        String expected = "Gut gemacht!";
        int key = 0b0011_1010_1001_0100_1101_0110_0011_1111;
        SPN spn = createSPN(key);
        assertEquals(expected, spn.decrypt(x));
    }



    @Test
    void spnEncryptBlock() {
        int key = 0b0001_0001_0010_1000_1000_1100_0000_0000;
        String x = "0001001010001111";
        String expected = "1010111010110100";

        SPN spn = createSPN(key);

        assertEquals(expected, spn.spnEncryptBlock(x));
    }

    private SPN createSPN(int key) {
        int r = 4;
        int n = 4;
        int m = 4;
        int s = 32;
        int[] sBox = {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7};
        int[] bitPermutation = {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};
        return new SPN(r, n, m, s, sBox, bitPermutation, key);
    }
}