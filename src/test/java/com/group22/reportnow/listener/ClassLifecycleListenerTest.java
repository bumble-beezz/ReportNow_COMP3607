package com.group22.reportnow.listener;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.group22.reportnow.storage.SessionData;

public class ClassLifecycleListenerTest {
    private SessionData data;
    private ClassLifecycleListener listener;

    @TempDir
    File testFolder;

    @BeforeEach
    void setup() {
        data = mock(SessionData.class);
        listener = new ClassLifecycleListener(data);
    }

    @Test
    void classCreated() throws Exception {
        listener.trackProject(testFolder, "TestProject");
        File file = new File(testFolder, "Student.java");
        assertTrue(file.createNewFile());
        listener.checkFiles();
        verify(data).recordClass("ClassCreated", "Student.java", "TestProject");
    }

    @Test
    void classModification() throws Exception {
        File file = new File(testFolder, "Student.java");
        Files.writeString(file.toPath(), "public class Student {}");

        listener.trackProject(testFolder, "TestProject");

        long oldTime = file.lastModified();
        Files.writeString(file.toPath(), "public class Student { int age; }");
        Files.setLastModifiedTime(file.toPath(), FileTime.fromMillis(oldTime + 5000));

        listener.checkFiles();

        verify(data).recordClass("ClassModified", "Student.java", "TestProject");
    }

    @Test
    void classDeleted() throws Exception {
        File file = new File(testFolder, "Student.java");
        assertTrue(file.createNewFile());

        listener.trackProject(testFolder, "TestProject");

        assertTrue(file.delete());
        listener.checkFiles();

        verify(data).recordClass("ClassDeleted", "Student.java", "TestProject");
    }
    @Test
    void existingClass() throws Exception {
        File file = new File(testFolder, "Student.java");
        assertTrue(file.createNewFile());

        listener.trackProject(testFolder, "TestProject");
        listener.checkFiles();

        verify(data, never()).recordClass("ClassCreated", "Student.java", "TestProject");
    }

    @Test
    void projectChange() throws Exception {
        listener.trackProject(testFolder, "TestProject");
        File file = new File(testFolder, "Student.java");
        assertTrue(file.createNewFile());
        listener.checkFiles();
        verify(data).recordProject("ProjectModified", "TestProject");
    }

    @Test
    void methodAddedRecordsMethodAddedEvent() throws Exception {
        File file = new File(testFolder, "Student.java");
        Files.writeString(file.toPath(), "public class Student {\n" + "    public void methodOne() {}\n" + "}\n");

        listener.trackProject(testFolder, "TestProject");
        long oldTime = file.lastModified();

        Files.writeString(file.toPath(), "public class Student {\n" + "    public void methodOne() {}\n" + "    public void methodTwo() {}\n" + "}\n");
        Files.setLastModifiedTime(file.toPath(), FileTime.fromMillis(oldTime + 5000));

        listener.checkFiles();

        verify(data).recordClass("MethodAdded", "Student.java", "TestProject");
        verify(data, never()).recordClass("ClassModified", "Student.java", "TestProject");
    }

    @Test
    void methodRemovedRecordsMethodRemovedEvent() throws Exception {
        File file = new File(testFolder, "Student.java");
        Files.writeString(file.toPath(), "public class Student {\n" + "    public void methodOne() {}\n" + "    public void methodTwo() {}\n" + "}\n");

        listener.trackProject(testFolder, "TestProject");
        long oldTime = file.lastModified();

        Files.writeString(file.toPath(), "public class Student {\n" + "    public void methodOne() {}\n" + "}\n");
        Files.setLastModifiedTime(file.toPath(), FileTime.fromMillis(oldTime + 5000));

        listener.checkFiles();

        verify(data).recordClass("MethodRemoved", "Student.java", "TestProject");
        verify(data, never()).recordClass("ClassModified", "Student.java", "TestProject");
    }
}