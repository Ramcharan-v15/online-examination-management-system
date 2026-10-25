package com.oems.service;

import com.oems.model.Exam;
import com.oems.model.Question;
import com.oems.model.Result;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scores an attempt. Scoring rule, stated plainly so tests and report agree:
 * correct answer = full question marks, wrong / skipped / out-of-range = 0.
 * No negative marking. Answers beyond the question count are ignored;
 * missing answers count as skipped (0).
 */
public final class QuizEngine {
    private QuizEngine() {}

    public static Result score(String rollNo, Exam exam, List<Integer> answers) {
        if (exam == null) throw new IllegalArgumentException("exam must not be null");
        if (answers == null) throw new IllegalArgumentException("answers must not be null");
        int score = 0;
        List<Question> questions = exam.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            int answer = i < answers.size() ? answers.get(i) : -1;
            if (questions.get(i).isCorrect(answer)) score += questions.get(i).getMarks();
        }
        return new Result(rollNo, exam.getId(), score, exam.totalMarks(), LocalDateTime.now());
    }
}
