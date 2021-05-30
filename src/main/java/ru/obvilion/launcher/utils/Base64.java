package ru.obvilion.launcher.utils;

public class Base64 {
    public static String encrypt(String text, String keyWord) {
        byte[] arr = text.getBytes();
        byte[] keyarr = keyWord.getBytes();
        byte[] result = new byte[arr.length];

        for(int i = 0; i< arr.length; i++) {
            result[i] = (byte) (arr[i] ^ keyarr[i % keyarr.length]);
        }

        return new String(result);
    }

    public static String decrypt(String in, String keyWord) {
        byte[] text = in.getBytes();
        byte[] result  = new byte[text.length];
        byte[] keyarr = keyWord.getBytes();

        for(int i = 0; i < text.length;i++) {
            result[i] = (byte) (text[i] ^ keyarr[i% keyarr.length]);
        }

        return new String(result);
    }
}
