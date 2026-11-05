package com.bookloop.service;

import com.bookloop.entity.*;
import com.bookloop.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service providing textbook catalog discovery for university students (FPT, VNU, AOF).
 */
@Service
@Transactional(readOnly = true)
public class TextbookCatalogService {

    private final InstitutionRepository institutionRepository;
    private final CampusRepository campusRepository;
    private final AcademicProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final AcademicBookRepository bookRepository;
    private final CourseBookRepository courseBookRepository;
    private final ProductRepository productRepository;

    public TextbookCatalogService(InstitutionRepository institutionRepository,
                                  CampusRepository campusRepository,
                                  AcademicProgramRepository programRepository,
                                  CourseRepository courseRepository,
                                  AcademicBookRepository bookRepository,
                                  CourseBookRepository courseBookRepository,
                                  ProductRepository productRepository) {
        this.institutionRepository = institutionRepository;
        this.campusRepository = campusRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.bookRepository = bookRepository;
        this.courseBookRepository = courseBookRepository;
        this.productRepository = productRepository;
    }

    public List<Institution> getActiveInstitutions() {
        return institutionRepository.findAllByActiveTrueOrderByNameAsc();
    }

    public Institution getInstitutionById(Long id) {
        return institutionRepository.findById(id).orElse(null);
    }

    public List<Campus> getCampusesByInstitution(Long institutionId) {
        return campusRepository.findByInstitutionIdOrderByNameAsc(institutionId);
    }

    public List<AcademicProgram> getProgramsByInstitution(Long institutionId) {
        return programRepository.findByInstitutionIdOrderByNameAsc(institutionId);
    }

    public List<AcademicProgram> getProgramsByCampus(Long campusId) {
        return programRepository.findByCampusIdOrderByNameAsc(campusId);
    }

    public AcademicProgram getProgramById(Long id) {
        return programRepository.findById(id).orElse(null);
    }

    public List<Course> getCoursesByProgram(Long programId) {
        return courseRepository.findByProgramIdOrderBySemesterAscNameAsc(programId);
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public List<CourseBook> getBooksForCourse(Long courseId) {
        return courseBookRepository.findByCourseIdWithBook(courseId);
    }

    public AcademicBook getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public List<CourseBook> getCoursesForBook(Long bookId) {
        return courseBookRepository.findByBookIdWithCourseDetails(bookId);
    }

    public Page<AcademicBook> searchAcademicBooks(String keyword, String category, Pageable pageable) {
        return searchAcademicBooks(null, null, null, null, category, keyword, pageable);
    }

    public Page<AcademicBook> searchAcademicBooks(Long institutionId, Long campusId, Long programId, Long courseId,
                                                  String category, String keyword, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cat = (category != null && !category.isBlank()) ? category.trim() : null;
        return bookRepository.searchWithHierarchy(institutionId, campusId, programId, courseId, cat, kw, pageable);
    }

    public Page<Course> searchCourses(String keyword, Long institutionId, Long programId, Pageable pageable) {
        return courseRepository.searchCourses(
                (keyword != null && keyword.isBlank()) ? null : keyword,
                institutionId, programId, pageable);
    }

    public List<String> getCategories() {
        return bookRepository.findDistinctCategories();
    }

    /**
     * Finds active used marketplace listings posted by students for this academic book.
     */
    public List<Product> getAvailableUsedListings(Long academicBookId) {
        return productRepository.findByAcademicBookIdAndStatusOrderByCreatedAtDesc(academicBookId, ListingStatus.AVAILABLE);
    }
}

