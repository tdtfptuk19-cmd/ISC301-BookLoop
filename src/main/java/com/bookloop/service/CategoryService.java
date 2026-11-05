package com.bookloop.service;

import com.bookloop.entity.Category;
import com.bookloop.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> findAllActive() {
        return categoryRepository.findAllByActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        if (id == null) return Optional.empty();
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findByName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        return categoryRepository.findByNameIgnoreCase(name.trim());
    }

    @Transactional(readOnly = true)
    public Optional<Category> findBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) return Optional.empty();
        return categoryRepository.findBySlug(slug.trim());
    }

    public Category save(Category category) {
        if (category.getSlug() == null || category.getSlug().isBlank()) {
            category.setSlug(generateSlug(category.getName()));
        }
        return categoryRepository.save(category);
    }

    public Category getOrCreate(String name, Integer displayOrder) {
        if (name == null || name.trim().isEmpty()) return null;
        String clean = name.trim();
        return categoryRepository.findByNameIgnoreCase(clean)
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName(clean);
                    cat.setSlug(generateSlug(clean));
                    cat.setDisplayOrder(displayOrder != null ? displayOrder : 99);
                    cat.setActive(true);
                    return categoryRepository.save(cat);
                });
    }

    public static String generateSlug(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[đĐ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return normalized;
    }
}

