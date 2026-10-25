package com.oems;

import com.oems.model.Exam;
import com.oems.model.Question;
import com.oems.model.Result;
import com.oems.model.Student;
import com.oems.service.ExamService;
import com.oems.service.QuizEngine;
import com.oems.service.ResultService;
import com.oems.store.CsvUtil;

import java.util.List;

/**
 * Dependency-free test harness (no JUnit on this machine / no build tool
 * in the project, by design). Run: see run_tests.sh. Exits non-zero
 * if any check fails, and prints one PASS/FAIL line per check.
 */
public final class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) { passed++; System.out.println("PASS: " + name); }
        else { failed++; System.out.println("FAIL: " + name); }
    }

    private static void expectThrows(String name, Runnable r) {
        try { r.run(); check(name, false); }
        catch (IllegalArgumentException e) { check(name, true); }
    }

    public static void main(String[] args) {
        // Scoring
        Exam exam = ExamService.sampleExam();
        check("sample exam has 5 questions", exam.getQuestions().size() == 5);
        check("sample exam total is 10 marks", exam.totalMarks() == 10);
        Result allCorrect = QuizEngine.score("R1", exam, List.of(1, 2, 2, 2, 2));
        check("all correct scores 10/10", allCorrect.getScore() == 10 && allCorrect.isPassed());
        Result demo = QuizEngine.score("R1", exam, List.of(1, 2, 0, 2, 2));
        check("demo answers score 8/10 (80%, PASS)", demo.getScore() == 8 && demo.percentage() == 80.0);
        Result skipped = QuizEngine.score("R1", exam, List.of());
        check("no answers scores 0 and FAILs", skipped.getScore() == 0 && !skipped.isPassed());
        Result outOfRange = QuizEngine.score("R1", exam, List.of(9, -1, 2, 2, 2));
        check("out-of-range answers count as wrong, not crash", outOfRange.getScore() == 6);
        Result boundary = QuizEngine.score("R1", exam, List.of(1, 2, 0, 0, 0));
        check("40% boundary passes (4/10)", boundary.getScore() == 4 && boundary.isPassed());

        // Validation
        expectThrows("question with 3 options rejected",
                () -> new Question("Q", "t", List.of("a", "b", "c"), 0, 1));
        expectThrows("question with correctIndex 4 rejected",
                () -> new Question("Q", "t", List.of("a", "b", "c", "d"), 4, 1));
        expectThrows("question with 0 marks rejected",
                () -> new Question("Q", "t", List.of("a", "b", "c", "d"), 0, 0));
        expectThrows("exam with 0 duration rejected", () -> new Exam("E", "T", 0));
        expectThrows("blank student roll rejected", () -> new Student("  ", "Name"));
        ExamService svc = new ExamService();
        svc.addExam(new Exam("E1", "T", 10));
        expectThrows("duplicate exam id rejected", () -> svc.addExam(new Exam("E1", "T2", 10)));
        expectThrows("question for unknown exam rejected",
                () -> svc.addQuestion("NOPE", new Question("Q", "t", List.of("a", "b", "c", "d"), 0, 1)));
        Exam dupQ = new Exam("E2", "T", 10);
        dupQ.addQuestion(new Question("Q1", "t", List.of("a", "b", "c", "d"), 0, 1));
        expectThrows("duplicate question id in one exam rejected",
                () -> dupQ.addQuestion(new Question("Q1", "t2", List.of("a", "b", "c", "d"), 0, 1)));

        // Result service
        ResultService rs = new ResultService();
        rs.add(allCorrect); rs.add(demo);
        check("results for exam filter works", rs.forExam("JAVA101").size() == 2);
        check("average percentage is 90.0", rs.averagePercentage("JAVA101") == 90.0);
        check("average of unknown exam is 0.0", rs.averagePercentage("NOPE") == 0.0);
        check("results for student filter works", rs.forStudent("R1").size() == 2);

        // CSV round-trip with commas and quotes in text
        String joined = CsvUtil.join("a,b", "say \"hi\"", "plain");
        List<String> parsed = CsvUtil.parse(joined);
        check("CSV quoting round-trips commas/quotes",
                parsed.size() == 3 && parsed.get(0).equals("a,b") && parsed.get(1).equals("say \"hi\""));

        System.out.println("\n" + passed + " passed, " + failed + " failed.");
        if (failed > 0) System.exit(1);
    }
}
