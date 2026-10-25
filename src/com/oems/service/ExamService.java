package com.oems.service;

import com.oems.model.Exam;
import com.oems.model.Question;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory registry of exams. Rejects duplicate exam ids. */
public final class ExamService {
    private final Map<String, Exam> exams = new LinkedHashMap<>();

    public void addExam(Exam exam) {
        if (exam == null) throw new IllegalArgumentException("exam must not be null");
        if (exams.containsKey(exam.getId())) {
            throw new IllegalArgumentException("duplicate exam id: " + exam.getId());
        }
        exams.put(exam.getId(), exam);
    }

    public Optional<Exam> find(String id) {
        return Optional.ofNullable(exams.get(id));
    }

    public List<Exam> list() { return new ArrayList<>(exams.values()); }

    public void addQuestion(String examId, Question question) {
        Exam exam = find(examId).orElseThrow(
                () -> new IllegalArgumentException("no exam with id: " + examId));
        exam.addQuestion(question);
    }

    /** The sample exam used by --demo and by the report screenshots. */
    public static Exam sampleExam() {
        Exam exam = new Exam("JAVA101", "Java Fundamentals", 30);
        exam.addQuestion(new Question("Q1", "Which keyword is used to inherit a class in Java?",
                List.of("implements", "extends", "inherits", "super"), 1, 2));
        exam.addQuestion(new Question("Q2", "Which of these is NOT a Java primitive type?",
                List.of("int", "boolean", "String", "char"), 2, 2));
        exam.addQuestion(new Question("Q3", "What is the size of an int in Java?",
                List.of("8 bits", "16 bits", "32 bits", "64 bits"), 2, 2));
        exam.addQuestion(new Question("Q4", "Which collection does NOT allow duplicate elements?",
                List.of("ArrayList", "LinkedList", "HashSet", "Vector"), 2, 2));
        exam.addQuestion(new Question("Q5", "Which method is the entry point of a Java program?",
                List.of("start()", "run()", "main()", "init()"), 2, 2));
        return exam;
    }
}
