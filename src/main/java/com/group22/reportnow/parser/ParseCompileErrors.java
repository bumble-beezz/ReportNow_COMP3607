//Student IDs: 816044172, 400017923, 816039066


//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html
//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Matcher.html

package com.group22.reportnow.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.group22.reportnow.model.CompileError;

/**
 * Reads compilation messages and extracts error or warning details.
 * It stores the information as CompileError objects.
 */
public class ParseCompileErrors {
    /**
     * Stores the pattern used to find the filename, line number, message type and description in a compilation message.
    */
    private static final Pattern LINE = Pattern.compile(
            "^(.+?):(\\d+):\\s*(error|warning|note):\\s*(.+)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Reads compilation messages and separates them into error details.
     * Lines that do not match the expected format are skipped.
     * @param rawMessage the compilation message to be checked
     * @return a list of compilation errors and warnings found
    */    
    public List<CompileError> parse(String rawMessage) {
        List<CompileError> result = new ArrayList<>();
        if (rawMessage ==null || rawMessage.isEmpty()) {
            return result;
        }
        String[] lines = rawMessage.split("\\R");
        for (String line : lines) {
            Matcher m = LINE.matcher(line.trim());
            if (!m.matches()) {
                continue;
            }
            result.add(new CompileError(
                    m.group(1).trim(),
                    Integer.parseInt(m.group(2)),
                    m.group(3).toUpperCase(),
                    m.group(4).trim()
            ));
        }
        return result;
    }
}