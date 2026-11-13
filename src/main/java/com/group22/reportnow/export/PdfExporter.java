//https://javadoc.io/doc/com.github.librepdf/openpdf/latest/com.github.librepdf.openpdf/module-summary.html
package com.group22.reportnow.export;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
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

    private final String studentId;
    private final String projectName;

    public PdfExporter(String studentId, String projectName) {
        this.studentId = studentId;
        this.projectName = projectName;
    }

    @Override
    public void export(SessionData data, File file) throws ExportException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            addTitle(doc, data);
            addSummary(doc, data);
            addTimeChart(doc, data);
            addCompileTable(doc, data);
            addRunTable(doc, data);
            addConsoleSummary(doc,file);
            addClassTable(doc, data);
            addErrorFrequency(doc, data);
            addPastSessions(doc, data);

            doc.close();
        } catch (Exception e) {
            throw new ExportException("PDF export failed", e);
        }
    }

    private void addTitle(Document doc, SessionData data) throws Exception {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(60, 90, 150));
        Paragraph title = new Paragraph("ReportNow Session Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(15f);
        doc.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        String sub = "Student: " + studentId + "   |   Project: " + projectName
                + "   |   Started: " + Instant.ofEpochMilli(data.getStartMs());
        Paragraph subPara = new Paragraph(sub, subFont);
        subPara.setAlignment(Element.ALIGN_CENTER);
        subPara.setSpacingAfter(15f);
        doc.add(subPara);
    }

    private void addSummary(Document doc, SessionData data) throws Exception {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        addSummaryRow(table, "Session duration", String.format("%.1f s", data.getSessionDurationMs() / 1000.0),
                "Active time", String.format("%.1f s", data.getActiveTimeMs() / 1000.0));
        addSummaryRow(table, "Idle time", String.format("%.1f s", data.getIdleTimeMs() / 1000.0),
                "Compiles", String.valueOf(data.getCompileEvents().size()));
        addSummaryRow(table, "Successful compiles", String.valueOf(data.getSuccessfulCompileCount()),
                "Failed compiles", String.valueOf(data.getFailedCompileCount()));
        addSummaryRow(table, "Runs", String.valueOf(data.getRunEvents().size()),
                "Successful runs", String.valueOf(data.getSuccessfulRunCount()));
        addSummaryRow(table, "Typing cadence", data.getTypingCadence() + " ms",
                "Edits per minute", String.format("%.1f", data.getEditsPerMinute()));
        doc.add(table);
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
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(6f);
        return cell;
    }

    private void addCompileTable(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Compilations", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(new float[]{3f, 2f, 2f, 2f, 5f});
        table.setWidthPercentage(100);
        addHeader(table, "Timestamp", "Result", "Duration", "Errors", "First error");

        boolean alternate = false;
        for (CompileEvent ce : data.getCompileEvents()) {
            if (!ce.getProjectName().equals(projectName)) {
                continue;
            }
            Color bg;
            if (alternate) {
                bg = new Color(245, 245, 250);
            } else {
                bg = Color.WHITE;
            }
            String result;
            if (ce.isSuccess()) {
                result = "SUCCESS";
            } else {
                result = "FAILED";
            }
            addCell(table, Instant.ofEpochMilli(ce.getTimestampMs()).toString(), bg);
            addCell(table, result, bg);
            addCell(table, ce.getDurationMs() + " ms", bg);
            addCell(table, String.valueOf(ce.getErrorCount()), bg);
            String detail = "-";
            if (!ce.getErrors().isEmpty()) {
                CompileError first = ce.getErrors().get(0);
                detail = first.getFileName() + ":" + first.getLineNumber() + " — " + first.getMessage();
            }
            addCell(table, detail, bg);
            alternate = !alternate;
        }
        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private void addRunTable(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Runs", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(new float[]{3f, 2f, 2f, 5f});
        table.setWidthPercentage(100);
        addHeader(table, "Timestamp", "Result", "Duration", "Message");

        boolean alternate = false;
        for (RunEvent re : data.getRunEvents()) {
            if (!re.getProjectName().equals(projectName)) {
                continue;
            }
            Color bg;
            if (alternate) {
                bg = new Color(245, 245, 250);
            } else {
                bg = Color.WHITE;
            }
            String result;
            if (re.isSuccess()) {
                result = "OK";
            } else {
                result = "FAILED";
            }
            addCell(table, Instant.ofEpochMilli(re.getTimestampMs()).toString(), bg);
            addCell(table, result, bg);
            if (re.getDurationMs() == 0) {
                addCell(table, "N/A", bg);
            }else{
                addCell(table, re.getDurationMs() + " ms", bg);
            }
            String detail = re.getMessage();
            if (detail == null) {
                detail = "-";
            }
            addCell(table, detail, bg);
            alternate = !alternate;
        }
        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private void addClassTable(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Class Activity", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(new float[]{3f, 3f, 4f});
        table.setWidthPercentage(100);
        addHeader(table, "Timestamp", "Action", "Class");

        boolean alternate = false;
        for (ClassEvent ce : data.getClassEvents()) {
            if (!ce.getProjectName().equals(projectName)) {
                continue;
            }
            Color bg;
            if (alternate) {
                bg = new Color(245, 245, 250);
            } else {
                bg = Color.WHITE;
            }
            addCell(table, Instant.ofEpochMilli(ce.getTimestampMs()).toString(), bg);
            addCell(table, ce.getAction(), bg);
            addCell(table, ce.getClassName(), bg);
            alternate = !alternate;
        }
        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }


    
    private void addTimeChart(Document doc, SessionData data) throws Exception {
        long active = data.getActiveTimeMs();
        long idle = data.getIdleTimeMs();
        long total = active + idle;

        Paragraph heading = new Paragraph("Active vs Idle Time", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new java.awt.Color(61, 94, 151)));
        heading.setSpacingBefore(12);
        heading.setSpacingAfter(10);
        doc.add(heading);

        if (total == 0) {
            doc.add(new Paragraph("No activity recorded yet."));
            return;
        }

        double activePercent = active * 100.0 / total;
        double idlePercent = idle * 100.0 / total;

        PdfPTable chart = new PdfPTable(2);
        chart.setWidthPercentage(100);
        chart.setWidths(new float[]{
            Math.max(0.01f, (float) active),
            Math.max(0.01f, (float) idle)
        });

        PdfPCell activeCell = new PdfPCell();
        activeCell.setBackgroundColor(new java.awt.Color(155, 120, 210));
        activeCell.setFixedHeight(15);
        activeCell.setBorder(0);
        chart.addCell(activeCell);

        PdfPCell idleCell = new PdfPCell();
        idleCell.setBackgroundColor(new java.awt.Color(150, 205, 245));
        idleCell.setFixedHeight(15);
        idleCell.setBorder(0);
        chart.addCell(idleCell);

        doc.add(chart);

        Paragraph percentages = new Paragraph(String.format("Active (Purple): %.1f%%   |   Idle (Light Blue): %.1f%%", activePercent, idlePercent), FontFactory.getFont(FontFactory.HELVETICA, 9));

        percentages.setSpacingBefore(8);
        percentages.setSpacingAfter(12);
        doc.add(percentages);
    }


    private void addConsoleSummary(Document doc, File file) throws Exception {
        doc.add(new Paragraph("Console Output Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        File consoleFile = new File(file.getAbsoluteFile().getParentFile(), "console-output.txt");

        String summary = new ConsoleSummary().summarize(consoleFile);

        doc.add(new Paragraph(summary));
        doc.add(Chunk.NEWLINE);
    }

    private void addErrorFrequency(Document doc, SessionData data) throws Exception {
        doc.add(new Paragraph("Compilation Error Frequency", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (CompileEvent ce : data.getCompileEvents()) {
            if (!ce.getProjectName().equals(projectName)) {
                continue;
            }
            for (CompileError error : ce.getErrors()) {
                String key = error.getFileName() + " — " + error.getMessage();
                if (counts.containsKey(key)) {
                    counts.put(key, counts.get(key) + 1);
                } else {
                    counts.put(key, 1);
                }
            }
        }

        PdfPTable table = new PdfPTable(new float[]{8f, 2f});
        table.setWidthPercentage(100);
        addHeader(table, "Error", "Frequency");

        boolean alternate = false;
        if (counts.isEmpty()) {
            addCell(table, "No errors recorded this session.", Color.WHITE);
            addCell(table, "0", Color.WHITE);
        } else {
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
    }

    private void addPastSessions(Document doc, SessionData data) throws Exception {
        List<SessionSummary> history = new ArrayList<>();
        for (SessionSummary s : data.getPastSessions()) {
            if (s.getProjectName().equals(projectName)) {
                history.add(s);
            }
        }
        if (history.isEmpty()) {
            return;
        }

        doc.newPage();
        doc.add(new Paragraph("Past Sessions", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(60, 90, 150))));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(new float[]{3f, 3f, 2f, 2f, 2f});
        table.setWidthPercentage(100);
        addHeader(table, "Project", "Date/Time", "Duration", "Compiles (OK/Total)", "Active");

        boolean alternate = false;
        for (SessionSummary s : history) {
            Color bg;
            if (alternate) {
                bg = new Color(245, 245, 250);
            } else {
                bg = Color.WHITE;
            }
            addCell(table, s.getProjectName(), bg);
            addCell(table, Instant.ofEpochMilli(s.getSessionStartMs()).toString(), bg);
            addCell(table, String.format("%.1f s", s.getSessionDurationMs() / 1000.0), bg);
            addCell(table, s.getSuccessfulCompilations() + " / " + s.getTotalCompilations(), bg);
            addCell(table, String.format("%.1f s", s.getActiveTimeMs() / 1000.0), bg);
            alternate = !alternate;
        }
        doc.add(table);
    }

    private void addHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
            cell.setBackgroundColor(new Color(60, 90, 150));
            cell.setPadding(6f);
            table.addCell(cell);
        }
    }

    private void addCell(PdfPTable table, String text, Color bg) {
        String actual = text;
        if (actual == null) {
            actual = "";
        }
        PdfPCell cell = new PdfPCell(new Phrase(actual, FontFactory.getFont(FontFactory.HELVETICA, 8)));
        cell.setBackgroundColor(bg);
        cell.setPadding(5f);
        table.addCell(cell);
    }
}