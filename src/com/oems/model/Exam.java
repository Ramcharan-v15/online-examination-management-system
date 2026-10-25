package com.oems.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An exam: an id, a title, a time limit in minutes, and its questions. */
public final class Exam {
    private final String id;
    private final String title;
    private final int durationMinutes;
    private final List<Question> questions = new ArrayList<>();

    public Exam(String id, String title, int durationMinutes) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("exam id must not be blank");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("exam title must not be blank");
        if (durationMinutes <= 0) throw new IllegalArgumentException("duration must be positive minutes");
        this.id = id.trim();
        this.title = title.trim();
        this.durationMinutes = durationMinutes;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getDurationMinutes() { return durationMinutes; }
    public List<Question> getQuestions() { return Collections.unmodifiableList(questions); }

    public void addQuestion(Question q) {
        if (q == null) throw new IllegalArgumentException("question must not be null");
        for (Question existing : questions) {
            if (existing.getId().equals(q.getId())) {
                throw new IllegalArgumentException("duplicate question id in this exam: " + q.getId());
            }
        }
        questions.add(q);
    }

    public int totalMarks() {
        int total = 0;
        for (Question q : questions) total += q.getMarks();
        return total;
    }
}
