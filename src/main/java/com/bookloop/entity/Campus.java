package com.bookloop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Campus location for an institution (e.g. FPTU Hoa Lac, VNU Hoa Lac, AOF Hoa Lac).
 */
@Entity
@Table(name = "campuses", indexes = {
        @Index(name = "idx_campus_institution", columnList = "institution_id")
})
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 255)
    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "is_hoa_lac", nullable = false)
    private boolean hoaLac = true;

    public Campus() {}

    public Campus(Institution institution, String name, String location, boolean hoaLac) {
        this.institution = institution;
        this.name = name;
        this.location = location;
        this.hoaLac = hoaLac;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isHoaLac() { return hoaLac; }
    public void setHoaLac(boolean hoaLac) { this.hoaLac = hoaLac; }
}

