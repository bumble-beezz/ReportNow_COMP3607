//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Duration.html
//https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/Instant.html
package com.group22.reportnow.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.group22.reportnow.model.ClassEvent;
import com.group22.reportnow.model.CompileError;
import com.group22.reportnow.model.CompileEvent;
import com.group22.reportnow.model.ProjectEvent;
import com.group22.reportnow.model.RunEvent;
import com.group22.reportnow.model.SessionSummary;
import com.group22.reportnow.model.TelemetryEvent;

/**
 * Stores the activity data collected during a ReportNow session.
 * Keeps track of events, editing activity and session statistics.
 */
public class SessionData {
    /**The maximum gap between edits counted as active time (3 minutes) */
    private static final long IDLE_THRESHOLD_MS =180_000;

    /**Unique ID for this session*/
    private final String sessionId;
    /**The time the session started in milliseconds*/
    private final long startMs;
    /**The time the session ended in milliseconds (0 if not ended)*/
    private long endMs = 0L;
    /**List of all telemetry events recorded during the session*/
    private final List<TelemetryEvent> events = new ArrayList<>();
    /**List of timestamps when editor text changes were detected*/
    private final List<Instant> editTimes = new ArrayList<>();
    /**List of previous session summaries loaded from saved reports */
    private List<SessionSummary> pastSessions = new ArrayList<>();


    /**
     * Creates a new session with a unique ID and records the start time.
     */
    public SessionData() {
        this.sessionId= UUID.randomUUID().toString();
        this.startMs = System.currentTimeMillis();
    }

    /**
     * Records a project activity event
     * @param action the project action
     * @param projectName the name of the project
     */
    public synchronized void recordProject(String action, String projectName) {
        events.add(new ProjectEvent(System.currentTimeMillis(),action,projectName));
    }

    /**
     * Records a class activity event
     * @param action the class action
     * @param className the name of the class
     * @param projectName the name of the project
     */
    public synchronized void recordClass(String action, String className,String projectName) {
        events.add(new ClassEvent(System.currentTimeMillis(), action,className, projectName));
    }

    /**
     * Records a compilation event
     * @param projectName the name of the project
     * @param success whether compilation succeeded
     * @param durationMs the compilation duration in milliseconds
     * @param errors the list of compilation errors
     */
    public synchronized void recordCompile(String projectName, boolean success,long durationMs, List<CompileError> errors) {
        events.add(new CompileEvent(System.currentTimeMillis(), projectName,success, durationMs,errors));
    }

    /**
     * Records a program execution event.
     * @param projectName the name of the project
     * @param success whether execution succeeded
     * @param durationMs the recorded duration in milliseconds
     * @param message any runtime message or exception information
     */
    public synchronized void recordRun(String projectName,boolean success, long durationMs, String message) {
        events.add(new RunEvent(System.currentTimeMillis(), projectName, success, durationMs, message));
    }

    /**
     * Records the timestamp of a detected text edit
     * @param when the time the edit was detected
     */
    public synchronized void recordEdit(Instant when) {
        editTimes.add(when);
    }

    /**
     * Calculates the average time between detected edits.
     * Gaps of three minutes or more are ignored.
     * @return the typing cadence in milliseconds
     */
    public synchronized long getTypingCadence(){
        long total = 0;
        int count = 0;
        for(int i = 1; i < editTimes.size(); i++){
            long gap = Duration.between(editTimes.get(i - 1), editTimes.get(i)).toMillis();
            if (gap >= 0 && gap < IDLE_THRESHOLD_MS) {
                total += gap;
                count++;
            }
        }
        if (count == 0) {
            return 0;
        }
        return total/count;
    }

    /**
     * Calculates the average number of detected edits per minute.
     * @return the edits per minute
     */
    public synchronized double getEditsPerMinute(){
        double minutes = getSessionDurationMs() / 60000.0;
        if (minutes <= 0) {
            return 0;
        }
        return editTimes.size() / minutes;
    }

    /**Marks the session as ended by recording the end time*/
    public synchronized void endSession() {
        this.endMs = System.currentTimeMillis();
    }

    /**
     * Gets an unmodifiable list of all recorded telemetry events
     * @return the list of events
     */
    public synchronized List<TelemetryEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    /**
     * Gets a filtered list of compilation events
     * @return the list of compilation events
     */
    public synchronized List<CompileEvent> getCompileEvents() {
        List<CompileEvent> result = new ArrayList<>();
        for (TelemetryEvent e : events) {
            if (e instanceof CompileEvent) {
                result.add((CompileEvent) e);
            }
        }return result;
    }

    /**
     * Gets a filtered list of execution events
     * @return the list of execution events
     */
    public synchronized List<RunEvent> getRunEvents() {
        List<RunEvent> result = new ArrayList<>();
        for (TelemetryEvent e : events) {
            if (e instanceof RunEvent){
                result.add((RunEvent) e);
            }
        }return result;
    }

    /**
     * Gets a filtered list of project events
     * @return the list of project events
     */
    public synchronized List<ProjectEvent> getProjectEvents() {
        List<ProjectEvent> result = new ArrayList<>();
        for (TelemetryEvent e : events) {
            if (e instanceof ProjectEvent) {
                result.add((ProjectEvent) e);
            }
        } return result;
    }

    /**
     * Gets a filtered list of class
     * @return the list of class events
     */
    public synchronized List<ClassEvent> getClassEvents() {
        List<ClassEvent> result = new ArrayList<>();
        for (TelemetryEvent e : events) {
            if (e instanceof ClassEvent) {
                result.add((ClassEvent) e);
            }
        } return result;
    }

    /**
     * Counts the total number of successful compilations
     * @return the successful compilation count
     */
    public synchronized int getSuccessfulCompileCount() {
        int count = 0;
        for (CompileEvent ce : getCompileEvents()) {
            if (ce.isSuccess()) {
                count++;
            }
        } return count;
    }

    /**
     * Counts the total number of failed compilations.
     * @return the failed compilation count
     */
    public synchronized int getFailedCompileCount() {
        return getCompileEvents().size() - getSuccessfulCompileCount();
    }

    /**
     * Counts the total number of successful executions.
     * @return the successful execution count
     */
    public synchronized int getSuccessfulRunCount() {
        int count = 0;
        for (RunEvent re : getRunEvents()) {
            if (re.isSuccess()) {
                count++;
            }
        } return count;
    }

    /**
     * Counts the total number of failed executions
     * @return the failed execution count
     */
    public synchronized int getFailedRunCount() {
        return getRunEvents().size() - getSuccessfulRunCount();
    }

    
    /**
     * Calculates the estimated active time using detected edits.
     * If no active editing time is founc, compilation and execution durations are used instead.
     * @return the estimated active time in milliseconds
     */
    public synchronized long getActiveTimeMs() {
        long fromEdits = getActiveTimeFromEditsMs();

        if (fromEdits > 0) {
            return fromEdits;
        }

        long total = 0L;

        for (TelemetryEvent e : events) {
            if (e instanceof CompileEvent) {
                total += ((CompileEvent) e).getDurationMs();
            } else if (e instanceof RunEvent) {
                total += ((RunEvent) e).getDurationMs();
            }
        }

        return total;   
    }

    /**
     * Calculates active time using gaps between detected edits that are shorter than three minutes
     * @return the estimated active editing time in milliseconds
     */
    public synchronized long getActiveTimeFromEditsMs() {
        if (editTimes.size() < 2) {
            return 0L;
        }

        long total = 0L;

        for (int i = 1; i < editTimes.size(); i++) {
            long gap = Duration.between(
                editTimes.get(i - 1),
                editTimes.get(i)
            ).toMillis();

            if (gap >= 0 && gap < IDLE_THRESHOLD_MS) {
                total += gap;
            }
        }

        return total;
    }

    /**
     * Gets the total duration of the session
     * @return the session duration in milliseconds
     */
    public synchronized long getSessionDurationMs() {
        if (endMs > 0) {
            return endMs - startMs;
        }
        return System.currentTimeMillis() - startMs;
    }

    /**
     * Calculates the estimated idle time by subtracting active time from the total session duration
     * @return the estimated idle time in milliseconds
     */
    public synchronized long getIdleTimeMs() {
        long duration = getSessionDurationMs();
        long active = getActiveTimeMs();
        long idle = duration - active;
        if (idle < 0) {
            return 0L;
        } return idle;
    }

    /**
     * Finds the most recently opened or created project
     * @return the current project name or UnKnownProject if none was recorded
     */
    public synchronized String getCurrentProjectName() {
        String latest = "UnknownProject";
        for (TelemetryEvent e : events) {
            if (e instanceof ProjectEvent) {
                ProjectEvent pe = (ProjectEvent) e;
                if ("ProjectOpened".equals(pe.getAction())) {
                    latest = pe.getProjectName();
                } else if ("ProjectCreated".equals(pe.getAction())) {
                    latest = pe.getProjectName();
                }
            }
        }
        return latest;
    }

    /**
     * Sets the list of previous session summaries
     * @param history the list of past session summaries
     */
    public synchronized void setPastSessions(List<SessionSummary> history) {
        this.pastSessions = history;
    }

    /**
     * Gets the stored list of previous session summaries
     * @return the list of past sessions
     */
    public synchronized List<SessionSummary> getPastSessions() {
        return pastSessions;
    }

    /**
     * Gets the unique session ID
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the session start time in milliseconds
     * @return the start time
     */
    public long getStartMs() {
        return startMs;
    }
}