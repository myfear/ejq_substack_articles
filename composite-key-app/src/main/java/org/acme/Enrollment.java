package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollment")
public class Enrollment extends PanacheEntityBase {

    @EmbeddedId
    public EnrollmentId id;

    public int grade;

    public Enrollment() {
    }

    public Enrollment(EnrollmentId id, int grade) {
        this.id = id;
        this.grade = grade;
    }
}