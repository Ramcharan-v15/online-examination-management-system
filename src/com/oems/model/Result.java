package com.oems.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Immutable outcome of one student taking one exam. Pass mark is 40%. */
public final class Result {
    public static final double PASS_PERCENTAGE = 40.0;

    private final String rollNo;
    private final String examId;
    private final int score;
    private final int maxMarks;
    private final LocalDateTime takenAt;

    public Result(String rollNo, String examId, int score, int maxMarks, LocalDateTime takenAt) {
        if (rollNo == null || rollNo.isBlank()) throw new IllegalArgumentException("roll number must not be blank");
        if (examId == null || examId.isBlank()) throw new IllegalArgumentException("exam id must not be blank");
        if (maxMarks < 0 || score < 0 || score > maxMarks) throw new IllegalArgumentException("invalid score/maxMarks");
        this.rollNo = rollNo.trim();
        this.examId = examId.trim();
        this.score = score;
        this.maxMarks = maxMarks;
        this.takenAt = takenAt == null ? LocalDateTime.now() : takenAt;
    }

    public String getRollNo() { return rollNo; }
    public String getExamId() { return examId; }
    public int getScore() { return score; }
    public int getMaxMarks() { return maxMarks; }
    public LocalDateTime getTakenAt() { return takenAt; }

    public double percentage() {
        return maxMarks == 0 ? 0.0 : (100.0 * score) / maxMarks;
    }
    public boolean isPassed() { return percentage() >= PASS_PERCENTAGE; }

    @Override public String toString() {
        return String.format("%s | Exam %s | %d/%d (%.1f%%) | %s | %s",
                rollNo, examId, score, maxMarks, percentage(),
                isPassed() ? "PASS" : "FAIL",
                takenAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}
