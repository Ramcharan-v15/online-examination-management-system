package com.oems.service;

import com.oems.model.Result;

import java.util.ArrayList;
import java.util.List;

/** In-memory result store with simple per-exam / per-student queries. */
public final class ResultService {
    private final List<Result> results = new ArrayList<>();

    public void add(Result result) {
        if (result == null) throw new IllegalArgumentException("result must not be null");
        results.add(result);
    }

    public List<Result> all() { return new ArrayList<>(results); }

    public List<Result> forExam(String examId) {
        List<Result> out = new ArrayList<>();
        for (Result r : results) if (r.getExamId().equals(examId)) out.add(r);
        return out;
    }

    public List<Result> forStudent(String rollNo) {
        List<Result> out = new ArrayList<>();
        for (Result r : results) if (r.getRollNo().equals(rollNo)) out.add(r);
        return out;
    }

    public double averagePercentage(String examId) {
        List<Result> list = forExam(examId);
        if (list.isEmpty()) return 0.0;
        double sum = 0;
        for (Result r : list) sum += r.percentage();
        return sum / list.size();
    }
}
