package com.group22.reportnow.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MethodCounterTest {

    private final MethodCounter counter = new MethodCounter();

    @TempDir
    Path tempDir;

    private File write(String content) throws IOException {
        File f = tempDir.resolve("Test" + System.nanoTime() + ".java").toFile();
        Files.writeString(f.toPath(), content);
        return f;
    }

    @Test
    void countsZeroMethodsInEmptyClass() throws IOException {
        assertEquals(0, counter.countMethods(write("public class Test {}")));
    }

    @Test
    void countsSingleMethod() throws IOException {
        assertEquals(1, counter.countMethods(write(
                "public class Test { public void foo() {} }")));
    }

    @Test
    void countsMultipleMethods() throws IOException {
        File f = write( "public class Test {\n" + "    public void foo() {}\n" + "    public int bar() { return 1; }\n" + "    private String baz(String s) { return s; }\n" + "}\n");
        assertEquals(3, counter.countMethods(f));
    }

    @Test
    void countsStaticMethods() throws IOException {
        assertEquals(1, counter.countMethods(write(
                "public class Test { public static void main(String[] args) {} }")));
    }

    @Test
    void countsMethodsWithMultipleModifiers() throws IOException {
        File f = write( "public class Test {\n" + "    public static final synchronized void foo() {}\n" + "    private static native int bar(long x);\n" + "}\n");
        assertEquals(2, counter.countMethods(f));
    }

    @Test
    void countsGenericReturnTypes() throws IOException {
        File f = write( "public class Test {\n" + "    public java.util.List<String> list() { return null; }\n" +  "    public java.util.Map<String, Integer> map() { return null; }\n" + "}\n");
        assertEquals(2, counter.countMethods(f));
    }

    @Test
    void ignoresConstructors() throws IOException {
        File f = write( "public class Test {\n" + "    public Test() {}\n" + "    public Test(int x) {}\n" + "}\n");
        assertEquals(0, counter.countMethods(f));
    }

    @Test
    void ignoresFields() throws IOException {
        File f = write( "public class Test {\n" +  "    public int x = 5;\n" + "    private String name;\n" + "}\n");
        assertEquals(0, counter.countMethods(f));
    }

    @Test
    void ignoresMethodCallsInsideBodies() throws IOException {
        File f = write( "public class Test {\n" + "    public void foo() {\n" + "        helper();\n" + "        int x = compute();\n" + "    }\n" + "    public void helper() {}\n" + "    public int compute() { return 0; }\n" + "}\n");
        assertEquals(3, counter.countMethods(f));
    }

    @Test
    void returnsZeroForNull() {
        assertEquals(0, counter.countMethods(null));
    }

    @Test
    void returnsZeroForMissingFile() {
        assertEquals(0, counter.countMethods(new File("does-not-exist.java")));
    }
}