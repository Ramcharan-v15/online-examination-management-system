package com.oems.ui;

import com.oems.model.Exam;
import com.oems.model.Question;
import com.oems.model.Result;
import com.oems.model.Student;
import com.oems.service.ExamService;
import com.oems.service.QuizEngine;
import com.oems.service.ResultService;
import com.oems.store.FileStore;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console front end for the Online Examination Management System.
 *
 * Usage:
 *   java com.oems.ui.Main            interactive menu
 *   java com.oems.ui.Main --demo     load the sample exam, run one scripted
 *                                    attempt, print the result, save CSVs
 *   java com.oems.ui.Main --help     print usage
 *
 * Roles: Admin creates exams/questions and views results; Student registers
 * and takes an exam. Data persists as CSV files under data/ (or the
 * directory named by the OEMS_DATA environment variable).
 * No passwords are stored: a student is identified by roll number only,
 * which is why this build is suitable for a classroom demo, not production
 * use with real credentials (see README, Known limitations).
 */
public final class Main {
    private final ExamService exams = new ExamService();
    private final ResultService results = new ResultService();
    private final List<Student> students = new ArrayList<>();
    private final FileStore store;

    public Main(Path dataDir) { this.store = new FileStore(dataDir); }

    public static void main(String[] args) {
        String envDir = System.getenv("OEMS_DATA");
        Path dataDir = Path.of(envDir == null || envDir.isBlank() ? "data" : envDir);
        Main app = new Main(dataDir);
        try { app.store.load(app.exams, app.students, app.results); }
        catch (IOException e) { System.out.println("Warning: could not load saved data: " + e.getMessage()); }

        if (args.length > 0 && args[0].equals("--help")) {
            System.out.println("Online Examination Management System");
            System.out.println("  java com.oems.ui.Main          interactive menu");
            System.out.println("  java com.oems.ui.Main --demo   scripted sample run (no input needed)");
            return;
        }
        if (args.length > 0 && args[0].equals("--demo")) {
            app.runDemo();
            return;
        }
        app.runInteractive();
    }

    /** Scripted run used for verification and screenshots: deterministic answers. */
    void runDemo() {
        System.out.println("=== Online Examination Management System (demo) ===");
        if (exams.find("JAVA101").isEmpty()) exams.addExam(ExamService.sampleExam());
        Exam exam = exams.find("JAVA101").orElseThrow();
        Student student = new Student("25881A05DQ", "Vannala Ramcharan");
        if (!students.contains(student)) students.add(student);
        System.out.println("Student: " + student);
        System.out.println("Exam: " + exam.getId() + " - " + exam.getTitle()
                + " (" + exam.getDurationMinutes() + " minutes, " + exam.totalMarks() + " marks)");
        // Answers are 0-based: Q1 correct, Q2 correct, Q3 wrong, Q4 correct, Q5 correct = 8/10.
        List<Integer> answers = List.of(1, 2, 0, 2, 2);
        for (int i = 0; i < exam.getQuestions().size(); i++) {
            Question q = exam.getQuestions().get(i);
            System.out.println((i + 1) + ". " + q.getText());
            System.out.println("   Answer chosen: " + (answers.get(i) + 1) + ". " + q.getOptions().get(answers.get(i)));
        }
        Result result = QuizEngine.score(student.getRollNo(), exam, answers);
        results.add(result);
        System.out.println("Result: " + result);
        try {
            store.save(exams, students, results);
            System.out.println("Saved to data/ (exams.csv, questions.csv, students.csv, results.csv)");
        } catch (IOException e) {
            System.out.println("Warning: could not save data: " + e.getMessage());
        }
    }

    private void runInteractive() {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== Online Examination Management System ===");
                System.out.println("1. Admin menu");
                System.out.println("2. Student menu");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");
                switch (readInt(sc, 1, 3)) {
                    case 1 -> adminMenu(sc);
                    case 2 -> studentMenu(sc);
                    case 3 -> {
                        saveQuietly();
                        System.out.println("Goodbye!");
                        return;
                    }
                }
            }
        }
    }

    private void adminMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Create exam");
            System.out.println("2. Add question to exam");
            System.out.println("3. List exams");
            System.out.println("4. View all results");
            System.out.println("5. Back");
            System.out.print("Choose an option: ");
            switch (readInt(sc, 1, 5)) {
                case 1 -> {
                    System.out.print("Exam ID: "); String id = sc.nextLine().trim();
                    System.out.print("Exam title: "); String title = sc.nextLine().trim();
                    System.out.print("Duration (minutes): "); int mins = readInt(sc, 1, 1000);
                    try { exams.addExam(new Exam(id, title, mins)); System.out.println("Exam created."); }
                    catch (IllegalArgumentException e) { System.out.println("Error: " + e.getMessage()); }
                }
                case 2 -> {
                    System.out.print("Exam ID: "); String id = sc.nextLine().trim();
                    System.out.print("Question ID: "); String qid = sc.nextLine().trim();
                    System.out.print("Question text: "); String text = sc.nextLine().trim();
                    List<String> opts = new ArrayList<>();
                    for (int i = 1; i <= 4; i++) { System.out.print("Option " + i + ": "); opts.add(sc.nextLine().trim()); }
                    System.out.print("Correct option (1-4): "); int correct = readInt(sc, 1, 4) - 1;
                    System.out.print("Marks: "); int marks = readInt(sc, 1, 100);
                    try { exams.addQuestion(id, new Question(qid, text, opts, correct, marks)); System.out.println("Question added."); }
                    catch (IllegalArgumentException e) { System.out.println("Error: " + e.getMessage()); }
                }
                case 3 -> {
                    if (exams.list().isEmpty()) System.out.println("No exams yet.");
                    for (Exam e : exams.list()) {
                        System.out.println(e.getId() + " - " + e.getTitle() + " (" + e.getDurationMinutes()
                                + " min, " + e.getQuestions().size() + " questions, " + e.totalMarks() + " marks)");
                    }
                }
                case 4 -> {
                    if (results.all().isEmpty()) System.out.println("No results yet.");
                    for (Result r : results.all()) System.out.println(r);
                }
                case 5 -> { return; }
            }
        }
    }

    private void studentMenu(Scanner sc) {
        System.out.print("Roll number: "); String roll = sc.nextLine().trim();
        System.out.print("Name: "); String name = sc.nextLine().trim();
        Student student;
        try { student = new Student(roll, name); }
        catch (IllegalArgumentException e) { System.out.println("Error: " + e.getMessage()); return; }
        if (!students.contains(student)) students.add(student);
        while (true) {
            System.out.println("\n--- Student Menu (" + student + ") ---");
            System.out.println("1. Take an exam");
            System.out.println("2. View my results");
            System.out.println("3. Back");
            System.out.print("Choose an option: ");
            switch (readInt(sc, 1, 3)) {
                case 1 -> takeExam(sc, student);
                case 2 -> {
                    List<Result> mine = results.forStudent(student.getRollNo());
                    if (mine.isEmpty()) System.out.println("No results yet.");
                    for (Result r : mine) System.out.println(r);
                }
                case 3 -> { return; }
            }
        }
    }

    private void takeExam(Scanner sc, Student student) {
        System.out.print("Exam ID: "); String id = sc.nextLine().trim();
        Exam exam = exams.find(id).orElse(null);
        if (exam == null) { System.out.println("Error: no exam with id: " + id); return; }
        if (exam.getQuestions().isEmpty()) { System.out.println("Error: this exam has no questions yet."); return; }
        Instant start = Instant.now();
        List<Integer> answers = new ArrayList<>();
        for (int i = 0; i < exam.getQuestions().size(); i++) {
            if (Duration.between(start, Instant.now()).toMinutes() >= exam.getDurationMinutes()) {
                System.out.println("Time is up! Submitting what you have answered so far.");
                break;
            }
            Question q = exam.getQuestions().get(i);
            System.out.println("\nQ" + (i + 1) + ". " + q.getText());
            for (int j = 0; j < q.getOptions().size(); j++) {
                System.out.println("  " + (j + 1) + ". " + q.getOptions().get(j));
            }
            System.out.print("Your answer (1-4, 0 to skip): ");
            int ans = readInt(sc, 0, 4);
            answers.add(ans == 0 ? -1 : ans - 1);
        }
        Result result = QuizEngine.score(student.getRollNo(), exam, answers);
        results.add(result);
        System.out.println("\nResult: " + result);
    }

    /** Reads an integer in range, reprompting on non-numeric or out-of-range input. */
    private static int readInt(Scanner sc, int min, int max) {
        while (true) {
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) return value;
                System.out.print("Please enter a number between " + min + " and " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("That was not a number. Try again: ");
            }
        }
    }

    private void saveQuietly() {
        try { store.save(exams, students, results); }
        catch (IOException e) { System.out.println("Warning: could not save data: " + e.getMessage()); }
    }
}
