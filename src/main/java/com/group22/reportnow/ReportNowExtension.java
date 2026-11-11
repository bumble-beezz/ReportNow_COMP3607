package com.group22.reportnow;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.group22.reportnow.config.StoreStudentId;
import com.group22.reportnow.export.ExportContext;
import com.group22.reportnow.export.ExportException;
import com.group22.reportnow.export.JsonExporter;
import com.group22.reportnow.export.LoadPastSessions;
import com.group22.reportnow.export.PdfExporter;
import com.group22.reportnow.listener.RecordCompile;
import com.group22.reportnow.listener.ExecutionListener;
import com.group22.reportnow.listener.TrackEditor;
import com.group22.reportnow.listener.ProjectLifecycleListener;
import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.BPackage;
import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import bluej.extensions2.MenuGenerator;
import javafx.scene.control.MenuItem;
import javafx.application.Platform;

public class ReportNowExtension extends Extension {
    private BlueJ bluej;
    private SessionData data;
    private ProjectLifecycleListener projectListener;
    private RecordCompile compileRecorder;
    private ExecutionListener execute;
    private TrackEditor editorTracker;
    private Timer editorTimer;
    private String studentId;
    private boolean shuttingDown = false;

    @Override
    public void startup(BlueJ bluej) {
        this.bluej = bluej;
        this.data = new SessionData();
        this.studentId = new StoreStudentId().getStudentId();

        projectListener = new ProjectLifecycleListener(data, this::onProjectClosed);
        compileRecorder = new RecordCompile(data);
        execute = new ExecutionListener(data);
        editorTracker= new TrackEditor(bluej, data);

        bluej.addPackageListener(projectListener);
        bluej.addCompileListener(compileRecorder);
        bluej.addInvocationFinishedListener(execute);
        bluej.addClassListener(projectListener.getClassListener());
        bluej.addPackageListener(editorTracker);

        projectListener.startTimer();
        editorTimer = new Timer("Editor Timer", true);
        editorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!shuttingDown) {
                        editorTracker.scanOpenEditors();
                    }
                });
            }   
        }, 1000, 1000);

        bluej.setMenuGenerator(new MenuGenerator() {
            @Override
            public MenuItem getToolsMenuItem(BPackage bp) {
                MenuItem item = new MenuItem("Export ReportNow...");
                item.setOnAction(e -> manualExport());
                return item;
            }
        });

        System.out.println("[ReportNow] Session " + data.getSessionId() + " started for " + studentId);
    }

    @Override
    public void terminate() {
        shuttingDown = true;

        if (editorTimer != null) {
            editorTimer.cancel();
        }
        if (projectListener != null) {
            projectListener.stopTimer();
        }

        if (bluej != null) {
            bluej.removePackageListener(projectListener);
            bluej.removeCompileListener(compileRecorder);
            bluej.removeInvocationFinishedListener(execute);
            bluej.removeClassListener(projectListener.getClassListener());
            bluej.removePackageListener(editorTracker);
            editorTracker.detachAll();
        }
        data.endSession();
        export(new File(System.getProperty("user.home"), "reportnow"), data.getCurrentProjectName(), false);
    }

    private void onProjectClosed(String projectName) {
        if (shuttingDown) {
            return;
        }
        File dir = new File(System.getProperty("user.home"), "reportnow");
        export(dir, projectName, false);
    }

    private void manualExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose folder to save ReportNow files");
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            String currentProject = data.getCurrentProjectName();
            export(chooser.getSelectedFile(), currentProject, true);
        }
    }

    private void export(File baseDir, String projectName, boolean showPopup) {
        if (!baseDir.exists()) {
            if (!baseDir.mkdirs()) {
                System.err.println("Could not create directory: " + baseDir);
                return;
            }
        }

        LoadPastSessions loader = new LoadPastSessions();
        data.setPastSessions(loader.load(baseDir, data.getSessionId()));

        File projectDir = new File(baseDir, projectName);
        if (!projectDir.exists()) {
            projectDir.mkdirs();
        }

        long stamp = System.currentTimeMillis();
        File jsonFile = new File(projectDir, "reportnow-" + stamp + ".json");
        File pdfFile = new File(projectDir, "reportnow-" + stamp + ".pdf");

        try {
            ExportContext ctx = new ExportContext();
            ctx.setStrategy(new JsonExporter(studentId, projectName));
            ctx.export(data, jsonFile);
            ctx.setStrategy(new PdfExporter(studentId, projectName));
            ctx.export(data, pdfFile);

            System.out.println("[ReportNow] Wrote " + jsonFile + " and " + pdfFile);

            if (showPopup) {
                JOptionPane.showMessageDialog(null,
                        "Saved:\n" + jsonFile.getAbsolutePath() + "\n" + pdfFile.getAbsolutePath(),
                        "ReportNow Export",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (ExportException e) {
            System.err.println("[ReportNow] Export failed: " + e.getMessage());
            if (showPopup) {
                JOptionPane.showMessageDialog(null, "Export failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public String getName() {
        return "ReportNow Telemetry";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean isCompatible() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Collects coding telemetry and exports JSON and PDF reports.";
    }
}