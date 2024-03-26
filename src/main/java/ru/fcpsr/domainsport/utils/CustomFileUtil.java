package ru.fcpsr.domainsport.utils;

import java.nio.file.Path;
import java.util.Optional;

public class CustomFileUtil {
    private static final float ONE_MB = 1048576.0f;
    public static Optional<String> getExtension(String filename){
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public static Path prepareFilePath(String filename){
        String extension = CustomFileUtil.getExtension(filename).orElse("not");
        String randomWord = "";
        for(int i=0; i<20; i++){
            char ch = (char) Math.round(65 + (Math.random() * 25.0f));
            randomWord = randomWord + ch;
        }
        String uid = randomWord + "." + extension;
        return Path.of("./src/main/resources/static/img/" + uid);
    }

    public static float getMegaBytes(int bitsAmount){
        return bitsAmount / ONE_MB;
    }
}
