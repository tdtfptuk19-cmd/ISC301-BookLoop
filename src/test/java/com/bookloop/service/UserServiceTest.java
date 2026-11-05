package com.bookloop.service;

import com.bookloop.dto.RegistrationForm;
import com.bookloop.entity.Role;
import com.bookloop.entity.User;
import com.bookloop.exception.DuplicateEmailException;
import com.bookloop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserService (registration, profile update, lookup).
 * Runs against H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.bookloop.repository.CartRepository cartRepository;

    @Autowired
    private com.bookloop.repository.PromotionPaymentRepository promotionPaymentRepository;

    @Autowired
    private com.bookloop.repository.ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        promotionPaymentRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    // --- Registration tests ---

    @Test
    @DisplayName("Register a new user successfully")
    void register_success() {
        RegistrationForm form = createForm("Test User", "test@example.com",
                "password123", "password123", "State University", "Main Campus");

        User user = userService.register(form);

        assertNotNull(user.getId());
        assertEquals("Test User", user.getFullName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(Role.STUDENT, user.getRole());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("Passwords are hashed with BCrypt, never stored as plaintext")
    void register_passwordIsHashed() {
        RegistrationForm form = createForm("Test User", "hash@example.com",
                "myplaintext", "myplaintext", null, null);

        User user = userService.register(form);

        assertNotEquals("myplaintext", user.getPassword());
        assertTrue(passwordEncoder.matches("myplaintext", user.getPassword()));
    }

    @Test
    @DisplayName("Duplicate email throws DuplicateEmailException")
    void register_duplicateEmail() {
        RegistrationForm form1 = createForm("User One", "dup@example.com",
                "password1", "password1", null, null);
        userService.register(form1);

        RegistrationForm form2 = createForm("User Two", "dup@example.com",
                "password2", "password2", null, null);

        assertThrows(DuplicateEmailException.class, () -> userService.register(form2));
    }

    @Test
    @DisplayName("Email is normalized to lowercase")
    void register_emailNormalized() {
        RegistrationForm form = createForm("User", "UPPER@EXAMPLE.COM",
                "password123", "password123", null, null);

        User user = userService.register(form);

        assertEquals("upper@example.com", user.getEmail());
    }

    @Test
    @DisplayName("Role is always STUDENT, never controlled by input")
    void register_roleIsStudent() {
        RegistrationForm form = createForm("User", "role@example.com",
                "password123", "password123", null, null);

        User user = userService.register(form);

        assertEquals(Role.STUDENT, user.getRole());
    }

    // --- Lookup tests ---

    @Test
    @DisplayName("Find user by email (case-insensitive)")
    void findByEmail_caseInsensitive() {
        RegistrationForm form = createForm("Lookup User", "lookup@example.com",
                "password123", "password123", null, null);
        userService.register(form);

        Optional<User> found = userService.findByEmail("LOOKUP@EXAMPLE.COM");

        assertTrue(found.isPresent());
        assertEquals("Lookup User", found.get().getFullName());
    }

    @Test
    @DisplayName("Find by email returns empty for nonexistent user")
    void findByEmail_notFound() {
        Optional<User> found = userService.findByEmail("nonexistent@example.com");
        assertTrue(found.isEmpty());
    }

    // --- Helper ---

    private RegistrationForm createForm(String fullName, String email,
                                         String password, String confirmPassword,
                                         String university, String campus) {
        RegistrationForm form = new RegistrationForm();
        form.setFullName(fullName);
        form.setEmail(email);
        form.setPassword(password);
        form.setConfirmPassword(confirmPassword);
        form.setUniversity(university);
        form.setCampus(campus);
        return form;
    }
}
