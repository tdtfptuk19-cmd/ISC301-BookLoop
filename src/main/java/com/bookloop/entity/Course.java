package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Course or subject in an academic program (e.g., PRF192, CSD201, INT1006).
 */
@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_course_program", columnList = "program_id"),
        @Index(name = "idx_course_code", columnList = "course_code")
})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private AcademicProgram program;

    @Size(max = 50)
    @Column(name = "course_code", length = 50)
    private String courseCode;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 2000)
    @Column(name = "description", columnDefinition = "NVARCHAR(2000)")
    private String description;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "credits")
    private Integer credits;

    @Column(name = "is_mandatory")
    private Boolean mandatory;

    public Course() {}

    public Course(AcademicProgram program, String courseCode, String name, String description, Integer semester, Integer credits, Boolean mandatory) {
        this.program = program;
        this.courseCode = courseCode;
        this.name = name;
        this.description = description;
        this.semester = semester;
        this.credits = credits;
        this.mandatory = mandatory;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AcademicProgram getProgram() { return program; }
    public void setProgram(AcademicProgram program) { this.program = program; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }
}

