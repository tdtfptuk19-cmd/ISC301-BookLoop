package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Academic degree program or major (e.g., Kỹ thuật phần mềm, Khoa học máy tính, Kế toán doanh nghiệp).
 */
@Entity
@Table(name = "academic_programs", indexes = {
        @Index(name = "idx_prog_institution", columnList = "institution_id"),
        @Index(name = "idx_prog_campus", columnList = "campus_id")
})
public class AcademicProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = true)
    private Campus campus;

    @Size(max = 150)
    @Column(name = "faculty", length = 150)
    private String faculty;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 100)
    @Column(name = "major", length = 100)
    private String major;

    @Size(max = 50)
    @Column(name = "program_code", length = 50)
    private String programCode;

    @Size(max = 500)
    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    public AcademicProgram() {}

    public AcademicProgram(Institution institution, Campus campus, String faculty, String name, String major, String programCode, String sourceUrl) {
        this.institution = institution;
        this.campus = campus;
        this.faculty = faculty;
        this.name = name;
        this.major = major;
        this.programCode = programCode;
        this.sourceUrl = sourceUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public Campus getCampus() { return campus; }
    public void setCampus(Campus campus) { this.campus = campus; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getProgramCode() { return programCode; }
    public void setProgramCode(String programCode) { this.programCode = programCode; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}

