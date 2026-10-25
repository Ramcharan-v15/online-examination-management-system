package com.oems.store;

import com.oems.model.Exam;
import com.oems.model.Question;
import com.oems.model.Result;
import com.oems.model.Student;
import com.oems.service.ExamService;
import com.oems.service.ResultService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CSV persistence under a data directory (default {@code data/}).
 * Files: exams.csv, questions.csv, students.csv, results.csv.
 * A malformed line is skipped, never crashes a load; save failures are
 * reported to the caller instead of being silently swallowed.
 */
public final class FileStore {
    private final Path dir;

    public FileStore(Path dir) { this.dir = dir; }

    public void save(ExamService exams, List<Student> students, ResultService results) throws IOException {
        Files.createDirectories(dir);
        StringBuilder examCsv = new StringBuilder("id,title,durationMinutes\n");
        StringBuilder qCsv = new StringBuilder("examId,questionId,text,opt1,opt2,opt3,opt4,correctIndex,marks\n");
        for (Exam e : exams.list()) {
            examCsv.append(CsvUtil.join(e.getId(), e.getTitle(), String.valueOf(e.getDurationMinutes()))).append('\n');
            for (Question q : e.getQuestions()) {
                List<String> o = q.getOptions();
                qCsv.append(CsvUtil.join(e.getId(), q.getId(), q.getText(), o.get(0), o.get(1), o.get(2), o.get(3),
                        String.valueOf(q.getCorrectIndex()), String.valueOf(q.getMarks()))).append('\n');
            }
        }
        StringBuilder sCsv = new StringBuilder("rollNo,name\n");
        for (Student s : students) sCsv.append(CsvUtil.join(s.getRollNo(), s.getName())).append('\n');
        StringBuilder rCsv = new StringBuilder("rollNo,examId,score,maxMarks,takenAt\n");
        for (Result r : results.all()) {
            rCsv.append(CsvUtil.join(r.getRollNo(), r.getExamId(), String.valueOf(r.getScore()),
                    String.valueOf(r.getMaxMarks()), r.getTakenAt().toString())).append('\n');
        }
        Files.writeString(dir.resolve("exams.csv"), examCsv.toString());
        Files.writeString(dir.resolve("questions.csv"), qCsv.toString());
        Files.writeString(dir.resolve("students.csv"), sCsv.toString());
        Files.writeString(dir.resolve("results.csv"), rCsv.toString());
    }

    public void load(ExamService exams, List<Student> students, ResultService results) throws IOException {
        Path examFile = dir.resolve("exams.csv");
        if (!Files.exists(examFile)) return; // first run: nothing saved yet
        for (String line : Files.readAllLines(examFile)) {
            if (line.startsWith("id,")) continue;
            List<String> f = CsvUtil.parse(line);
            if (f.size() < 3) continue;
            try { exams.addExam(new Exam(f.get(0), f.get(1), Integer.parseInt(f.get(2)))); }
            catch (IllegalArgumentException ignored) { /* duplicate / bad line: skip */ }
        }
        Path qFile = dir.resolve("questions.csv");
        if (Files.exists(qFile)) {
            for (String line : Files.readAllLines(qFile)) {
                if (line.startsWith("examId,")) continue;
                List<String> f = CsvUtil.parse(line);
                if (f.size() < 9) continue;
                try {
                    exams.addQuestion(f.get(0), new Question(f.get(1), f.get(2),
                            List.of(f.get(3), f.get(4), f.get(5), f.get(6)),
                            Integer.parseInt(f.get(7)), Integer.parseInt(f.get(8))));
                } catch (IllegalArgumentException ignored) { /* skip bad line */ }
            }
        }
        Path sFile = dir.resolve("students.csv");
        if (Files.exists(sFile)) {
            for (String line : Files.readAllLines(sFile)) {
                if (line.startsWith("rollNo,")) continue;
                List<String> f = CsvUtil.parse(line);
                if (f.size() < 2) continue;
                try { students.add(new Student(f.get(0), f.get(1))); }
                catch (IllegalArgumentException ignored) { /* skip */ }
            }
        }
        Path rFile = dir.resolve("results.csv");
        if (Files.exists(rFile)) {
            for (String line : Files.readAllLines(rFile)) {
                if (line.startsWith("rollNo,")) continue;
                List<String> f = CsvUtil.parse(line);
                if (f.size() < 5) continue;
                try {
                    results.add(new Result(f.get(0), f.get(1), Integer.parseInt(f.get(2)),
                            Integer.parseInt(f.get(3)), LocalDateTime.parse(f.get(4))));
                } catch (IllegalArgumentException ignored) { /* skip */ }
            }
        }
    }
}
