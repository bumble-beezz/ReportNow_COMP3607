package com.group22.reportnow.parser;
// https://bluej.org/doc/extensionsAPI2/bluej/extensions2/BClass.htm
//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html#readString(java.nio.file.Path)
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodCounter {
    private static final Pattern METHOD = Pattern.compile("(?:(?:public|private|protected|static|final|abstract|synchronized|native|default|strictfp)\\s+)+" + "[\\w<>\\[\\],.?]+(?:\\s[\\w<>\\[\\],.?]+)*\\s+" + "\\w+\\s*\\([^)]*\\)");

    public int countMethods(File javaFile) {
        if (javaFile == null || !javaFile.exists()) {
            return 0;
        }
        try {
            String source = Files.readString(javaFile.toPath());
            Matcher m = METHOD.matcher(source);
            int count = 0;
            while (m.find()) {
                count++;
            }
            return count;
        } catch (IOException e) {
            return 0;
        }
    }
}