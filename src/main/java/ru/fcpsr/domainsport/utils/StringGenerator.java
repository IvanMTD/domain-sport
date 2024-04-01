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

    public static String generatePassword(int length){
        StringBuilder randomWord = new StringBuilder();
        for(int i=0; i<length; i++) {
            if (Math.round(Math.random() * 2.0f) >= 1) {
                char ch = (char) Math.round(65 + (Math.random() * 25.0f));
                randomWord.append(ch);
            } else {
                char ch = (char) Math.round(97 + (Math.random() * 25.0f));
                randomWord.append(ch);
            }
        }
        return randomWord.toString();
    }
}
