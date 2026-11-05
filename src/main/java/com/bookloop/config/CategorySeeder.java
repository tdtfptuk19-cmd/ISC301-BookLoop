package com.bookloop.config;

import com.bookloop.entity.Category;
import com.bookloop.entity.Product;
import com.bookloop.repository.AcademicBookRepository;
import com.bookloop.repository.CategoryRepository;
import com.bookloop.repository.ProductRepository;
import com.bookloop.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds standard university and academic textbook categories in SQL Server
 * and migrates/links existing listings to the Category entity.
 */
@Component
@Order(30)
public class CategorySeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CategorySeeder.class);

    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final AcademicBookRepository academicBookRepository;

    public CategorySeeder(CategoryRepository categoryRepository,
                          CategoryService categoryService,
                          ProductRepository productRepository,
                          AcademicBookRepository academicBookRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.academicBookRepository = academicBookRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing BookLoop Category Catalog from SQL Server...");

        // Standard academic categories with descriptions and display orders
        Map<String, String> standardCategories = new LinkedHashMap<>();
        standardCategories.put("Công nghệ thông tin", "Khoa học máy tính, kỹ thuật phần mềm, hệ thống thông tin, mạng máy tính");
        standardCategories.put("Khoa học dữ liệu & AI", "Trí tuệ nhân tạo, Machine Learning, Data Science, Big Data, thị giác máy tính");
        standardCategories.put("Kinh tế - Quản trị", "Kinh tế học, quản trị kinh doanh, thương mại điện tử, logistics, nhân sự");
        standardCategories.put("Marketing", "Marketing căn bản, Digital Marketing, PR, truyền thông, hành vi người tiêu dùng");
        standardCategories.put("Kế toán - Tài chính", "Kế toán doanh nghiệp, kiểm toán, tài chính ngân hàng, thuế, chứng khoán");
        standardCategories.put("Khoa học cơ bản", "Toán giải tích, đại số tuyến tính, xác suất thống kê, vật lý, triết học");
        standardCategories.put("Kỹ thuật - Công nghệ", "Điện - điện tử, viễn thông, cơ điện tử, tự động hóa, kỹ thuật số");
        standardCategories.put("Luật - Chính trị", "Pháp luật đại cương, luật kinh tế, luật dân sự, đường lối tư tưởng");
        standardCategories.put("Ngoại ngữ", "Tiếng Anh chuyên ngành, IELTS, TOEIC, tiếng Nhật, tiếng Hàn, tiếng Trung");
        standardCategories.put("Thiết kế - Đồ họa", "Thiết kế đồ họa, UI/UX, mỹ thuật số, multimedia, thiết kế web");
        standardCategories.put("Khoa học xã hội", "Tâm lý học, xã hội học, kỹ năng giao tiếp, phương pháp nghiên cứu khoa học");
        standardCategories.put("Sách kỹ năng & Văn học", "Kỹ năng mềm, phát triển bản thân, phương pháp học tập, văn học kinh điển");
        standardCategories.put("Khác", "Tài liệu học tập khác, đề cương, sách tham khảo tổng hợp");

        int order = 1;
        for (Map.Entry<String, String> entry : standardCategories.entrySet()) {
            String name = entry.getKey();
            String desc = entry.getValue();
            if (!categoryRepository.existsByNameIgnoreCase(name)) {
                Category cat = new Category();
                cat.setName(name);
                cat.setDescription(desc);
                cat.setSlug(CategoryService.generateSlug(name));
                cat.setDisplayOrder(order++);
                cat.setActive(true);
                categoryRepository.save(cat);
                log.info("Seeded category: {} (order: {})", name, cat.getDisplayOrder());
            } else {
                order++;
            }
        }

        // Migrate any unlinked existing Products to Category entity
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            if (product.getCategoryRef() == null && product.getCategory() != null && !product.getCategory().isBlank()) {
                String catName = product.getCategory().trim();
                Category resolved = categoryRepository.findByNameIgnoreCase(catName)
                        .orElseGet(() -> categoryService.getOrCreate(catName, 99));
                product.setCategoryRef(resolved);
                productRepository.save(product);
            }
        }

        log.info("Category catalog initialized successfully with {} active categories.",
                categoryRepository.findAllByActiveTrueOrderByDisplayOrderAscNameAsc().size());
    }
}

