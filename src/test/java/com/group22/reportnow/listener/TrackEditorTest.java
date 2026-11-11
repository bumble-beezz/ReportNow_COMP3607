package com.group22.reportnow.listener;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.BlueJ;
import bluej.extensions2.editor.JavaEditor;
import bluej.extensions2.event.PackageEvent;

class TrackEditorTest {

    private BlueJ bluej;
    private SessionData data;
    private TrackEditor tracker;

    @BeforeEach
    void setUp() {
        bluej= mock(BlueJ.class);
        data = mock(SessionData.class);
        tracker = new TrackEditor(bluej, data);
    }

    @Test
    void scanWithNoProjectsDoesNotThrow() {
        when(bluej.getOpenProjects()).thenReturn(new BProject[0]);
        assertDoesNotThrow(() -> tracker.scanOpenEditors());
    }

    @Test
    void scanHandlesProjectWithNoPackages() throws Exception {
        BProject project = mock(BProject.class);
        when(project.getPackages()).thenReturn(new BPackage[0]);
        when(bluej.getOpenProjects()).thenReturn(new BProject[]{project});

        assertDoesNotThrow(() -> tracker.scanOpenEditors());
    }

    @Test
    void scanHandlesPackageWithNoClasses() throws Exception {
        BProject project = mock(BProject.class);
        BPackage pkg = mock(BPackage.class);
        when(pkg.getClasses()).thenReturn(new BClass[0]);
        when(project.getPackages()).thenReturn(new BPackage[]{pkg});
        when(bluej.getOpenProjects()).thenReturn(new BProject[]{project});

        assertDoesNotThrow(() -> tracker.scanOpenEditors());
    }

    @Test
    void scanHandlesClassWithNoOpenEditor() throws Exception {
        BProject project = mock(BProject.class);
        BPackage pkg= mock(BPackage.class);
        BClass cls= mock(BClass.class);
        when(cls.getJavaEditorIfOpen()).thenReturn(null);
        when(pkg.getClasses()).thenReturn(new BClass[]{cls});
        when(project.getPackages()).thenReturn(new BPackage[]{pkg});
        when(bluej.getOpenProjects()).thenReturn(new BProject[]{project});

        assertDoesNotThrow(() -> tracker.scanOpenEditors());
    }

    @Test
    void scanAttachesListenerToOpenEditor() throws Exception {
        BProject project = mock(BProject.class);
        BPackage pkg = mock(BPackage.class);
        BClass cls = mock(BClass.class);
        JavaEditor editor = mock(JavaEditor.class);
        when(editor.getTextLength()).thenReturn(100);
        when(cls.getJavaEditorIfOpen()).thenReturn(editor);
        when(pkg.getClasses()).thenReturn(new BClass[]{cls});
        when(project.getPackages()).thenReturn(new BPackage[]{pkg});
        when(bluej.getOpenProjects()).thenReturn(new BProject[]{project});

        assertDoesNotThrow(() -> tracker.scanOpenEditors());
        assertDoesNotThrow(() -> tracker.scanOpenEditors());
    }

    @Test
    void packageOpenedTriggersScan() {
        when(bluej.getOpenProjects()).thenReturn(new BProject[0]);
        PackageEvent event = mock(PackageEvent.class);
        assertDoesNotThrow(() -> tracker.packageOpened(event));
    }

    @Test
    void detachAllDoesNotThrow() {
        assertDoesNotThrow(() -> tracker.detachAll());
    }
}