package org.acme;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record EnrollmentId(
    @Column(name = "student_id", nullable = false) String studentId,
    @Column(name = "course_code", nullable = false) String courseCode
) implements Serializable {}