// Student IDs: 400017923, 816044172, 816039066

package com.group22.reportnow.listener;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.event.ClassEvent;

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

    private ClassEvent classEvent(File projectFolder, String className, String oldName) throws Exception {
        BProject project = mock(BProject.class);
        when(project.getDir()).thenReturn(projectFolder);

        BPackage pkg = mock(BPackage.class);
        when(pkg.getProject()).thenReturn(project);
        when(pkg.getDir()).thenReturn(projectFolder);

        BClass cls = mock(BClass.class);
        when(cls.getName()).thenReturn(className);

        ClassEvent event = mock(ClassEvent.class);
        when(event.getPackage()).thenReturn(pkg);
        when(event.getBClass()).thenReturn(cls);
        when(event.getOldName()).thenReturn(oldName);
        return event;
    }

    private ClassEvent brokenEvent() {
        ClassEvent event = mock(ClassEvent.class);
        when(event.getPackage()).thenThrow(new RuntimeException("BlueJ unavailable"));
        return event;
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

    @Test
    void classesInSubfoldersAreDetected() throws Exception {
        listener.trackProject(testFolder, "TestProject");
        File sub = new File(testFolder, "shapes");
        assertTrue(sub.mkdir());
        assertTrue(new File(sub, "Circle.java").createNewFile());

        listener.checkFiles();

        verify(data).recordClass("ClassCreated", "Circle.java", "TestProject");
    }

    @Test
    void nonJavaAndShellFilesAreIgnored() throws Exception {
        listener.trackProject(testFolder, "TestProject");
        assertTrue(new File(testFolder, "README.txt").createNewFile());
        assertTrue(new File(testFolder, "__SHELL1.java").createNewFile());

        listener.checkFiles();

        verify(data, never()).recordClass(anyString(), anyString(), anyString());
    }

    @Test
    void trackingSameProjectTwiceDoesNotRescan() throws Exception {
        listener.trackProject(testFolder, "TestProject");
        assertTrue(new File(testFolder, "Student.java").createNewFile());
        listener.trackProject(testFolder, "TestProject");
        listener.checkFiles();

        verify(data, times(1)).recordClass("ClassCreated", "Student.java", "TestProject");
    }

    @Test
    void unchangedFileIsNotReportedTwice() throws Exception {
        listener.trackProject(testFolder, "TestProject");
        assertTrue(new File(testFolder, "Student.java").createNewFile());

        listener.checkFiles();
        listener.checkFiles();

        verify(data, times(1)).recordClass("ClassCreated", "Student.java", "TestProject");
        verify(data, never()).recordClass(eq("ClassModified"), anyString(), anyString());
    }

    @Test
    void filesInDeletedProjectAreNotReportedAsClassDeletions() throws Exception {
        File project = new File(testFolder, "MyProject");
        assertTrue(project.mkdir());
        File file = new File(project, "Student.java");
        assertTrue(file.createNewFile());
        listener.trackProject(project, "MyProject");

        assertTrue(file.delete());
        assertTrue(project.delete());
        listener.checkFiles();

        verify(data, never()).recordClass(eq("ClassDeleted"), anyString(), anyString());
    }

    @Test
    void trackingAFileInsteadOfAFolderDoesNotThrow() throws Exception {
        File notAFolder = new File(testFolder, "Student.java");
        assertTrue(notAFolder.createNewFile());

        assertDoesNotThrow(() -> listener.trackProject(notAFolder, "TestProject"));
        assertDoesNotThrow(() -> listener.checkFiles());
    }

    @Test
    void classStateChangedDoesNothing() {
        assertDoesNotThrow(() -> listener.classStateChanged(mock(ClassEvent.class)));
        verify(data, never()).recordClass(anyString(), anyString(), anyString());
    }

    @Test
    void classRemovedRecordsDeletion() throws Exception {
        assertTrue(new File(testFolder, "Student.java").createNewFile());
        listener.trackProject(testFolder, "TestProject");

        listener.classRemoved(classEvent(testFolder, "Student", null));

        verify(data).recordClass("ClassDeleted", "Student.java", "TestProject");
        verify(data).recordProject("ProjectModified", "TestProject");
    }

    @Test
    void classRemovedHandlesPackageQualifiedNames() throws Exception {
        assertTrue(new File(testFolder, "Student.java").createNewFile());
        listener.trackProject(testFolder, "TestProject");

        listener.classRemoved(classEvent(testFolder, "school.Student", null));

        verify(data).recordClass("ClassDeleted", "Student.java", "TestProject");
    }

    @Test
    void classRemovedThenFileDeletedIsOnlyReportedOnce() throws Exception {
        File file = new File(testFolder, "Student.java");
        assertTrue(file.createNewFile());
        listener.trackProject(testFolder, "TestProject");

        listener.classRemoved(classEvent(testFolder, "Student", null));
        assertTrue(file.delete());
        listener.checkFiles();

        verify(data, times(1)).recordClass("ClassDeleted", "Student.java", "TestProject");
    }

    @Test
    void classRemovedIgnoresUntrackedClass() throws Exception {
        listener.trackProject(testFolder, "TestProject");

        listener.classRemoved(classEvent(testFolder, "Ghost", null));

        verify(data, never()).recordClass(anyString(), anyString(), anyString());
    }

    @Test
    void classRemovedIgnoresUntrackedProject() throws Exception {
        assertTrue(new File(testFolder, "Student.java").createNewFile());

        listener.classRemoved(classEvent(testFolder, "Student", null));

        verify(data, never()).recordClass(anyString(), anyString(), anyString());
    }

    @Test
    void classRemovedHandlesBlueJErrors() {
        assertDoesNotThrow(() -> listener.classRemoved(brokenEvent()));
    }

    @Test
    void classRenameIsRecordedAsModification() throws Exception {
        File oldFile = new File(testFolder, "Student.java");
        assertTrue(oldFile.createNewFile());
        listener.trackProject(testFolder, "TestProject");

        File newFile = new File(testFolder, "Pupil.java");
        assertTrue(oldFile.renameTo(newFile));
        listener.classNameChanged(classEvent(testFolder, "Pupil", "Student"));

        verify(data).recordClass("ClassModified", "Pupil.java", "TestProject");
    }

    @Test
    void renamedClassIsNotReportedAsDeletedAndCreated() throws Exception {
        File oldFile = new File(testFolder, "Student.java");
        assertTrue(oldFile.createNewFile());
        listener.trackProject(testFolder, "TestProject");

        assertTrue(oldFile.renameTo(new File(testFolder, "Pupil.java")));
        listener.classNameChanged(classEvent(testFolder, "Pupil", "Student"));
        listener.checkFiles();

        verify(data, never()).recordClass("ClassDeleted", "Student.java", "TestProject");
        verify(data, never()).recordClass("ClassCreated", "Pupil.java", "TestProject");
    }

    @Test
    void renameWhenNewFileMissingStillRecordsModification() throws Exception {
        File oldFile = new File(testFolder, "Student.java");
        assertTrue(oldFile.createNewFile());
        listener.trackProject(testFolder, "TestProject");

        listener.classNameChanged(classEvent(testFolder, "Pupil", "Student"));

        verify(data).recordClass("ClassModified", "Pupil.java", "TestProject");
    }

    @Test
    void renameOfUntrackedClassIsIgnored() throws Exception {
        listener.trackProject(testFolder, "TestProject");

        listener.classNameChanged(classEvent(testFolder, "Pupil", "Ghost"));

        verify(data, never()).recordClass(anyString(), anyString(), anyString());
    }

    @Test
    void renameInUntrackedProjectIsIgnored() throws Exception {
        assertTrue(new File(testFolder, "Student.java").createNewFile());

        listener.classNameChanged(classEvent(testFolder, "Pupil", "Student"));

        verify(data, never()).recordClass(anyString(), anyString(), anyString());
    }

    @Test
    void classNameChangedHandlesBlueJErrors() {
        assertDoesNotThrow(() -> listener.classNameChanged(brokenEvent()));
    }
}
