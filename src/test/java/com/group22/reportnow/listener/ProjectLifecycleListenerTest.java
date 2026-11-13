package com.group22.reportnow.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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

    private PackageEvent makeEvent() throws Exception {
        BProject project = mock(BProject.class);
        when(project.getName()).thenReturn("TestProject");
        when(project.getDir()).thenReturn(testFolder);

        BPackage pkg = mock(BPackage.class);
        when(pkg.getProject()).thenReturn(project);

        PackageEvent event = mock(PackageEvent.class);
        when(event.getPackage()).thenReturn(pkg);

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
}

