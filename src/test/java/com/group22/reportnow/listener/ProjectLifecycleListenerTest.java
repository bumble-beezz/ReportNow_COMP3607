// Student IDs: 400017923, 816039066

package com.group22.reportnow.listener;

import java.io.File;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.event.PackageEvent;

public class ProjectLifecycleListenerTest {
    private SessionData data;
    private ProjectLifecycleListener listener;
    private Consumer<String> onClose;

    @TempDir
    File testFolder;

    @BeforeEach
    void setup() {
        data = mock(SessionData.class);
        onClose = name -> { };
        listener = new ProjectLifecycleListener(data, onClose);
    }

    @AfterEach
    void tearDown() {
        listener.stopTimer();
    }

    private PackageEvent makeEvent() throws Exception {
        return makeEvent(testFolder);
    }

    private PackageEvent makeEvent(File folder) throws Exception {
        BProject project = mock(BProject.class);
        when(project.getName()).thenReturn("TestProject");
        when(project.getDir()).thenReturn(folder);

        BPackage pkg = mock(BPackage.class);
        when(pkg.getProject()).thenReturn(project);

        PackageEvent event = mock(PackageEvent.class);
        when(event.getPackage()).thenReturn(pkg);

        return event;
    }

    private PackageEvent brokenEvent() {
        PackageEvent event = mock(PackageEvent.class);
        when(event.getPackage()).thenThrow(new RuntimeException("BlueJ unavailable"));
        return event;
    }

    @Test
    void projectCreated() throws Exception {
        listener.packageOpened(makeEvent());

        verify(data).recordProject("ProjectCreated", "TestProject");
    }

    @Test
    void projectOpened() throws Exception {
        listener.packageOpened(makeEvent());

        verify(data).recordProject("ProjectOpened", "TestProject");
    }

    @Test
    void projectModification() {
        listener.recordProjectModified("TestProject");

        verify(data).recordProject("ProjectModified", "TestProject");
    }

    @Test
    void projectDeleted() throws Exception {
        listener.packageOpened(makeEvent());

        assertTrue(testFolder.delete());
        listener.checkProjects();

        verify(data).recordProject("ProjectDeleted", "TestProject");
    }

    @Test
    void projectClosing() throws Exception {
        final String[] closedProject = {null};

        ProjectLifecycleListener closingListener = new ProjectLifecycleListener(data, name -> closedProject[0] = name);

        closingListener.packageClosing(makeEvent());

        assertEquals("TestProject", closedProject[0]);
    }

    @Test
    void getClassListenerReturnsSameInstance() {
        assertNotNull(listener.getClassListener());
        assertSame(listener.getClassListener(), listener.getClassListener());
    }

    @Test
    void openingSameProjectTwiceRecordsOneOpenAndOneCreate() throws Exception {
        PackageEvent event = makeEvent();
        listener.packageOpened(event);
        listener.packageOpened(event);

        verify(data, times(1)).recordProject("ProjectOpened", "TestProject");
        verify(data, times(1)).recordProject("ProjectCreated", "TestProject");
    }

    @Test
    void reopeningClosedProjectRecordsOpenAgainButNotCreate() throws Exception {
        PackageEvent event = makeEvent();
        listener.packageOpened(event);
        listener.packageClosing(event);
        listener.packageOpened(event);

        verify(data, times(2)).recordProject("ProjectOpened", "TestProject");
        verify(data, times(1)).recordProject("ProjectCreated", "TestProject");
    }

    @Test
    void unreadableFolderIsNotTreatedAsNewProject() throws Exception {
        File missing = new File(testFolder, "does-not-exist");

        listener.packageOpened(makeEvent(missing));

        verify(data, never()).recordProject("ProjectCreated", "TestProject");
        verify(data).recordProject("ProjectOpened", "TestProject");
    }

    @Test
    void packageOpenedHandlesBlueJErrors() {
        assertDoesNotThrow(() -> listener.packageOpened(brokenEvent()));
        verify(data, never()).recordProject(anyString(), anyString());
    }

    @Test
    void packageClosingWithoutCallbackDoesNotThrow() throws Exception {
        ProjectLifecycleListener noCallback = new ProjectLifecycleListener(data, null);
        PackageEvent event = makeEvent();

        assertDoesNotThrow(() -> noCallback.packageClosing(event));
    }

    @Test
    void packageClosingHandlesBlueJErrors() {
        final String[] closedProject = {null};
        ProjectLifecycleListener closingListener = new ProjectLifecycleListener(data, name -> closedProject[0] = name);

        assertDoesNotThrow(() -> closingListener.packageClosing(brokenEvent()));
        assertNull(closedProject[0]);
    }

    @Test
    void existingProjectIsNotReportedAsDeleted() throws Exception {
        listener.packageOpened(makeEvent());
        listener.checkProjects();

        verify(data, never()).recordProject("ProjectDeleted", "TestProject");
    }

    @Test
    void deletedProjectIsOnlyReportedOnce() throws Exception {
        listener.packageOpened(makeEvent());
        assertTrue(testFolder.delete());

        listener.checkProjects();
        listener.checkProjects();

        verify(data, times(1)).recordProject("ProjectDeleted", "TestProject");
    }

    private void waitForProjectEvent(String action) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            boolean recorded = mockingDetails(data).getInvocations().stream()
                    .anyMatch(i -> i.getMethod().getName().equals("recordProject")
                            && action.equals(i.getArgument(0)));
            if (recorded) {
                return;
            }
            Thread.sleep(100);
        }
    }

    @Test
    void timerDetectsDeletedProject() throws Exception {
        listener.packageOpened(makeEvent());
        assertTrue(testFolder.delete());

        listener.startTimer();
        listener.startTimer();

        waitForProjectEvent("ProjectDeleted");
        listener.stopTimer();
        verify(data, times(1)).recordProject("ProjectDeleted", "TestProject");
    }

    @Test
    void timerKeepsRunningWhenACheckFails() throws Exception {
        doThrow(new RuntimeException("storage failure"))
                .when(data).recordProject("ProjectDeleted", "TestProject");
        listener.packageOpened(makeEvent());
        assertTrue(testFolder.delete());

        listener.startTimer();

        waitForProjectEvent("ProjectDeleted");
        listener.stopTimer();
        verify(data, times(1)).recordProject("ProjectDeleted", "TestProject");
    }

    @Test
    void stopTimerIsSafeWhenNotStarted() {
        assertDoesNotThrow(() -> listener.stopTimer());
        listener.startTimer();
        assertDoesNotThrow(() -> listener.stopTimer());
        assertDoesNotThrow(() -> listener.stopTimer());
    }
}
