package com.bookloop.config;

import com.bookloop.entity.*;
import com.bookloop.repository.OrderRepository;
import com.bookloop.repository.ProductRepository;
import com.bookloop.repository.ReviewRepository;
import com.bookloop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds the database with fictional demonstration data on first run.
 * Only active when the "dev" profile is enabled AND when no users exist yet.
 *
 * Usage: run with -Dspring.profiles.active=dev
 *
 * ALL DATA BELOW IS FICTIONAL AND FOR DEMONSTRATION PURPOSES ONLY.
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      ProductRepository productRepository,
                      OrderRepository orderRepository,
                      ReviewRepository reviewRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Ensure at least one ADMIN user exists (even if DB has existing data)
        if (userRepository.count() > 0) {
            ensureAdminExists();
            log.info("Database already has data — skipping full seed.");
            return;
        }

        log.info("Seeding database with demonstration data...");

        // --- Create demo users (all passwords are "password123") ---
        User alice = createUser("Alice Johnson", "alice@university.edu",
                "State University", "Main Campus", "+1-555-0101", Role.STUDENT);
        User bob = createUser("Bob Martinez", "bob@university.edu",
                "State University", "North Campus", "+1-555-0102", Role.STUDENT);
        User carol = createUser("Carol Chen", "carol@university.edu",
                "Tech Institute", "Downtown Campus", "+1-555-0103", Role.STUDENT);

        // --- Create demo admin account ---
        // Default credentials: admin@bookloop.edu / admin123
        // IMPORTANT: Change admin password immediately after first login.
        createUser("Admin User", "admin@bookloop.edu",
                "BookLoop Platform", "Admin Office", null, Role.ADMIN);

        // --- Create demo product listings with realistic VND prices ---
        Product p1 = createProduct("Head First Java", "Kathy Sierra, Bert Bates",
                "O'Reilly Media", "2nd", "978-0596009205", "CS101",
                "Computer Science", "Textbook",
                new BigDecimal("65000"), Condition.GOOD,
                "Main Campus Library",
                "Some highlighting in chapters 3-5. Cover has minor wear.",
                "https://m.media-amazon.com/images/I/81U5RkFRGlL._SL1500_.jpg",
                alice, ListingStatus.AVAILABLE);

        Product p2 = createProduct("Introduction to Algorithms", "Thomas H. Cormen et al.",
                "MIT Press", "3rd", "978-0262033848", "CS201",
                "Computer Science", "Reference textbook, very thorough coverage of algorithm design.",
                new BigDecimal("120000"), Condition.LIKE_NEW,
                "North Campus Bookstore",
                null,
                "https://m.media-amazon.com/images/I/61ZXunAMYyL._SL1233_.jpg",
                bob, ListingStatus.SOLD);

        Product p3 = createProduct("Principles of Marketing", "Philip Kotler",
                "Pearson", "17th", "978-0134492513", "MKT201",
                "Business", "Used for Marketing 201. All pages intact.",
                new BigDecimal("75000"), Condition.ACCEPTABLE,
                "Student Center",
                "Pen markings on some pages. Spine slightly creased.",
                "https://m.media-amazon.com/images/I/61rxNnGr+LL._SL1000_.jpg",
                carol, ListingStatus.AVAILABLE);

        Product p4 = createProduct("Calculus: Early Transcendentals", "James Stewart",
                "Cengage Learning", "8th", "978-1285741550", "MATH101",
                "Mathematics", "Standard calculus textbook for first and second year students.",
                new BigDecimal("85000"), Condition.GOOD,
                "Downtown Campus Café",
                "Previous owner's name on the first page.",
                "https://m.media-amazon.com/images/I/51byHhcRxAL._SL1000_.jpg",
                carol, ListingStatus.AVAILABLE);

        Product p5 = createProduct("Operating System Concepts", "Abraham Silberschatz",
                "Wiley", "10th", "978-1119800361", "CS301",
                "Computer Science", "The dinosaur book. Comprehensive coverage of OS design.",
                new BigDecimal("110000"), Condition.NEW,
                "Main Campus Library",
                null,
                "https://m.media-amazon.com/images/I/81RrFzjbdFL._SL1500_.jpg",
                alice, ListingStatus.AVAILABLE);

        Product p6 = createProduct("Microeconomics", "N. Gregory Mankiw",
                "Cengage Learning", "9th", "978-1337617406", "ECON101",
                "Economics", "Great introductory economics textbook.",
                new BigDecimal("70000"), Condition.LIKE_NEW,
                "North Campus Bookstore",
                null,
                "https://m.media-amazon.com/images/I/81bH9j+k0wL._SL1500_.jpg",
                bob, ListingStatus.AVAILABLE);

        Product p7 = createProduct("Organic Chemistry", "Paula Y. Bruice",
                "Pearson", "8th", "978-0134042282", "CHEM201",
                "Chemistry", "Orgo textbook with study guide and practice problems.",
                new BigDecimal("95000"), Condition.GOOD,
                "Science Building Lobby",
                "Some tabs stuck on key chapters. Clean interior.",
                "https://m.media-amazon.com/images/I/71u9sJm9D2L._SL1500_.jpg",
                alice, ListingStatus.AVAILABLE);

        Product p8 = createProduct("Database System Concepts", "Abraham Silberschatz",
                "McGraw Hill", "7th", "978-0078022159", "CS305",
                "Computer Science", "Covers relational model, SQL, and database design.",
                new BigDecimal("60000"), Condition.ACCEPTABLE,
                "Student Center",
                "Water stain on back cover. Pages are clean and intact.",
                "https://m.media-amazon.com/images/I/71Y8KzN+p9L._SL1500_.jpg",
                bob, ListingStatus.AVAILABLE);

        // --- Demo Order 1: Completed order between Alice (buyer) and Bob (seller) with a Review ---
        Order order1 = new Order();
        order1.setOrderNumber("ORD-20261022-DEMO01");
        order1.setBuyer(alice);
        order1.setOrderDate(LocalDateTime.now().minusDays(3));
        order1.setTotalAmount(new BigDecimal("120000"));
        order1.setPaymentMethod(PaymentMethod.IN_PERSON_MEETUP);
        order1.setStatus(OrderStatus.COMPLETED);
        order1.setMeetupNote("Met at North Campus Library at 11 AM.");
        order1.setContactPhone(alice.getPhone());

        OrderItem item1 = new OrderItem(order1, p2, bob, new BigDecimal("120000"), 1);
        order1.addItem(item1);
        orderRepository.save(order1);

        Review review1 = new Review(order1, alice, bob, 5,
                "Great textbook! Clean copy, exactly as described, and Bob was on time for our library meetup.");
        reviewRepository.save(review1);

        // --- Demo Order 2: Pending order between Carol (buyer) and Alice (seller) ---
        Order order2 = new Order();
        order2.setOrderNumber("ORD-20261022-DEMO02");
        order2.setBuyer(carol);
        order2.setOrderDate(LocalDateTime.now().minusHours(2));
        order2.setTotalAmount(new BigDecimal("65000"));
        order2.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        order2.setStatus(OrderStatus.PENDING);
        order2.setMeetupNote("Can we meet between 2:00 PM and 4:00 PM outside Main Campus Library?");
        order2.setContactPhone(carol.getPhone());

        OrderItem item2 = new OrderItem(order2, p1, alice, new BigDecimal("65000"), 1);
        order2.addItem(item2);
        orderRepository.save(order2);

        log.info("Seed data created: {} users, {} products, {} orders, {} reviews.",
                userRepository.count(), productRepository.count(), orderRepository.count(), reviewRepository.count());
    }

    private User createUser(String fullName, String email,
                            String university, String campus, String phone, Role role) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        // Use a simple default password encoded with BCrypt.
        // IMPORTANT: Change after first login in production.
        String defaultPassword = (role == Role.ADMIN) ? "admin123" : "password123";
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setUniversity(university);
        user.setCampus(campus);
        user.setPhone(phone);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Product createProduct(String title, String author, String publisher,
                                String edition, String isbn, String courseCode,
                                String category, String description,
                                BigDecimal price, Condition condition,
                                String campus, String defectNotes,
                                String imageUrl, User seller, ListingStatus status) {
        Product p = new Product();
        p.setTitle(title);
        p.setAuthor(author);
        p.setPublisher(publisher);
        p.setEdition(edition);
        p.setIsbn(isbn);
        p.setCourseCode(courseCode);
        p.setCategory(category);
        p.setDescription(description);
        p.setPrice(price);
        p.setCondition(condition);
        p.setCampus(campus);
        p.setDefectNotes(defectNotes);
        p.setImageUrl(imageUrl);
        p.setStatus(status);
        p.setSeller(seller);
        return productRepository.save(p);
    }

    /**
     * Ensures an admin account exists. Creates one if none found.
     * Safe to call on existing databases — only inserts if no ADMIN role exists.
     *
     * Default admin credentials: admin@bookloop.edu / admin123
     * IMPORTANT: Change admin password after first login.
     */
    private void ensureAdminExists() {
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            log.warn("No admin account found. Creating default admin: admin@bookloop.edu");
            User admin = new User();
            admin.setFullName("Admin User");
            admin.setEmail("admin@bookloop.edu");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setUniversity("BookLoop Platform");
            admin.setCampus("Admin Office");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.warn("Default admin created. Login: admin@bookloop.edu / admin123. CHANGE PASSWORD IMMEDIATELY.");
        }
    }
}
