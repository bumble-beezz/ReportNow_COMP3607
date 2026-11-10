package com.group22.reportnow.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.group22.reportnow.model.CompileError;

public class ParseCompileErrors {

    private static final Pattern LINE = Pattern.compile(
            "^(.+?):(\\d+):\\s*(error|warning|note):\\s*(.+)$",
            Pattern.CASE_INSENSITIVE);

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