# Online Examination Management System

A console-based Java application for creating multiple-choice exams,
letting students take them, and recording results. Built with plain Java
only — no frameworks, no external libraries, no database server.

> Status note (honest baseline): the ZIP supplied for this task
> (`Course_End_Report_Latex_Template.zip`) contained **only** a LaTeX
> course-end report template — no Java source code, no existing exam
> system. This implementation was therefore built from scratch to match
> the project title in the supplied list (item 6, "Online Examination
> Management System"). Nothing in it is presented as pre-existing code
> that was merely audited.

## Features (actually implemented)

- **Admin:** create an exam (id, title, duration in minutes), add
  4-option multiple-choice questions with marks, list exams, view all results.
- **Student:** register by roll number and name, take an exam by id,
  skip questions (enter 0), view only their own results.
- **Scoring:** correct answer = full question marks; wrong, skipped or
  out-of-range answer = 0. No negative marking. Pass mark is 40%.
- **Time limit:** if the exam duration elapses during an attempt, the
  attempt is auto-submitted with the answers given so far.
- **Persistence:** exams, questions, students and results are saved as
  CSV files under `data/` (`exams.csv`, `questions.csv`,
  `students.csv`, `results.csv`) and reloaded on the next run.
  CSV quoting handles commas and quotes inside question text.
- **Input validation:** blank ids/names/text, non-4-option questions,
  out-of-range correct options, non-positive marks/duration, duplicate
  exam ids and duplicate question ids inside one exam are all rejected
  with a plain error message instead of crashing. Non-numeric menu
  input is reprompted.

## Technology stack

| Item | Choice |
|---|---|
| Language | Java (plain, no frameworks) |
| UI | Console (standard input/output) |
| Storage | CSV files under `data/` |
| Build | `javac` / `java` directly (see below) |
| Tests | Dependency-free test harness in `tests/` (no JUnit) |
| Java version | JDK 11 or later; verified with OpenJDK 21.0.12.1 |

## Project structure

```
src/com/oems/
  model/    Question.java  Exam.java  Student.java  Result.java
  service/  ExamService.java  QuizEngine.java  ResultService.java
  store/    CsvUtil.java  FileStore.java
  ui/       Main.java            # menu, demo mode, time-limit handling
tests/com/oems/TestRunner.java   # 20 checks, exits non-zero on failure
docs/report/                     # editable LaTeX (Overleaf-ready) + compiled PDF
screenshots/                     # genuine terminal output from real runs
run.sh  run_tests.sh
```

## Prerequisites

A JDK with `javac` and `java` on your PATH (JDK 11 or later is enough;
no Maven/Gradle, no extra downloads).

## Build and run

```sh
./run.sh            # interactive menu
./run.sh --demo     # scripted sample run, no input needed
./run.sh --help
```

Or manually, from the project root:

```sh
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out com.oems.ui.Main
java -cp out com.oems.ui.Main --demo
```

By default data is stored in `./data/`. Set `OEMS_DATA` to use another
directory, e.g. `OEMS_DATA=/tmp/oems-data ./run.sh --demo`.

## Example usage (demo)

`./run.sh --demo` loads the sample exam `JAVA101 – Java Fundamentals`
(5 questions, 10 marks, 30 minutes), runs one scripted attempt for
roll number `25881A05DQ`, and prints:

```
Student: 25881A05DQ - Vannala Ramcharan
Exam: JAVA101 - Java Fundamentals (30 minutes, 10 marks)
Result: 25881A05DQ | Exam JAVA101 | 8/10 (80.0%) | PASS | ...
Saved to data/ (exams.csv, questions.csv, students.csv, results.csv)
```

The demo answers are fixed in `Main.runDemo()`: questions 1, 2, 4 and 5
correct, question 3 wrong — hence 8/10. Interactive use: choose
*Admin menu* to create your own exam, then *Student menu* to take it.

## Testing

```sh
./run_tests.sh
```

This runs the 20 checks in `tests/com/oems/TestRunner.java` covering
scoring (including the 40% pass boundary and out-of-range answers),
validation rejections, result filtering/averages, and CSV quoting
round-trips. Test status is recorded in the report (§7) from an actual
run, not assumed.

## Report

The academic report is in `docs/report/`:

- `main.tex` — editable LaTeX source, ready to import into Overleaf
  (compile `main.tex` with pdfLaTeX; all assets use relative paths).
- `project_report.pdf` — the compiled report (15 pages maximum; the
  actual page count is verified with `pdfinfo` after compiling).

## Known limitations

- No passwords or authentication: a student is identified by roll
  number only. Do **not** use this for real high-stakes exams without
  adding authentication, and do not store real credentials in `data/`.
- Console UI only — there is no Swing/JavaFX/web front end.
- Single-user, single-machine: CSV files are not safe for concurrent
  writers, and there is no database, question shuffling, or
  per-question timer.
- The sample exam is a fixed teaching example, not a question bank.

## Future improvements

Authentication and roles, a database-backed store, question shuffling
and randomised order, per-question timing, a graphical/web UI, and
result export (PDF/Excel) — all listed as future work in the report,
not claimed as implemented.
