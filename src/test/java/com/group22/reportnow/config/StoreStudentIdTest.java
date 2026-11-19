// Student IDs: 816044172
package com.group22.reportnow.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class StoreStudentIdTest {

    @TempDir
    File fakeHome;

    private String realHome;
    private File configDir;
    private File configFile;
    private MockedStatic<JOptionPane> dialog;

    @BeforeEach
    void setUp() {
        realHome = System.getProperty("user.home");
        System.setProperty("user.home", fakeHome.getAbsolutePath());
        configDir = new File(fakeHome, ".reportnow");
        configFile = new File(configDir, "config.properties");
        dialog = mockStatic(JOptionPane.class);
    }

    @AfterEach
    void tearDown() {
        dialog.close();
        System.setProperty("user.home", realHome);
    }

    private void userTypes(String input) {
        dialog.when(() -> JOptionPane.showInputDialog(any(), any(), any(), anyInt(), any(), any(), any()))
              .thenReturn(input);
    }

    private void verifyPromptShown(int count) {
        dialog.verify(() -> JOptionPane.showInputDialog(any(), any(), any(), anyInt(), any(), any(), any()),
                times(count));
    }

    private void writeSavedId(String id) throws Exception {
        assertTrue(configDir.mkdirs());
        Properties props = new Properties();
        props.setProperty("student_id", id);
        try (FileOutputStream out = new FileOutputStream(configFile)) {
            props.store(out, "test");
        }
    }

    private String readSavedId() throws Exception {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(configFile)) {
            props.load(in);
        }
        return props.getProperty("student_id");
    }

    @Test
    void returnsAStudentId() {
        StoreStudentId store = new StoreStudentId();
        assertNotNull(store);
    }

    @Test
    void savedIdIsReturnedWithoutPrompting() throws Exception {
        writeSavedId("816000001");
        assertEquals("816000001", new StoreStudentId().getStudentId());
        dialog.verify(() -> JOptionPane.showInputDialog(any(), any(), any(), anyInt(), any(), any(), any()),
                never());
    }

    @Test
    void enteredIdIsTrimmedAndSaved() throws Exception {
        userTypes("  816000002  ");

        assertEquals("816000002", new StoreStudentId().getStudentId());
        assertTrue(configFile.exists());
        assertEquals("816000002", readSavedId());
    }

    @Test
    void savedIdIsReusedInLaterSessions() {
        userTypes("816000003");
        new StoreStudentId().getStudentId();
        assertEquals("816000003", new StoreStudentId().getStudentId());
        verifyPromptShown(1);
    }

    @Test
    void cancellingPromptReturnsUnknownStudent() {
        userTypes(null);

        assertEquals("UNKNOWN_STUDENT", new StoreStudentId().getStudentId());
        assertFalse(configFile.exists());
    }

    @Test
    void blankEntryReturnsUnknownStudent() {
        userTypes("   ");

        assertEquals("UNKNOWN_STUDENT", new StoreStudentId().getStudentId());
        assertFalse(configFile.exists());
    }

    @Test
    void blankSavedIdCausesPrompt() throws Exception {
        writeSavedId("   ");
        userTypes("816000004");

        assertEquals("816000004", new StoreStudentId().getStudentId());
        verifyPromptShown(1);
        assertEquals("816000004", readSavedId());
    }

    @Test
    void unreadableAndUnwritableConfigIsHandled() {
        assertTrue(configFile.mkdirs());
        userTypes("816000005");

        StoreStudentId store = assertDoesNotThrow(StoreStudentId::new);
        assertEquals("816000005", assertDoesNotThrow(store::getStudentId));
    }
}
