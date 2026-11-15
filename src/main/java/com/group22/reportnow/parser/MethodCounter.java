//Student IDs: 400017923, 816044172

package com.group22.reportnow.parser;
// https://bluej.org/doc/extensionsAPI2/bluej/extensions2/BClass.html
//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html#readString(java.nio.file.Path)
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Counts the methods found in a Java source file.
 * This helps ReportNow check whether methods have been added or removed.
 */
public class MethodCounter {
    /** Stores the pattern used to find possible method declarations.*/
    private static final Pattern METHOD = Pattern.compile("(?:(?:public|private|protected|static|final|abstract|synchronized|native|default|strictfp)\\s+)+" + "[\\w<>\\[\\],.?]+(?:\\s[\\w<>\\[\\],.?]+)*\\s+" + "\\w+\\s*\\([^)]*\\)");

    /**
     * Reads a Java file and counts method declarations using a pattern.
     * @param javaFile the Java source file being checked
     * @return the number of methods found, or 0 if the file cannot be read
    */
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