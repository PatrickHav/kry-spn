package org.kry;

import java.util.*;

/**
 * Implements a SPN and uses it in CTR mode.
 */
public class SPN {

    private final int r;
    private final int n;
    private final int m;
    private final int s;
    private final int l;

    private final int[] sBox;

    private final int[] bitPermutation;

    private final int key;

    private final int[] roundKeys;

    /**
     * Creates a new SPN.
     *
     * @param r              number of rounds
     * @param n              size of one S-box input in bits
     * @param m              number of S-boxes per block
     * @param s              key length in bits
     * @param sBox           S-box mapping
     * @param bitPermutation bit permutation mapping
     * @param key            encryption key
     */
    public SPN(int r, int n, int m, int s, int[] sBox, int[] bitPermutation, int key) {
        this.r = r;
        this.n = n;
        this.m = m;
        this.s = s;
        this.l = n * m;
        this.sBox = sBox;
        this.bitPermutation = bitPermutation;
        this.key = key;
        this.roundKeys = createRoundKeys(key, r, s);
    }

    /**
     * Encrypts a given text.
     *
     * @param text plaintext to encrypt
     * @return ciphertext as a bit string
     */
    public String encrypt(String text) {
        String ascii = textToAscii(text);
        String paddedAscii = padAscii(ascii);

        String initialY = createRandomBitString();

        List<String> ys = new ArrayList<>();
        ys.add(initialY);

        List<String> parts = splitToParts(paddedAscii, l);

        for (int i = 0; i < parts.size(); i++) {
            String inc = incrementModulo2PowL(initialY, i);
            String spnEncrypted = spnEncryptBlock(inc);
            String xored = xorString(spnEncrypted, parts.get(i));
            ys.add(xored);
        }
        return String.join("", ys);
    }

    /**
     * Decrypts the given ciphertext using CTR mode.
     *
     * @param chiffre ciphertext as a bit string
     * @return decrypted plaintext
     */
    public String decrypt(String chiffre) {
        List<String> xs = new ArrayList<>();
        String initialY = chiffre.substring(0, l);
        List<String> chiffreBlocks = splitToParts(chiffre.substring(l), l);

        // Decrypt blocks
        for (int i = 0; i < chiffreBlocks.size(); i++) {
            String inc = incrementModulo2PowL(initialY, i);
            String spnEncrypted = spnEncryptBlock(inc);
            String xored = xorString(spnEncrypted, chiffreBlocks.get(i));
            xs.add(xored);
        }

        String asciiPadded = String.join("", xs);
        // Remove padding
        int lastIndexOf1 = asciiPadded.lastIndexOf("1");
        System.out.println("lastIndexOf1: " + lastIndexOf1);
        String ascii = String.join("", asciiPadded.substring(0, lastIndexOf1));

        // Convert ascii
        return asciiToText(ascii);
    }

    /**
     * Creates all round keys from the original key.
     *
     * @param key original key
     * @param r   number of rounds
     * @param s   key length
     * @return array of round keys
     */
    private int[] createRoundKeys(int key, int r, int s) {
        int[] keys = new int[r + 1];
        String bin = toFixedLengthBitString(key, s);
        for (int i = 0; i < keys.length; i++) {
            keys[i] = Integer.parseInt(bin.substring(i * n, i * n + l), 2);
        }
        return keys;
    }

    /**
     * Creates a random bit string
     *
     * @return random bit string
     */
    private String createRandomBitString() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(l);
        for (int i = 0; i < l; i++) {
            sb.append(rand.nextBoolean() ? '1' : '0');
        }
        return sb.toString();
    }

    /**
     * Converts a text to ASCII.
     *
     * @param text input text
     * @return ASCII bit string
     */
    private String textToAscii(String text) {
        StringBuilder bits = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            int value = text.charAt(i);
            String bin = String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0');
            bits.append(bin);
        }

        return bits.toString();
    }

    /**
     * Converts an ASCII bit string to plaintext.
     *
     * @param bits ASCII bit string
     * @return decoded text
     */
    private String asciiToText(String bits) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < bits.length(); i += 8) {
            String byteString = bits.substring(i, i + 8);
            int value = Integer.parseInt(byteString, 2);
            text.append((char) value);
        }

        return text.toString();
    }

    /**
     * Pads the ASCII bit string.
     *
     * @param ascii unpadded ASCII bit string
     * @return padded bit string
     */
    private String padAscii(String ascii) {
        StringBuilder sb = new StringBuilder(ascii + "1");
        while (sb.length() % l != 0) {
            sb.append('0');
        }
        return sb.toString();
    }

    /**
     * Computes (bits + i) mod 2^l.
     *
     * @param bits input bit string
     * @param i    increment value
     * @return incremented bit string
     */
    private String incrementModulo2PowL(String bits, int i) {
        int val = (Integer.parseInt(bits, 2) + i) % Math.powExact(2, l);
        return toFixedLengthBitString(val, l);
    }

    /**
     * Encrypts one block with the SPN.
     *
     * @param block plaintext block
     * @return encrypted block
     */
    public String spnEncryptBlock(String block) {
        // Initial step
        String x = xorString(block, toFixedLengthBitString(roundKeys[0], l));
        // Regular steps
        for (int i = 1; i <= r - 1; i++) {
            // SBOX
            List<String> parts = sbox(x, sBox);
            //Permutation
            String sboxed = String.join("", parts);
            String part = permutate(sboxed);
            x = xorString(part, toFixedLengthBitString(roundKeys[i], l));
        }
        // Finishing step
        List<String> parts = sbox(x, sBox);
        x = parts.stream()
                .reduce("", String::concat);
        x = xorString(x, toFixedLengthBitString(roundKeys[r], l));
        return x;
    }

    /**
     * Applies the bit permutation to a bit string.
     *
     * @param s input bit string
     * @return permutated bit string
     */
    private String permutate(String s) {
        List<String> bits = splitToParts(s, 1);

        List<String> permutated = new ArrayList<>(bits);
        for (int j = 0; j < bits.size(); j++) {
            permutated.set(j, bits.get(bitPermutation[j]));
        }
        return String.join("", permutated);
    }

    /**
     * Computes the bitwise XOR of two bit strings.
     *
     * @param a first bit string
     * @param b second bit string
     * @return XOR result
     */
    private String xorString(String a, String b) {
        int x = Integer.parseInt(a, 2) ^ Integer.parseInt(b, 2);
        return toFixedLengthBitString(x, l);
    }

    /**
     * Converts an integer to a binary string of fixed length.
     * Zeros are prepended if necessary.
     *
     * @param value  value to convert
     * @param length target length
     * @return bit string with target length
     */
    private String toFixedLengthBitString(int value, int length) {
        String bin = Integer.toBinaryString(value);
        if (bin.length() > length) {
            bin = bin.substring(bin.length() - length);
        }
        return String.format("%" + length + "s", bin).replace(' ', '0');
    }

    /**
     * Splits a string into parts of equal size.
     *
     * @param a      input string
     * @param length length of each part
     * @return list of parts
     */
    private List<String> splitToParts(String a, int length) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < a.length(); i += length) {
            String chunk = a.substring(i, i + length);
            parts.add(chunk);
        }
        return parts;
    }

    /**
     * Applies the S-box to the input.
     *
     * @param x    input bit string
     * @param sBox S-box mapping
     * @return substituted parts
     */
    private List<String> sbox(String x, int[] sBox) {
        List<String> parts = splitToParts(x, n);
        parts.replaceAll(string -> toFixedLengthBitString(sBox[Integer.parseInt(string, 2)], n));
        return parts;
    }
}