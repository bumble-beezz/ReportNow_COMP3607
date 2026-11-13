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

public class SessionData {
    private static final long IDLE_THRESHOLD_MS =180_000;

    private final String sessionId;
    private final long startMs;
    private long endMs = 0L;
    private final List<TelemetryEvent> events = new ArrayList<>();
    private final List<Instant> editTimes = new ArrayList<>();
    private List<SessionSummary> pastSessions = new ArrayList<>();

    public SessionData() {
        this.sessionId= UUID.randomUUID().toString();
        this.startMs = System.currentTimeMillis();
    }

    public synchronized void recordProject(String action, String projectName) {
        events.add(new ProjectEvent(System.currentTimeMillis(),action,projectName));
    }

    public synchronized void recordClass(String action, String className,String projectName) {
        events.add(new ClassEvent(System.currentTimeMillis(), action,className, projectName));
    }

    public synchronized void recordCompile(String projectName, boolean success,long durationMs, List<CompileError> errors) {
        events.add(new CompileEvent(System.currentTimeMillis(), projectName,success, durationMs,errors));
    }

    public synchronized void recordRun(String projectName,boolean success, long durationMs, String message) {
        events.add(new RunEvent(System.currentTimeMillis(), projectName, success, durationMs, message));
    }

    public synchronized void recordEdit(Instant when) {
        editTimes.add(when);
    }

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
        return total / count;
    }

    public synchronized double getEditsPerMinute(){
        double minutes = getSessionDurationMs() / 60000.0;
        if (minutes <= 0) {
            return 0;
        }
        return editTimes.size() / minutes;
    }

    public synchronized void endSession() {
        this.endMs = System.currentTimeMillis();
    }

    public synchronized List<TelemetryEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    public synchronized List<CompileEvent> getCompileEvents() {
        List<CompileEvent> result = new ArrayList<>();
        for (TelemetryEvent e : events) {
            if (e instanceof CompileEvent) {
                result.add((CompileEvent) e);
            }
        }return result;
    }

    public synchronized List<RunEvent> getRunEvents() {
        List<RunEvent> result = new ArrayList<>();
        for (TelemetryEvent e : events) {
            if (e instanceof RunEvent){
                result.add((RunEvent) e);
            }
        }return result;
    }

    public synchronized List<ProjectEvent> getProjectEvents() {
        List<ProjectEvent> result = new ArrayList<>();
        for (TelemetryEvent e : events) {
            if (e instanceof ProjectEvent) {
                result.add((ProjectEvent) e);
            }
        } return result;
    }

    public synchronized List<ClassEvent> getClassEvents() {
        List<ClassEvent> result = new ArrayList<>();
        for (TelemetryEvent e : events) {
            if (e instanceof ClassEvent) {
                result.add((ClassEvent) e);
            }
        } return result;
    }

    public synchronized int getSuccessfulCompileCount() {
        int count = 0;
        for (CompileEvent ce : getCompileEvents()) {
            if (ce.isSuccess()) {
                count++;
            }
        } return count;
    }

    public synchronized int getFailedCompileCount() {
        return getCompileEvents().size() - getSuccessfulCompileCount();
    }

    public synchronized int getSuccessfulRunCount() {
        int count = 0;
        for (RunEvent re : getRunEvents()) {
            if (re.isSuccess()) {
                count++;
            }
        } return count;
    }

    public synchronized int getFailedRunCount() {
        return getRunEvents().size() - getSuccessfulRunCount();
    }

    
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

       


    public synchronized long getSessionDurationMs() {
        if (endMs > 0) {
            return endMs - startMs;
        }
        return System.currentTimeMillis() - startMs;
    }

    public synchronized long getIdleTimeMs() {
        long duration = getSessionDurationMs();
        long active = getActiveTimeMs();
        long idle = duration - active;
        if (idle < 0) {
            return 0L;
        } return idle;
    }

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

    public synchronized void setPastSessions(List<SessionSummary> history) {
        this.pastSessions = history;
    }

    public synchronized List<SessionSummary> getPastSessions() {
        return pastSessions;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getStartMs() {
        return startMs;
    }
}