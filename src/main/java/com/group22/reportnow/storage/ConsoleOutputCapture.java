package com.group22.reportnow.storage;
//https://www.bluej.org/extensions/writingextensions2.html
//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/io/PrintStream.html
//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;

public class ConsoleOutputCapture {
    private static final Pattern[] INTERNAL_PATTERNS = {
        Pattern.compile("^\\[ReportNow\\].*"),
        Pattern.compile("^WARNING:.*"),
        Pattern.compile("^Could not .*"),
        Pattern.compile("^Error checking .*"),
        Pattern.compile("^BlueJ.*"),
        Pattern.compile("^Debug.*"),
        Pattern.compile("^org\\.apache\\..*"),
        Pattern.compile("^\\s+at org\\.apache\\..*"),
        Pattern.compile("^java\\.net\\..*"),
        Pattern.compile("^\\s+at java\\.net\\..*"),
        Pattern.compile("^\\s+at java\\.base\\..*")
    };

    private final StringBuilder buffer = new StringBuilder();
    private final PrintStream originalOut;
    private final PrintStream originalErr;
    private PrintStream capturingStream;

    public ConsoleOutputCapture() {
        this.originalOut = System.out;
        this.originalErr = System.err;
    }

    public synchronized void start() {
        if (capturingStream != null) return;
        capturingStream = new PrintStream(new TeeOutputStream(buffer, originalOut), true);
        System.setOut(capturingStream);
        System.setErr(capturingStream);
    }

    public synchronized void stop() {
        if (capturingStream == null) return;
        System.setOut(originalOut);
        System.setErr(originalErr);
        capturingStream= null;
    }

    public synchronized String getCapturedOutput() {
        String raw = buffer.toString();
        if (raw.isEmpty()) {
            return "";
        }
        StringBuilder filtered = new StringBuilder();
        String[] lines = raw.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (isInternal(line)) continue;
            filtered.append(line);
            if (i < lines.length - 1) {
                filtered.append("\n");
            }
        }
        return filtered.toString();
    }

    public synchronized void writeToFile(File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(getCapturedOutput().getBytes());
        }
    }

    private boolean isInternal(String line) {
        for (Pattern p : INTERNAL_PATTERNS) {
            if (p.matcher(line).matches()) return true;
        }
        return false;
    }

    private static class TeeOutputStream extends OutputStream {
        private final StringBuilder buffer;
        private final PrintStream original;

        TeeOutputStream(StringBuilder buffer, PrintStream original) {
            this.buffer = buffer;
            this.original= original;
        }

        @Override
        public void write(int b) throws IOException {
            buffer.append((char) b);
            original.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            buffer.append(new String(b, off, len));
            original.write(b, off, len);
        }
        @Override
        public void flush() throws IOException {
            original.flush();
        }
    }
}