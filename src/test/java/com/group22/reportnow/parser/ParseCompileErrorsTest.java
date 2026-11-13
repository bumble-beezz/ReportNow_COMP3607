package com.group22.reportnow.parser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.group22.reportnow.model.CompileError;

public class ParseCompileErrorsTest {

    private final ParseCompileErrors parser = new ParseCompileErrors();
    @Test
    void parsesSingleErrorLine() {
        List<CompileError> out = parser.parse("Foo.java:15: error: cannot find symbol");
        assertEquals(1, out.size());
        assertEquals("Foo.java", out.get(0).getFileName());
        assertEquals(15, out.get(0).getLineNumber());
        assertEquals("ERROR", out.get(0).getErrorCode());
        assertEquals("cannot find symbol", out.get(0).getMessage());
    }

    @Test
    void parsesMultipleLines() {
        List<CompileError>out = parser.parse( "A.java:1: error: boom\n" + "B.java:2: warning: unused\n" + "C.java:3: note: hint");
        assertEquals(3, out.size());
        assertEquals("A.java", out.get(0).getFileName());
        assertEquals("B.java", out.get(1).getFileName());
        assertEquals("C.java", out.get(2).getFileName());
    }

    @Test
    void ignoresNonMatchingLines() {
        List<CompileError> out = parser.parse( "just random text\n" + "Foo.java:5: error: real one");
        assertEquals(1, out.size());
        assertEquals("Foo.java", out.get(0).getFileName());
    }

    @Test
    void handlesNullAndEmpty() {
        assertTrue(parser.parse(null).isEmpty());
        assertTrue(parser.parse("").isEmpty());
    }
}