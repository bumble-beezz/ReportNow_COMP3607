package com.group22.reportnow.export;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.group22.reportnow.storage.SessionData;

class ExportContextTest {
    @Test
    void throwsExceptionIfNoStrategySet() {
        ExportContext ctx = new ExportContext();
        SessionData data = new SessionData();
        File dummyFile = new File("dummy.txt");
        
        assertThrows(IllegalStateException.class, () -> {
            ctx.export(data, dummyFile);
        });
    }
}