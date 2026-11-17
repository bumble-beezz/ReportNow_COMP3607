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
import com.group22.reportnow.listener.ExecutionListener;
import com.group22.reportnow.listener.ProjectLifecycleListener;
import com.group22.reportnow.listener.RecordCompile;
import com.group22.reportnow.listener.TrackEditor;
import com.group22.reportnow.storage.ConsoleOutputCapture;
import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.BPackage;
import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;
import bluej.extensions2.MenuGenerator;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;

/**
 * Main class for the ReportNow BlueJ extension.
 * Sets up the listeners, collects session data and allows students to export their activity reports as JSON and PDF files.
 */
public class ReportNowExtension extends Extension {
    /**The BlueJ environment running the extension*/
    private BlueJ bluej;
    /**The in-memory cache storing all telemetry events for the current session*/
    private SessionData data;
    /**Listens for project and class lifecycle events. */
    private ProjectLifecycleListener projectListener;
    /**Listens for compilation events*/
    private RecordCompile compileRecorder;
    /**Listens for execution events */
    private ExecutionListener execute;
    /**Tracks editor interactions */
    private TrackEditor editorTracker;
    /**Background timer used to scan open editors for changes */
    private Timer editorTimer;
    /**The unique ID of the student using the IDE */
    private String studentId;
    /**Tracks whether the extension is shutting down to prevent extra exports */
    private boolean shuttingDown = false;
    /**Captures output written to System.out and System.err */
    private ConsoleOutputCapture consoleCapture;

    /**
     * Starts the ReportNow extension when BlueJ loads it.
     * Creates the session data, registers the listeners and adds the report export option to the Tools menu.
     * @param bluej the BlueJ environment running the extension
    */
    @Override
    public void startup(BlueJ bluej) {
        this.bluej = bluej;
        this.data = new SessionData();
        this.studentId = new StoreStudentId().getStudentId();
        this.consoleCapture = new ConsoleOutputCapture();
        this.consoleCapture.start();

        projectListener = new ProjectLifecycleListener(data, this::onProjectClosed);
        compileRecorder = new RecordCompile(data);
        execute = new ExecutionListener(data);
        editorTracker = new TrackEditor(bluej, data);

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
                    if (!shuttingDown) editorTracker.scanOpenEditors();
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

    /**
     * Stops the ReportNow extension when BlueJ closes it.
     * Stops the timers, removes the listeners and ends the session.
     * Also saves the final reports automatically.
    */
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
        if (consoleCapture != null) {
            consoleCapture.stop();
        }
    }

    /**
     * Saves a report when a tracked project is closed.
     * Does not export again if the extension is shutting down.
     * @param projectName the name of the project being closed
    */
    private void onProjectClosed(String projectName) {
        if (shuttingDown) {
            return;
        }
        File dir = new File(System.getProperty("user.home"), "reportnow");
        export(dir, projectName, false);
    }

    /** Opens a folder selection window so the student can choose where to save the JSON and PDF reports.*/
    private void manualExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose folder to save ReportNow files");
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            export(chooser.getSelectedFile(), data.getCurrentProjectName(), true);
        }
    }

    /**
     * Saves the session information as JSON and PDF reports.
     * Creates the project folder, loads past sessions and attempts to save captured console output.
     * @param baseDir the folder where the reports will be saved
     * @param projectName the name of the project being reported
     * @param showPopup whether to display a message after exporting
    */
    private void export(File baseDir, String projectName, boolean showPopup) {
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            System.err.println("Could not create directory: " + baseDir);
            return;
        }

        LoadPastSessions loader = new LoadPastSessions();
        data.setPastSessions(loader.load(baseDir, data.getSessionId()));

        File projectDir = new File(baseDir, projectName);
        if (!projectDir.exists()) {
            projectDir.mkdirs();
        }

        if (consoleCapture != null) {
            try {
                consoleCapture.writeToFile(new File(projectDir, "console-output.txt"));
            } catch (Exception e) {
                System.err.println("[ReportNow] Could not write console output: " + e.getMessage());
            }
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
                JOptionPane.showMessageDialog(null, "Saved:\n" + jsonFile.getAbsolutePath() + "\n" + pdfFile.getAbsolutePath(), "ReportNow Export", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (ExportException e) {
            System.err.println("[ReportNow] Export failed: " + e.getMessage());
            if (showPopup) {
                JOptionPane.showMessageDialog(null, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**    (non-Javadoc)
     * Returns the display name of the extension
     * @return the name "ReportNow Telemetry"
     */
    @Override public String getName() { return "ReportNow Telemetry"; }

    /**
     * Returns the current version of the extension.
     * @return the version string "1.0.0"
     */
    @Override public String getVersion() { return "1.0.0"; }

    /**Checks if the extension is compatible with the current BlueJ version
     * @return true, as it is designed for the current Extensions API.
     */
    @Override public boolean isCompatible() { return true; }

    /**
     * Returns a brief description of the extension's purpose
     * @return a string describing the plugin
     */
    @Override public String getDescription() { return "Collects coding telemetry and exports JSON and PDF reports."; }
}