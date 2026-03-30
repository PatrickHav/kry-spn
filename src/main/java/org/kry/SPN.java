package org.kry;

public class SPN {
    public static final int r = 4;
    public static final int n = 4;
    public static final int m = 4;
    public static final int s = 32;

    public static final int[] sBox =
            {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7};

    public static final int[] bitPermutation =
            {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};

    public static final int key = 0b0011_1010_1001_0100_1101_0110_0011_1111;

    public static final String cipherText =
            "000001001101001000001011101110000000001010001111" +
                    "100011100111111101100000010100010100001110100000" +
                    "000010011011001110010101110110000";

}


