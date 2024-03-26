package ru.fcpsr.domainsport.utils;

public class StringGenerator {
    public static String getGeneratedString(int length){
        String randomWord = "";
        for(int i=0; i<length; i++){
            char ch = (char) Math.round(65 + (Math.random() * 25.0f));
            randomWord = randomWord + ch;
        }
        return randomWord;
    }
}
