//Student IDs: 816039066, 816044172

package com.group22.reportnow.listener;

import java.io.File;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.BlueJ;
import bluej.extensions2.ProjectNotOpenException;
import bluej.extensions2.editor.JavaEditor;
import bluej.extensions2.event.PackageEvent;

public class TrackEditorTest {

    private BlueJ bluej;
    private SessionData data;
    private TrackEditor tracker;

    @BeforeEach
    void setUp() {
        bluej= mock(BlueJ.class);
        data = mock(SessionData.class);
        tracker = new TrackEditor(bluej, data);
    }

    private void openProjectWith(BClass cls) throws Exception {
        BProject project = mock(BProject.class);
        BPackage pkg = mock(BPackage.class);
        when(pkg.getClasses()).thenReturn(new BClass[]{cls});
        when(project.getPackages()).thenReturn(new BPackage[]{pkg});
        when(bluej.getOpenProjects()).thenReturn(new BProject[]{project});
    }

    private BClass classWithOpenEditor(JavaEditor editor) throws Exception {
        BClass cls = mock(BClass.class);
        when(cls.getJavaEditorIfOpen()).thenReturn(editor);
        when(cls.getJavaFile()).thenReturn(new File("Foo.java"));
        return cls;
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
        JavaEditor editor = mock(JavaEditor.class);
        when(editor.getTextLength()).thenReturn(100);
        openProjectWith(classWithOpenEditor(editor));
        tracker.scanOpenEditors();
        tracker.scanOpenEditors();
        verify(editor, times(3)).getTextLength();
        verify(data, never()).recordEdit(any(Instant.class));
    }

    @Test
    void packageOpenedTriggersScan() {
        when(bluej.getOpenProjects()).thenReturn(new BProject[0]);
        PackageEvent event = mock(PackageEvent.class);
        assertDoesNotThrow(() -> tracker.packageOpened(event));
        verify(bluej).getOpenProjects();
    }

    @Test
    void detachAllDoesNotThrow() {
        assertDoesNotThrow(() -> tracker.detachAll());
    }

    @Test
    void editInOpenEditorIsRecorded() throws Exception {
        JavaEditor editor = mock(JavaEditor.class);
        when(editor.getTextLength()).thenReturn(100);
        openProjectWith(classWithOpenEditor(editor));

        tracker.scanOpenEditors();
        when(editor.getTextLength()).thenReturn(150);
        tracker.scanOpenEditors();

        verify(data, times(1)).recordEdit(any(Instant.class));
    }

    @Test
    void editorWithoutSourceFileIsIgnored() throws Exception {
        JavaEditor editor = mock(JavaEditor.class);
        when(editor.getTextLength()).thenReturn(100);
        BClass cls = mock(BClass.class);
        when(cls.getJavaEditorIfOpen()).thenReturn(editor);
        when(cls.getJavaFile()).thenReturn(null);
        openProjectWith(cls);
        tracker.scanOpenEditors();
        when(editor.getTextLength()).thenReturn(150);
        tracker.scanOpenEditors();
        verify(data, never()).recordEdit(any(Instant.class));
    }

    @Test
    void closedEditorIsDetached() throws Exception {
        JavaEditor editor = mock(JavaEditor.class);
        when(editor.getTextLength()).thenReturn(100);
        BClass cls = classWithOpenEditor(editor);
        openProjectWith(cls);
        tracker.scanOpenEditors();
        when(cls.getJavaEditorIfOpen()).thenReturn(null);
        tracker.scanOpenEditors();
        when(editor.getTextLength()).thenReturn(150);
        when(cls.getJavaEditorIfOpen()).thenReturn(editor);
        tracker.scanOpenEditors();

        verify(data, never()).recordEdit(any(Instant.class));
    }

    @Test
    void detachAllForgetsPreviousEditorState() throws Exception {
        JavaEditor editor = mock(JavaEditor.class);
        when(editor.getTextLength()).thenReturn(100);
        openProjectWith(classWithOpenEditor(editor));
        tracker.scanOpenEditors();

        tracker.detachAll();
        when(editor.getTextLength()).thenReturn(150);
        tracker.scanOpenEditors();

        verify(data, never()).recordEdit(any(Instant.class));
    }

    @Test
    void scanHandlesProjectThatIsNoLongerOpen() throws Exception {
        BProject project = mock(BProject.class);
        when(project.getPackages()).thenThrow(ProjectNotOpenException.class);
        when(bluej.getOpenProjects()).thenReturn(new BProject[]{project});

        assertDoesNotThrow(() -> tracker.scanOpenEditors());
        verify(data, never()).recordEdit(any(Instant.class));
    }

    @Test
    void packageClosingDoesNothing() {
        assertDoesNotThrow(() -> tracker.packageClosing(mock(PackageEvent.class)));
        verify(bluej, never()).getOpenProjects();
    }
}
