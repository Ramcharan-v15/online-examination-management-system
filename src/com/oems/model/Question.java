package com.oems.model;

import java.util.Arrays;
import java.util.List;

/** One multiple-choice question. Options are exactly four; correctIndex is 0-based. */
public final class Question {
    private final String id;
    private final String text;
    private final List<String> options;
    private final int correctIndex;
    private final int marks;

    public Question(String id, String text, List<String> options, int correctIndex, int marks) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("question id must not be blank");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("question text must not be blank");
        if (options == null || options.size() != 4) throw new IllegalArgumentException("exactly 4 options are required");
        for (String o : options) {
            if (o == null || o.isBlank()) throw new IllegalArgumentException("options must not be blank");
        }
        if (correctIndex < 0 || correctIndex > 3) throw new IllegalArgumentException("correctIndex must be 0..3");
        if (marks <= 0) throw new IllegalArgumentException("marks must be positive");
        this.id = id.trim();
        this.text = text.trim();
        this.options = List.copyOf(options);
        this.correctIndex = correctIndex;
        this.marks = marks;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public int getMarks() { return marks; }
    public boolean isCorrect(int answerIndex) { return answerIndex == correctIndex; }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(text).append('\n');
        for (int i = 0; i < options.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(options.get(i)).append('\n');
        }
        return sb.toString();
    }

    @Override public boolean equals(Object o) {
        return o instanceof Question q && id.equals(q.id) && text.equals(q.text)
                && options.equals(q.options) && correctIndex == q.correctIndex && marks == q.marks;
    }
    @Override public int hashCode() { return id.hashCode(); }
}
