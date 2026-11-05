package com.bookloop.config;

import com.bookloop.entity.*;
import com.bookloop.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Automatically seeds the 4 core BookLoop demonstration accounts and a rich
 * collection of marketplace listings for peer-to-peer student buying/selling demo.
 *
 * Accounts (all with password: '123456'):
 * 1. BookLoop Demo Admin (admin@bookloop.example) - ADMIN
 * 2. Quang Huy (quanghuy@bookloop.example) - FPT University (Buyer & Seller)
 * 3. Vũ Tùng (vutung@bookloop.example) - VNU (Buyer & Seller)
 * 4. Đại Thành (daithanh@bookloop.example) - AOF (Buyer & Seller)
 */
@Component
@Order(25)
@ConditionalOnProperty(name = "bookloop.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class DemoAccountSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoAccountSeeder.class);

    private final UserRepository userRepository;
    private final SellerPaymentAccountRepository sellerPaymentAccountRepository;
    private final ProductRepository productRepository;
    private final AcademicBookRepository academicBookRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoAccountSeeder(UserRepository userRepository,
                             SellerPaymentAccountRepository sellerPaymentAccountRepository,
                             ProductRepository productRepository,
                             AcademicBookRepository academicBookRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sellerPaymentAccountRepository = sellerPaymentAccountRepository;
        this.productRepository = productRepository;
        this.academicBookRepository = academicBookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking and seeding BookLoop Demo Accounts & Marketplace Listings...");

        // 1. Admin
        seedUser(
                "BookLoop Demo Admin",
                "admin@bookloop.example",
                Role.ADMIN,
                "BookLoop Platform",
                "Admin Office",
                "0980001122"
        );

        // 2. Quang Huy (FPT University)
        User quangHuy = seedUser(
                "Quang Huy",
                "quanghuy@bookloop.example",
                Role.STUDENT,
                "Trường Đại học FPT",
                "FPT University Hòa Lạc",
                "0981112233"
        );
        ensureSellerAccount(quangHuy, "TPBank", "TPB", "*******2233", "0981112233", "QUANG HUY");
        seedFptuListings(quangHuy);

        // 3. Vũ Tùng (VNU ĐHQGHN)
        User vuTung = seedUser(
                "Vũ Tùng",
                "vutung@bookloop.example",
                Role.STUDENT,
                "Đại học Quốc gia Hà Nội",
                "VNU Hòa Lạc (Đô thị ĐHQGHN)",
                "0982223344"
        );
        ensureSellerAccount(vuTung, "MB Bank", "MB", "*******3344", "0982223344", "VU TUNG");
        seedVnuListings(vuTung);

        // 4. Đại Thành (AOF Học viện Tài chính)
        User daiThanh = seedUser(
                "Đại Thành",
                "daithanh@bookloop.example",
                Role.STUDENT,
                "Học viện Tài chính",
                "Học viện Tài chính - Cơ sở Hòa Lạc",
                "0983334455"
        );
        ensureSellerAccount(daiThanh, "Techcombank", "TCB", "*******4455", "1903334455", "DAI THANH");
        seedAofListings(daiThanh);

        log.info("Demo Accounts & Marketplace Listings ready for peer-to-peer demo!");
    }

    private void seedFptuListings(User seller) {
        seedSingleListing(
                seller,
                "Lập trình Java căn bản & Nâng cao (PRJ301)",
                "Kathy Sierra, Bert Bates",
                "PRJ301",
                "Công nghệ thông tin",
                new BigDecimal("75000"),
                Condition.LIKE_NEW,
                "Sách photo đẹp, bìa màu, bọc bìa kiếng cẩn thận. Tặng kèm tập code mẫu Assignment kỳ trước được 9.5 điểm.",
                "https://m.media-amazon.com/images/I/81U5RkFRGlL._SL1500_.jpg",
                null
        );

        seedSingleListing(
                seller,
                "Cấu trúc dữ liệu và Giải thuật (CSD201)",
                "Thomas H. Cormen",
                "CSD201",
                "Công nghệ thông tin",
                new BigDecimal("85000"),
                Condition.GOOD,
                "Bản tóm tắt giải thuật Introduction to Algorithms cho môn CSD201. Đầy đủ cây nhị phân, đồ thị, thuật toán sắp xếp.",
                "https://m.media-amazon.com/images/I/61ZXunAMYyL._SL1233_.jpg",
                "Có ghi chú giải thích thuật toán bằng bút chì ở chương 4."
        );

        seedSingleListing(
                seller,
                "Giáo trình Xác suất Thống kê Ứng dụng (MAS291)",
                "Jay L. Devore",
                "MAS291",
                "Khoa học cơ bản",
                new BigDecimal("60000"),
                Condition.LIKE_NEW,
                "Sách giáo trình MAS291 in rõ nét, đầy đủ bảng tra phân phối chuẩn, t-student và công thức ôn thi Final.",
                "https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg",
                null
        );

        seedSingleListing(
                seller,
                "Cơ sở dữ liệu và Truy vấn SQL (DBI202)",
                "Alan Beaulieu",
                "DBI202",
                "Công nghệ thông tin",
                new BigDecimal("70000"),
                Condition.GOOD,
                "Cuốn Learning SQL rất hữu ích cho môn DBI202 và ôn thi thực hành PE. Hướng dẫn viết query từ cơ bản tới nâng cao.",
                "https://covers.openlibrary.org/b/isbn/9781492057611-L.jpg",
                "Gáy sách hơi sờn nhẹ nhưng không rách trang."
        );

        seedSingleListing(
                seller,
                "Kỹ năng làm việc nhóm & Giao tiếp sinh viên (SSG104)",
                "Philip Kotler",
                "SSG104",
                "Đại cương & Ngoại ngữ",
                new BigDecimal("45000"),
                Condition.NEW,
                "Sách kỹ năng mềm mới 100%, chưa dùng do học kỳ vừa rồi học trực tuyến. Thích hợp cho tân sinh viên.",
                "https://m.media-amazon.com/images/I/61rxNnGr+LL._SL1000_.jpg",
                null
        );

        seedSingleListing(
                seller,
                "Mạng máy tính và An toàn hệ thống (NWC203c)",
                "Andrew S. Tanenbaum",
                "NWC203c",
                "Công nghệ thông tin",
                new BigDecimal("80000"),
                Condition.LIKE_NEW,
                "Giáo trình Computer Networks chuẩn syllabus FPT. Minh họa rõ ràng mô hình OSI, TCP/IP, định tuyến và an ninh mạng.",
                "https://covers.openlibrary.org/b/isbn/9780132126953-L.jpg",
                null
        );
    }

    private void seedVnuListings(User seller) {
        seedSingleListing(
                seller,
                "Lập trình Web với Java & Spring Boot (Thực hành)",
                "Joshua Bloch",
                "INT1008",
                "Công nghệ thông tin",
                new BigDecimal("75000"),
                Condition.LIKE_NEW,
                "Sách Effective Java kết hợp hướng dẫn xây dựng REST API với Spring Boot. Dành cho sinh viên UET ĐHQGHN.",
                "https://m.media-amazon.com/images/I/71oDkgbO7HL._SL1500_.jpg",
                null
        );

        seedSingleListing(
                seller,
                "Giải tích 1 & Đại số tuyến tính ĐHQGHN (MAT1093)",
                "ĐHQGHN Khoa Toán - Cơ",
                "MAT1093",
                "Khoa học cơ bản",
                new BigDecimal("55000"),
                Condition.GOOD,
                "Giáo trình Toán cao cấp dành cho khối kỹ thuật và công nghệ ĐHQGHN. Đầy đủ các chuyên đề tích phân và ma trận.",
                "https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg",
                "Có lời giải chi tiết các đề thi năm trước đính kèm ở cuối sách."
        );

        seedSingleListing(
                seller,
                "Vật lý đại cương 1 - Cơ nhiệt (PHY1100)",
                "Hugh D. Young, Roger A. Freedman",
                "PHY1100",
                "Khoa học cơ bản",
                new BigDecimal("50000"),
                Condition.ACCEPTABLE,
                "Giáo trình University Physics bản dịch, dùng cho học phần Vật lý đại cương tại ĐHQGHN Hòa Lạc.",
                "https://covers.openlibrary.org/b/isbn/9780321573513-L.jpg",
                "Bìa ngoài dán băng dính trong để bảo vệ, ruột sách hoàn toàn nguyên vẹn."
        );

        seedSingleListing(
                seller,
                "Lịch sử Đảng Cộng sản Việt Nam (HIS1001)",
                "Bộ Giáo dục và Đào tạo",
                "HIS1001",
                "Đại cương & Ngoại ngữ",
                new BigDecimal("40000"),
                Condition.LIKE_NEW,
                "Giáo trình chuẩn Bộ GD&ĐT xuất bản mới nhất. Có highlight sẵn các mốc sự kiện quan trọng để ôn thi trắc nghiệm.",
                "https://covers.openlibrary.org/b/isbn/9781118875056-L.jpg",
                null
        );

        seedSingleListing(
                seller,
                "Thiết kế & Phân tích Thuật toán nâng cao (INT2204)",
                "Steven S. Skiena",
                "INT2204",
                "Công nghệ thông tin",
                new BigDecimal("90000"),
                Condition.LIKE_NEW,
                "Cuốn The Algorithm Design Manual nổi tiếng. Rất hay cho sinh viên học nâng cao hoặc ôn thi lập trình thi đấu.",
                "https://books.google.com/books/content?id=TrXd-gxPhVYC&printsec=frontcover&img=1&zoom=1",
                null
        );

        seedSingleListing(
                seller,
                "Kinh tế Vi mô đại cương (BSA2001)",
                "N. Gregory Mankiw",
                "BSA2001",
                "Kinh tế - Quản trị kinh doanh",
                new BigDecimal("65000"),
                Condition.GOOD,
                "Sách Principles of Microeconomics bìa màu, diễn giải các quy luật cung cầu, thị trường và hành vi tiêu dùng.",
                "https://m.media-amazon.com/images/I/81bH9j+k0wL._SL1500_.jpg",
                null
        );
    }

    private void seedAofListings(User seller) {
        seedSingleListing(
                seller,
                "Giáo trình Nguyên lý Kế toán Doanh nghiệp AOF",
                "GS.TS. Ngô Thế Chi",
                "ACC101",
                "Kế toán - Tài chính",
                new BigDecimal("65000"),
                Condition.GOOD,
                "Giáo trình Nguyên lý kế toán chuẩn của Học viện Tài chính. Rất quan trọng cho sinh viên chuyên ngành Kế toán - Kiểm toán.",
                "https://covers.openlibrary.org/b/isbn/9781118875056-L.jpg",
                "Có ghi chú bút chì cẩn thận các dạng định khoản tài khoản kế toán."
        );

        seedSingleListing(
                seller,
                "Tài chính Doanh nghiệp căn bản (FIN101)",
                "PGS.TS. Bùi Văn Vần",
                "FIN101",
                "Kế toán - Tài chính",
                new BigDecimal("70000"),
                Condition.LIKE_NEW,
                "Giáo trình Tài chính doanh nghiệp AOF cơ sở Hòa Lạc. Sách trình bày rõ ràng về giá trị thời gian của tiền và đòn bẩy tài chính.",
                "https://covers.openlibrary.org/b/isbn/9780133970777-L.jpg",
                null
        );

        seedSingleListing(
                seller,
                "Kinh tế Vĩ mô ứng dụng (ECO102)",
                "N. Gregory Mankiw",
                "ECO102",
                "Kinh tế - Quản trị kinh doanh",
                new BigDecimal("60000"),
                Condition.LIKE_NEW,
                "Cuốn Principles of Macroeconomics bản dịch, đầy đủ mô hình IS-LM, lạm phát, thất nghiệp và chính sách tài khóa.",
                "https://m.media-amazon.com/images/I/81Wn0P8n67L._SL1500_.jpg",
                null
        );

        seedSingleListing(
                seller,
                "Luật Kinh tế và Thương mại đại cương (LAW101)",
                "Bộ môn Luật Kinh tế AOF",
                "LAW101",
                "Luật & Khoa học xã hội",
                new BigDecimal("50000"),
                Condition.GOOD,
                "Giáo trình cập nhật các văn bản luật doanh nghiệp mới, luật phá sản và hợp đồng kinh tế cho sinh viên tài chính.",
                "https://m.media-amazon.com/images/I/61rxNnGr+LL._SL1000_.jpg",
                null
        );

        seedSingleListing(
                seller,
                "Thuế và Kế toán Thuế Việt Nam (TAX201)",
                "PGS.TS. Nguyễn Thị Liên",
                "TAX201",
                "Kế toán - Tài chính",
                new BigDecimal("85000"),
                Condition.NEW,
                "Sách mới 100% chưa qua sử dụng. Trình bày chi tiết cách tính thuế GTGT, thuế TNDN, thuế TNCN theo thông tư mới nhất.",
                "https://covers.openlibrary.org/b/isbn/9781118875056-L.jpg",
                null
        );

        seedSingleListing(
                seller,
                "Kiểm toán căn bản và Báo cáo tài chính (AUD201)",
                "GS.TS. Vương Đình Huệ",
                "AUD201",
                "Kế toán - Tài chính",
                new BigDecimal("75000"),
                Condition.LIKE_NEW,
                "Giáo trình Kiểm toán Học viện Tài chính. Hệ thống hóa toàn bộ chuẩn mực kiểm toán Việt Nam (VSA) và kỹ thuật chọn mẫu.",
                "https://covers.openlibrary.org/b/isbn/9780072465631-L.jpg",
                null
        );
    }

    private void seedSingleListing(User seller, String title, String author, String courseCode,
                                   String category, BigDecimal price, Condition condition,
                                   String description, String imageUrl, String defectNotes) {

        boolean exists = productRepository.findAll().stream()
                .anyMatch(p -> p.getSeller() != null && p.getSeller().getId().equals(seller.getId())
                        && p.getTitle().equalsIgnoreCase(title));

        if (exists) {
            return;
        }

        Product product = new Product();
        product.setTitle(title);
        product.setAuthor(author);
        product.setCourseCode(courseCode);
        product.setCategory(category);
        product.setPrice(price);
        product.setCondition(condition);
        product.setStatus(ListingStatus.AVAILABLE);
        product.setCampus(seller.getCampus());
        product.setSeller(seller);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setDefectNotes(defectNotes);
        product.setCreatedAt(LocalDateTime.now().minusHours((long) (Math.random() * 48)));

        // Try to match an AcademicBook in the catalog
        String searchPrefix = category.contains("-") ? category.split("-")[0].trim() : category.split(" ")[0].trim();
        List<AcademicBook> academicBooks = academicBookRepository.findAll();
        Optional<AcademicBook> ab = academicBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(searchPrefix.toLowerCase()))
                .findFirst();
        ab.ifPresent(product::setAcademicBook);

        productRepository.save(product);
        log.info("Seeded listing: '{}' ({}) by seller {}", title, courseCode, seller.getEmail());
    }

    private User seedUser(String fullName, String email, Role role, String university, String campus, String phone) {
        String normalizedEmail = email.toLowerCase().trim();
        Optional<User> existing = userRepository.findByEmailIgnoreCase(normalizedEmail);
        if (existing.isPresent()) {
            User u = existing.get();
            if (!passwordEncoder.matches("123456", u.getPassword())) {
                u.setPassword(passwordEncoder.encode("123456"));
                userRepository.save(u);
                log.info("Updated password for demo account: {}", normalizedEmail);
            }
            return u;
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(role);
        user.setUniversity(university);
        user.setCampus(campus);
        user.setPhone(phone);
        user.setEnabled(true);
        user = userRepository.save(user);
        log.info("Created demo account: {} ({}) with role {}", fullName, normalizedEmail, role);
        return user;
    }

    private void ensureSellerAccount(User seller, String bankName, String bankCode, String mask, String fullNum, String holder) {
        Optional<SellerPaymentAccount> existing = sellerPaymentAccountRepository.findBySellerId(seller.getId());
        if (existing.isEmpty()) {
            SellerPaymentAccount account = new SellerPaymentAccount();
            account.setSeller(seller);
            account.setProviderCode("MOCK_PAYMENT_PARTNER");
            account.setProviderRecipientId("REC_" + seller.getId() + "_" + bankCode);
            account.setBankName(bankName);
            account.setBankCode(bankCode);
            account.setAccountNumberMask(mask);
            account.setCitizenId("00120200" + (1000 + seller.getId()));
            account.setAccountHolderName(holder);
            account.setKycStatus(KycStatus.VERIFIED);
            account.setStatus(SellerAccountStatus.ACTIVE);
            sellerPaymentAccountRepository.save(account);
            log.info("Configured SellerPaymentAccount for seller {}", seller.getEmail());
        }
    }
}
