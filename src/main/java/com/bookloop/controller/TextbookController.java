package com.bookloop.controller;

import com.bookloop.entity.AcademicBook;
import com.bookloop.entity.Course;
import com.bookloop.entity.CourseBook;
import com.bookloop.entity.Product;
import com.bookloop.service.TextbookCatalogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Academic Textbook Catalog discovery feature.
 * Path: /textbooks ("Tìm giáo trình")
 */
@Controller
@RequestMapping("/textbooks")
public class TextbookController {

    private static final int DEFAULT_PAGE_SIZE = 8;
    private final TextbookCatalogService catalogService;

    public TextbookController(TextbookCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * Browse and search academic textbooks across institutions (FPT, VNU, AOF).
     * Cascading filters: Institution -> Campus -> Program -> Course -> Textbooks.
     */
    @GetMapping
    public String browseTextbooks(@RequestParam(required = false) Long institutionId,
                                  @RequestParam(required = false) Long campusId,
                                  @RequestParam(required = false) Long programId,
                                  @RequestParam(required = false) Long courseId,
                                  @RequestParam(required = false) String category,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "8") int size,
                                  Model model) {

        model.addAttribute("institutions", catalogService.getActiveInstitutions());
        model.addAttribute("categories", catalogService.getCategories());
        model.addAttribute("selectedInstitutionId", institutionId);
        model.addAttribute("selectedCampusId", campusId);
        model.addAttribute("selectedProgramId", programId);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);

        if (institutionId != null) {
            model.addAttribute("campuses", catalogService.getCampusesByInstitution(institutionId));
            model.addAttribute("programs", catalogService.getProgramsByInstitution(institutionId));
        } else if (campusId != null) {
            model.addAttribute("programs", catalogService.getProgramsByCampus(campusId));
        }

        if (programId != null) {
            model.addAttribute("courses", catalogService.getCoursesByProgram(programId));
        }

        // Search textbooks matching the selected hierarchy and keywords
        Page<AcademicBook> books = catalogService.searchAcademicBooks(
                institutionId, campusId, programId, courseId, category, keyword, PageRequest.of(page, size));
        model.addAttribute("books", books);

        // Preload associated course mappings for all books on this page
        Map<Long, List<CourseBook>> bookCoursesMap = new HashMap<>();
        for (AcademicBook b : books.getContent()) {
            bookCoursesMap.put(b.getId(), catalogService.getCoursesForBook(b.getId()));
        }
        model.addAttribute("bookCoursesMap", bookCoursesMap);

        return "textbooks/browse";
    }

    /**
     * Standalone view showing all recommended and required textbooks for a specific course.
     */
    @GetMapping("/courses/{courseId}")
    public String viewCourseBooks(@PathVariable Long courseId, Model model) {
        Course course = catalogService.getCourseById(courseId);
        if (course == null) {
            return "redirect:/textbooks";
        }
        List<CourseBook> courseBooks = catalogService.getBooksForCourse(courseId);
        model.addAttribute("selectedCourse", course);
        model.addAttribute("courseBooks", courseBooks);
        return "textbooks/course-books";
    }

    /**
     * View detail of a specific academic textbook and matching used copies from BookLoop sellers.
     */
    @GetMapping("/{id}")
    public String viewTextbookDetail(@PathVariable Long id, Model model) {
        AcademicBook book = catalogService.getBookById(id);
        if (book == null) {
            return "redirect:/textbooks";
        }

        List<CourseBook> courses = catalogService.getCoursesForBook(id);
        List<Product> availableUsedCopies = catalogService.getAvailableUsedListings(id);

        model.addAttribute("book", book);
        model.addAttribute("courseMappings", courses);
        model.addAttribute("usedCopies", availableUsedCopies);
        return "textbooks/detail";
    }
}

