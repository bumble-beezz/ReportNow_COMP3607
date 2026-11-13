//https://javadoc.io/doc/com.github.librepdf/openpdf/latest/com.github.librepdf.openpdf/module-summary.html
package com.group22.reportnow.export;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.group22.reportnow.model.ClassEvent;
import com.group22.reportnow.model.CompileError;
import com.group22.reportnow.model.CompileEvent;
import com.group22.reportnow.model.RunEvent;
import com.group22.reportnow.model.SessionSummary;
import com.group22.reportnow.storage.SessionData;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PdfExporter implements ExportStrategy {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final String studentId;
    private final String projectName;

    public PdfExporter(String studentId, String projectName) {
        this.studentId = studentId;
        this.projectName = projectName;
    }

    @Override
    public void export(SessionData data, File file) throws ExportException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            Document doc =new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            addTitle(doc, data);
            addSummary(doc, data);
            addTimeChart(doc, data);
            addErrorFrequency(doc, data);
            addCompileTable(doc, data);
            addClassProgression(doc, data);
            addRunTable(doc,data);
            addConsoleSummary(doc, file);
            addPastSessions(doc, data);

            doc.close();
        } catch (Exception e) {
            throw new ExportException("PDF export failed", e);
        }
    }

    private void addTitle(Document doc, SessionData data) throws Exception {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(60, 90, 150));
        Paragraph title = new Paragraph("ReportNow Session Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10f);
        doc.add(title);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        String sub = "Student: " + studentId + "   |   Project: " + projectName + "   |   Started: " + DATETIME_FORMATTER.format(Instant.ofEpochMilli(data.getStartMs()));
        Paragraph subPara = new Paragraph(sub, subFont);
        subPara.setAlignment(Element.ALIGN_CENTER);
        subPara.setSpacingAfter(20f);
        doc.add(subPara);
    }

    private void addSummary(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Session Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        int totalCompiles = 0;
        int successCompiles = 0;
        for (CompileEvent ce : data.getCompileEvents()) {
            if (!ce.getProjectName().equals(projectName)) {
                continue;
            }
            totalCompiles++;
            if (ce.isSuccess()) {
                successCompiles++;
            }
        }
        int totalRuns = 0;
        int successRuns = 0;
        for (RunEvent re : data.getRunEvents()) {
            if (!re.getProjectName().equals(projectName)) {
                continue;
            }
            totalRuns++;
            if (re.isSuccess()) {
                successRuns++;
            }
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        addSummaryRow(table, "Duration", String.format("%.1f s", data.getSessionDurationMs() / 1000.0),"Active Time", String.format("%.1f s", data.getActiveTimeMs() / 1000.0));
        addSummaryRow(table, "Idle Time", String.format("%.1f s", data.getIdleTimeMs() / 1000.0), "Typing Cadence", data.getTypingCadence() + " ms");
        addSummaryRow(table, "Total Compiles", String.valueOf(totalCompiles),"Success / Fail", successCompiles + " / " + (totalCompiles - successCompiles));
        addSummaryRow(table, "Total Runs", String.valueOf(totalRuns), "Success / Fail", successRuns + " / " + (totalRuns - successRuns));

        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
    }

    private void addSummaryRow(PdfPTable table, String key1, String value1, String key2, String value2) {
        table.addCell(summaryCell(key1, true));
        table.addCell(summaryCell(value1, false));
        table.addCell(summaryCell(key2, true));
        table.addCell(summaryCell(value2, false));
    }
    
    private PdfPCell summaryCell(String text, boolean bold) {
        Font font;
        if (bold) {
            font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
        } else {
            font = FontFactory.getFont(FontFactory.HELVETICA, 10);
        }
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOTTOM);
        cell.setPaddingBottom(8f);
        cell.setPaddingTop(4f);
        return cell;
    }

    private void addTimeChart(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Active vs Idle Time", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        long active = data.getActiveTimeMs();
        long idle = data.getIdleTimeMs();
        long total = active + idle;

        if (total == 0) {
            doc.add(new Paragraph("No activity recorded yet."));
            return;
        }
        double activePercent = active * 100.0 / total;
        double idlePercent = idle * 100.0 / total;

        PdfPTable chart = new PdfPTable(2);
        chart.setWidthPercentage(100);
        chart.setWidths(new float[]{Math.max(0.01f, (float) active), Math.max(0.01f, (float) idle)});

        PdfPCell activeCell = new PdfPCell(new Phrase(String.format("Active\n %.1f%%", activePercent), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        activeCell.setBackgroundColor(new Color(60, 90, 150));
        activeCell.setFixedHeight(30);
        activeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        activeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        chart.addCell(activeCell);

        PdfPCell idleCell = new PdfPCell(new Phrase(String.format("Idle\n %.1f%%", idlePercent), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK)));
        idleCell.setBackgroundColor(new Color(200, 216, 239));
        idleCell.setFixedHeight(30);
        idleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        idleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        chart.addCell(idleCell);

        doc.add(chart);
        Paragraph percentages = new Paragraph("Active - (Dark Blue)  |   Idle - (Light Blue)", FontFactory.getFont(FontFactory.HELVETICA, 10));
        percentages.setSpacingBefore(8);
        percentages.setSpacingAfter(20);
        doc.add(percentages);
    }

    private void addErrorFrequency(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Compilation Error Frequencies", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (CompileEvent ce : data.getCompileEvents()) {
            if (!ce.getProjectName().equals(projectName)) continue;
            for (CompileError error : ce.getErrors()) {
                String key = error.getFileName() + " — " + error.getMessage();
                counts.put(key, counts.getOrDefault(key, 0) + 1);
            }
        }

        PdfPTable table = new PdfPTable(new float[]{8f, 2f});
        table.setWidthPercentage(100);
        addHeader(table, "Error Message", "Frequency");

        if (counts.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("No errors recorded this session.", FontFactory.getFont(FontFactory.HELVETICA, 9)));
            cell.setBackgroundColor(Color.WHITE);
            cell.setPadding(6f);
            cell.setColspan(2);
            table.addCell(cell);
        } else {
            boolean alternate = false;
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                Color bg;
                if (alternate) {
                    bg = new Color(245, 245, 250);
                } else {
                    bg = Color.WHITE;
                }
                addCell(table, entry.getKey(), bg);
                addCell(table, String.valueOf(entry.getValue()), bg);
                alternate = !alternate;
            }
        }
        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
    }

    private void addCompileTable(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Compilation Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        List<CompileEvent> events = new ArrayList<>();
        for (CompileEvent ce : data.getCompileEvents()) {
            if (ce.getProjectName().equals(projectName)) {
                events.add(ce);
            }
        }
        if (events.isEmpty()) {
            doc.add(new Paragraph("No compilation events for this project.", FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY)));
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{3f, 2f, 2f, 2f, 5f});
        table.setWidthPercentage(100);
        addHeader(table, "Timestamp", "Result", "Duration", "Errors", "First Error");

        boolean alternate = false;
        for (CompileEvent ce : events) {
            Color bg;
            if (alternate) {
                bg = new Color(245, 245, 250);
            } else {
                bg = Color.WHITE;
            }
            addCell(table, formatTime(ce.getTimestampMs()), bg);
            
            String resultText;
            if (ce.isSuccess()) {
                resultText = "SUCCESS";
            } else {
                resultText = "FAILED";
            }
            addCell(table, resultText, bg);
            
            addCell(table, ce.getDurationMs() + " ms", bg);
            addCell(table, String.valueOf(ce.getErrorCount()), bg);

            String detail = "-";
            if (!ce.getErrors().isEmpty()) {
                CompileError first = ce.getErrors().get(0);
                detail = first.getFileName() + ":" + first.getLineNumber();
            }
            addCell(table, detail, bg);
            alternate = !alternate;
        }
        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
    }

    private void addClassProgression(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Class & Method Progression", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(60, 90, 150))));
        doc.add(new Paragraph("Note: File modifications represent method definitions or code edits.", FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
        doc.add(Chunk.NEWLINE);

        List<ClassEvent> events = new ArrayList<>();
        for (ClassEvent ce : data.getClassEvents()) {
            if (ce.getProjectName().equals(projectName)) {
                events.add(ce);
            }
        }

        if (events.isEmpty()) {
            doc.add(new Paragraph("No class activity for this project.", FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY)));
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{3f, 3f, 4f});
        table.setWidthPercentage(100);
        addHeader(table, "Timestamp", "Action", "Class Name");

        boolean alternate = false;
        for (ClassEvent ce : events) {
            Color bg;
            if (alternate) {
                bg = new Color(245, 245, 250);
            } else {
                bg = Color.WHITE;
            }
            addCell(table, formatTime(ce.getTimestampMs()), bg);

            String action = ce.getAction();
            if (action.equals("ClassModified")) {
                action = "Method/Class Edit";
            } else if (action.equals("ClassCreated")) {
                action = "Class Created";
            } else if ("MethodAdded".equals(action)) {
                action = "Method Added";
            } else if ("MethodRemoved".equals(action)) {
                action = "Method Removed";
            }

            addCell(table, action, bg);
            addCell(table, ce.getClassName(), bg);
            alternate = !alternate;
        }
        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
    }

    private void addRunTable(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Execution Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        List<RunEvent> events = new ArrayList<>();
        for (RunEvent re : data.getRunEvents()) {
            if (re.getProjectName().equals(projectName)) {
                events.add(re);
            }
        }

        if (events.isEmpty()) {
            doc.add(new Paragraph("No execution events for this project.", FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY)));
            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);
            return;
        }

        PdfPTable table = new PdfPTable(new float[]{3f, 2f, 2f, 5f});
        table.setWidthPercentage(100);
        addHeader(table, "Timestamp", "Result", "Duration", "Message");

        boolean alternate = false;
        for (RunEvent re : events) {
            Color bg;
            if (alternate) {
                bg = new Color(245, 245, 250);
            } else {
                bg = Color.WHITE;
            }
            addCell(table, formatTime(re.getTimestampMs()), bg);
            
            String resultText;
            if (re.isSuccess()) {
                resultText = "OK";
            } else {
                resultText = "FAILED";
            }
            addCell(table, resultText, bg);

            String durationText;
            if (re.getDurationMs() == 0) {
                durationText = "N/A";
            } else {
                durationText = re.getDurationMs() + " ms";
            }
            addCell(table, durationText, bg);

            String messageText;
            if (re.getMessage() == null) {
                messageText = "-";
            } else {
                messageText = re.getMessage();
            }
            addCell(table, messageText, bg);
            
            alternate = !alternate;
        }
        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
    }

        private void addConsoleSummary(Document doc, File file) throws Exception {
        doc.add(new Paragraph("Console Output Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        File consoleFile = new File(file.getAbsoluteFile().getParentFile(), "console-output.txt");
        String summary = new ConsoleSummary().summarize(consoleFile);

        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100);

        Font consoleFont = FontFactory.getFont(FontFactory.COURIER, 9, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(summary, consoleFont));
        
        cell.setBackgroundColor(new Color(245, 245, 245));
        cell.setBorderColor(new Color(200, 200, 200));
        cell.setPadding(10f);
        
        box.addCell(cell);
        doc.add(box);
        
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
    }

    private void addPastSessions(Document doc, SessionData data) throws Exception {
        List<SessionSummary> history = new ArrayList<>();
        for (SessionSummary s : data.getPastSessions()) {
            if (s.getProjectName().equals(projectName)) {
                history.add(s);
            }
        }
        if (history.isEmpty()) return;

        doc.newPage();
        doc.add(new Paragraph("Past Sessions for " + projectName, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(new float[]{3f, 3f, 2f, 2f, 2f});
        table.setWidthPercentage(100);
        addHeader(table, "Project", "Date/Time", "Duration", "Compiles (Success/Total)", "Active");

        boolean alternate = false;
        for (SessionSummary s : history) {
            Color bg;
            if (alternate) {
                bg = new Color(245, 245, 250);
            } else {
                bg = Color.WHITE;
            }
            addCell(table, s.getProjectName(), bg);
            addCell(table, formatDateTime(s.getSessionStartMs()), bg);
            addCell(table, String.format("%.1f s", s.getSessionDurationMs() / 1000.0), bg);
            addCell(table, s.getSuccessfulCompilations() + " / " + s.getTotalCompilations(), bg);
            addCell(table, String.format("%.1f s", s.getActiveTimeMs() / 1000.0), bg);
            alternate = !alternate;
        }
        doc.add(table);
    }

    private void addHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
            cell.setBackgroundColor(new Color(60, 90, 150));
            cell.setPadding(8f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        table.setHeaderRows(1);
    }

    private void addCell(PdfPTable table, String text, Color bg) {
        String actual;
        if (text == null) {
            actual = "";
        } else {
            actual = text;
        }
        PdfPCell cell = new PdfPCell(new Phrase(actual, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.setBackgroundColor(bg);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private String formatTime(long timestampMs) {
        return TIME_FORMATTER.format(Instant.ofEpochMilli(timestampMs));
    }

    private String formatDateTime(long timestampMs) {
        return DATETIME_FORMATTER.format(Instant.ofEpochMilli(timestampMs));
    }
}