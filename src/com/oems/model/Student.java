package com.oems.model;

/** A student, identified by roll number (the login key for this console app). */
public final class Student {
    private final String rollNo;
    private final String name;

    public Student(String rollNo, String name) {
        if (rollNo == null || rollNo.isBlank()) throw new IllegalArgumentException("roll number must not be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        this.rollNo = rollNo.trim();
        this.name = name.trim();
    }

    public String getRollNo() { return rollNo; }
    public String getName() { return name; }

    @Override public String toString() { return rollNo + " - " + name; }
    @Override public boolean equals(Object o) {
        return o instanceof Student s && rollNo.equals(s.rollNo);
    }
    @Override public int hashCode() { return rollNo.hashCode(); }
}
