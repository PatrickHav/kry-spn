package org.kry;

import java.util.*;

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

    public String encrypt(String text) {
        String ascii = textToAscii(text);
        String paddedAscii = padAscii(ascii);


        // Create random bitstring
        String initialY = createRandomBitString();

        List<String> ys = new ArrayList<>();
        ys.add(initialY);

        // Split
        List<String> parts = splitToParts(paddedAscii, l);

        // Encrypt blocks
        for (int i = 0; i < parts.size(); i++) {
            String inc = incrementModulo2PowL(initialY, i);
            String spnEncrypted = spnEncryptBlock(inc);
            String xored = xorString(spnEncrypted, parts.get(i));
            ys.add(xored);
        }
        return String.join("", ys);
    }

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

    private int[] createRoundKeys(int key, int r, int s) {
        int[] keys = new int[r + 1];
        String bin = toFixedLengthBitString(key, s);
        for (int i = 0; i < keys.length; i++) {
            keys[i] = Integer.parseInt(bin.substring(i * n, i * n + l), 2);
        }
        return keys;
    }

    private String createRandomBitString() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(l);
        for (int i = 0; i < l; i++) {
            sb.append(rand.nextBoolean() ? '1' : '0');
        }
        return sb.toString();
    }

    private String textToAscii(String text) {
        StringBuilder bits = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            int value = text.charAt(i);
            String bin = String.format("%8s", Integer.toBinaryString(value)).replace(' ', '0');
            bits.append(bin);
        }

        return bits.toString();
    }

    private String asciiToText(String bits) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < bits.length(); i += 8) {
            String byteString = bits.substring(i, i + 8);
            int value = Integer.parseInt(byteString, 2);
            text.append((char) value);
        }

        return text.toString();
    }

    private String padAscii(String ascii) {
        StringBuilder sb = new StringBuilder(ascii + "1");
        while (sb.length() % l != 0) {
            sb.append('0');
        }
        return sb.toString();
    }

    private String incrementModulo2PowL(String bits, int i) {
        int val = (Integer.parseInt(bits, 2) + i) % Math.powExact(2, l);
        return toFixedLengthBitString(val, l);
    }

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

    private String permutate(String s) {
        List<String> bits = splitToParts(s, 1);

        List<String> permutated = new ArrayList<>(bits);
        for (int j = 0; j < bits.size(); j++) {
            permutated.set(j, bits.get(bitPermutation[j]));
        }
        return String.join("", permutated);
    }

    private String xorString(String a, String b) {
        int x = Integer.parseInt(a, 2) ^ Integer.parseInt(b, 2);
        return toFixedLengthBitString(x, l);
    }

    private String toFixedLengthBitString(int value, int length) {
        String bin = Integer.toBinaryString(value);
        if (bin.length() > length) {
            bin = bin.substring(bin.length() - length);
        }
        return String.format("%" + length + "s", bin).replace(' ', '0');
    }

    private List<String> splitToParts(String a, int length) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < a.length(); i += length) {
            String chunk = a.substring(i, i + length);
            parts.add(chunk);
        }
        return parts;
    }

    private List<String> sbox(String x, int[] sBox) {
        List<String> parts = splitToParts(x, n);
        parts.replaceAll(string -> toFixedLengthBitString(sBox[Integer.parseInt(string, 2)], n));
        return parts;
    }
}


