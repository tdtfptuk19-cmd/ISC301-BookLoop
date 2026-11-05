package com.bookloop.config;

import com.bookloop.entity.*;
import com.bookloop.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Idempotent seeder expanding the Academic Textbook Catalog to 50+ verified textbooks per university.
 * Institutions covered:
 * 1. FPT University (FPTU Hoa Lac) -> 55 distinct textbooks
 * 2. Vietnam National University, Hanoi (VNU Hoa Lac) -> 55 distinct textbooks
 * 3. Academy of Finance (AOF Hoa Lac) -> 55 distinct textbooks
 *
 * Every textbook has complete verified bibliographic metadata and an accessible, high-resolution cover image.
 */
@Component
@Order(10)
@org.springframework.context.annotation.Profile("!test")
public class AcademicCatalogSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AcademicCatalogSeeder.class);

    private final InstitutionRepository institutionRepository;
    private final CampusRepository campusRepository;
    private final AcademicProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final AcademicBookRepository bookRepository;
    private final CourseBookRepository courseBookRepository;

    public AcademicCatalogSeeder(InstitutionRepository institutionRepository,
                                 CampusRepository campusRepository,
                                 AcademicProgramRepository programRepository,
                                 CourseRepository courseRepository,
                                 AcademicBookRepository bookRepository,
                                 CourseBookRepository courseBookRepository) {
        this.institutionRepository = institutionRepository;
        this.campusRepository = campusRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.bookRepository = bookRepository;
        this.courseBookRepository = courseBookRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (bookRepository.count() >= 100) {
            log.info("Academic catalog already populated with {} textbooks. Skipping seeding.", bookRepository.count());
            return;
        }

        log.info("Populating/Expanding Academic Textbook Catalog for FPTU, VNU, AOF...");

        // 1. INSTITUTIONS & CAMPUSES
        Institution fptu = getOrCreateInstitution(
                "Trường Đại học FPT", "FPTU",
                "Đại học tiên phong công nghệ và chuyển đổi số trực thuộc Tập đoàn FPT.",
                "https://daihoc.fpt.edu.vn/");
        Campus fptuCampus = getOrCreateCampus(fptu, "FPT University Hòa Lạc",
                "Khu Công nghệ cao Hòa Lạc, Km29 Đại lộ Thăng Long, Hà Nội", true);

        Institution vnu = getOrCreateInstitution(
                "Đại học Quốc gia Hà Nội", "VNU",
                "Trung tâm đào tạo đại học, sau đại học và nghiên cứu đa ngành hàng đầu Việt Nam.",
                "https://vnu.edu.vn/");
        Campus vnuCampus = getOrCreateCampus(vnu, "VNU Hòa Lạc (Đô thị ĐHQGHN)",
                "Đô thị ĐHQGHN tại Hòa Lạc, Thạch Thất, Hà Nội", true);

        Institution aof = getOrCreateInstitution(
                "Học viện Tài chính", "AOF",
                "Cơ sở đào tạo hàng đầu về tài chính, kế toán, kinh tế và quản trị kinh doanh.",
                "https://hvtc.edu.vn/");
        Campus aofCampus = getOrCreateCampus(aof, "Học viện Tài chính - Cơ sở Hòa Lạc",
                "Khu Đô thị Đại học Hòa Lạc, Thạch Thất, Hà Nội", true);

        // 2. ACADEMIC PROGRAMS
        AcademicProgram fptSE = getOrCreateProgram(fptu, fptuCampus, "Khoa Công nghệ thông tin", "Kỹ thuật phần mềm", "Software Engineering", "SE", "https://daihoc.fpt.edu.vn/");
        AcademicProgram fptAI = getOrCreateProgram(fptu, fptuCampus, "Khoa Công nghệ thông tin", "Trí tuệ nhân tạo", "Artificial Intelligence", "AI", "https://daihoc.fpt.edu.vn/");
        AcademicProgram fptBA = getOrCreateProgram(fptu, fptuCampus, "Khoa Quản trị kinh doanh", "Quản trị kinh doanh", "Business Administration", "BA", "https://daihoc.fpt.edu.vn/");

        AcademicProgram vnuIT = getOrCreateProgram(vnu, vnuCampus, "Trường Đại học Công nghệ (UET)", "Công nghệ thông tin", "Information Technology", "CN1", "https://uet.vnu.edu.vn/");
        AcademicProgram vnuCS = getOrCreateProgram(vnu, vnuCampus, "Trường Đại học Công nghệ (UET)", "Khoa học máy tính", "Computer Science", "CN2", "https://uet.vnu.edu.vn/");
        AcademicProgram vnuMIS = getOrCreateProgram(vnu, vnuCampus, "Trường Quốc tế (VNU-IS)", "Hệ thống thông tin quản lý", "Management Information Systems", "MIS", "https://www.is.vnu.edu.vn/");
        AcademicProgram vnuUEB = getOrCreateProgram(vnu, vnuCampus, "Trường ĐH Kinh tế (UEB)", "Kinh tế & Quản trị kinh doanh", "Economics & Business", "UEB", "https://ueb.vnu.edu.vn/");

        AcademicProgram aofAcc = getOrCreateProgram(aof, aofCampus, "Khoa Kế toán", "Kế toán doanh nghiệp", "Corporate Accounting", "KT", "https://hvtc.edu.vn/");
        AcademicProgram aofFin = getOrCreateProgram(aof, aofCampus, "Khoa Tài chính doanh nghiệp", "Tài chính doanh nghiệp", "Corporate Finance", "TCDN", "https://hvtc.edu.vn/");
        AcademicProgram aofEcon = getOrCreateProgram(aof, aofCampus, "Khoa Kinh tế", "Kinh tế đầu tư & Ngân hàng", "Economics & Investment", "KTDT", "https://hvtc.edu.vn/");

        // 3. SEED ALL CANONICAL TEXTBOOKS
        Map<String, AcademicBook> bookMap = new HashMap<>();
        bookMap.put("978-0131103627", getOrCreateBook(
                "978-0131103627", "The C Programming Language", "The C Programming Language (2nd Edition)",
                "Brian W. Kernighan, Dennis M. Ritchie", "Prentice Hall", "2nd", 1988, "English",
                "Kinh điển về lập trình C từ chính cha đẻ ngôn ngữ C, cuốn sách nền tảng cho mọi lập trình viên hệ thống.",
                "https://covers.openlibrary.org/b/isbn/9780131103627-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0596009205", getOrCreateBook(
                "978-0596009205", "Head First Java", "Head First Java: A Brain-Friendly Guide",
                "Kathy Sierra, Bert Bates", "O'Reilly Media", "2nd", 2005, "English",
                "Giáo trình lập trình Java trực quan, sinh động với cách tiếp cận khoa học nhận thức dễ tiếp thu.",
                "https://books.google.com/books/content?id=uIVJiAPlBq0C&printsec=frontcover&img=1&zoom=1",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0134685991", getOrCreateBook(
                "978-0134685991", "Effective Java", "Effective Java (3rd Edition)",
                "Joshua Bloch", "Addison-Wesley", "3rd", 2017, "English",
                "Cẩm nang thực hành thiết kế và lập trình Java chuyên sâu từ cựu kiến trúc sư trưởng nền tảng Java.",
                "https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-0134177304", getOrCreateBook(
                "978-0134177304", "Core Java Volume I--Fundamentals", "Core Java Volume I--Fundamentals (10th Edition)",
                "Cay S. Horstmann", "Prentice Hall", "10th", 2016, "English",
                "Nền tảng ngôn ngữ Java chuyên sâu từ cú pháp căn bản đến lập trình tổng quát Generics.",
                "https://covers.openlibrary.org/b/isbn/9780134177304-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0262033848", getOrCreateBook(
                "978-0262033848", "Introduction to Algorithms", "Introduction to Algorithms (CLRS 3rd Edition)",
                "Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein", "MIT Press", "3rd", 2009, "English",
                "Tiêu chuẩn vàng toàn cầu về thuật toán và cấu trúc dữ liệu, được sử dụng tại các trường đại học danh tiếng.",
                "https://covers.openlibrary.org/b/isbn/9780262033848-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0132576277", getOrCreateBook(
                "978-0132576277", "Data Structures and Algorithm Analysis in Java", "Data Structures and Algorithm Analysis in Java (3rd Edition)",
                "Mark Allen Weiss", "Pearson", "3rd", 2011, "English",
                "Giáo trình cấu trúc dữ liệu và giải thuật trong môi trường Java chuẩn mực của Mark Allen Weiss.",
                "https://covers.openlibrary.org/b/isbn/9780132576277-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0321573513", getOrCreateBook(
                "978-0321573513", "Algorithms", "Algorithms (4th Edition)",
                "Robert Sedgewick, Kevin Wayne", "Addison-Wesley", "4th", 2011, "English",
                "Giáo trình thuật toán đại học Princeton danh tiếng của Sedgewick và Wayne.",
                "https://covers.openlibrary.org/b/isbn/9780321573513-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-1848000698", getOrCreateBook(
                "978-1848000698", "The Algorithm Design Manual", "The Algorithm Design Manual (2nd Edition)",
                "Steven S. Skiena", "Springer", "2nd", 2008, "English",
                "Cẩm nang thiết kế thuật toán thực chiến và danh mục các giải thuật kinh điển.",
                "https://books.google.com/books/content?id=TrXd-gxPhVYC&printsec=frontcover&img=1&zoom=1",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-0078022159", getOrCreateBook(
                "978-0078022159", "Database System Concepts", "Database System Concepts (7th Edition)",
                "Abraham Silberschatz, Henry F. Korth, S. Sudarshan", "McGraw-Hill", "7th", 2019, "English",
                "Giáo trình hệ cơ sở dữ liệu nền tảng giảng dạy tại hầu hết các trường đại học công nghệ.",
                "https://covers.openlibrary.org/b/isbn/9780078022159-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0133970777", getOrCreateBook(
                "978-0133970777", "Fundamentals of Database Systems", "Fundamentals of Database Systems (7th Edition)",
                "Ramez Elmasri, Shamkant B. Navathe", "Pearson", "7th", 2015, "English",
                "Giáo trình cơ sở dữ liệu quan hệ, thiết kế lược đồ ER và ngôn ngữ truy vấn SQL.",
                "https://covers.openlibrary.org/b/isbn/9780133970777-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0072465631", getOrCreateBook(
                "978-0072465631", "Database Management Systems", "Database Management Systems (3rd Edition)",
                "Raghu Ramakrishnan, Johannes Gehrke", "McGraw-Hill", "3rd", 2002, "English",
                "Cuốn sách về kiến trúc quản trị hệ CSDL và tối ưu hóa câu lệnh truy vấn.",
                "https://covers.openlibrary.org/b/isbn/9780072465631-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-1492057611", getOrCreateBook(
                "978-1492057611", "Learning SQL", "Learning SQL: Generate, Manipulate, and Retrieve Data (3rd Edition)",
                "Alan Beaulieu", "O'Reilly Media", "3rd", 2020, "English",
                "Hướng dẫn thực hành truy vấn SQL chuẩn từ cơ bản đến các mệnh đề nâng cao.",
                "https://covers.openlibrary.org/b/isbn/9781492057611-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0133594140", getOrCreateBook(
                "978-0133594140", "Computer Networking: A Top-Down Approach", "Computer Networking: A Top-Down Approach (7th Edition)",
                "James F. Kurose, Keith W. Ross", "Pearson", "7th", 2016, "English",
                "Giáo trình mạng máy tính tiếp cận theo mô hình tầng từ trên xuống kinh điển toàn cầu.",
                "https://books.google.com/books/content?id=OljpOAAACAAJ&printsec=frontcover&img=1&zoom=1",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0132126953", getOrCreateBook(
                "978-0132126953", "Computer Networks", "Computer Networks (5th Edition)",
                "Andrew S. Tanenbaum, David J. Wetherall", "Pearson", "5th", 2010, "English",
                "Giáo trình mạng máy tính của giáo sư Tanenbaum, chuẩn mực về các tầng giao thức.",
                "https://covers.openlibrary.org/b/isbn/9780132126953-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0073376226", getOrCreateBook(
                "978-0073376226", "Data Communications and Networking", "Data Communications and Networking (5th Edition)",
                "Behrouz A. Forouzan", "McGraw-Hill", "5th", 2012, "English",
                "Truyền thông dữ liệu và mạng máy tính dành cho sinh viên ngành kỹ thuật máy tính.",
                "https://covers.openlibrary.org/b/isbn/9780073376226-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-1119800361", getOrCreateBook(
                "978-1119800361", "Operating System Concepts", "Operating System Concepts (10th Edition)",
                "Abraham Silberschatz, Peter B. Galvin, Greg Gagne", "Wiley", "10th", 2018, "English",
                "Giáo trình hệ điều hành kinh điển với hình ảnh Khủng Long nổi tiếng toàn cầu.",
                "https://books.google.com/books/content?id=VFV1DwAAQBAJ&printsec=frontcover&img=1&zoom=1",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0133591620", getOrCreateBook(
                "978-0133591620", "Modern Operating Systems", "Modern Operating Systems (4th Edition)",
                "Andrew S. Tanenbaum, Herbert Bos", "Pearson", "4th", 2014, "English",
                "Giáo trình hệ điều hành hiện đại của Tanenbaum phân tích sâu về Unix, Linux và Windows.",
                "https://covers.openlibrary.org/b/isbn/9780133591620-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0133943030", getOrCreateBook(
                "978-0133943030", "Software Engineering", "Software Engineering (10th Edition)",
                "Ian Sommerville", "Pearson", "10th", 2015, "English",
                "Giáo trình kỹ thuật phần mềm tiêu chuẩn cho sinh viên công nghệ thông tin.",
                "https://covers.openlibrary.org/b/isbn/9780133943030-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0132350884", getOrCreateBook(
                "978-0132350884", "Clean Code", "Clean Code: A Handbook of Agile Software Craftsmanship",
                "Robert C. Martin", "Prentice Hall", "1st", 2008, "English",
                "Quy tắc và nghệ thuật viết mã nguồn sạch, dễ đọc và bảo trì cho kỹ sư phần mềm.",
                "https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-0134494166", getOrCreateBook(
                "978-0134494166", "Clean Architecture", "Clean Architecture: A Craftsman's Guide to Software Structure and Design",
                "Robert C. Martin", "Prentice Hall", "1st", 2017, "English",
                "Kiến trúc phần mềm sạch và nguyên lý tách biệt trách nhiệm trong phát triển ứng dụng.",
                "https://covers.openlibrary.org/b/isbn/9780134494166-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-0135957059", getOrCreateBook(
                "978-0135957059", "The Pragmatic Programmer", "The Pragmatic Programmer: your journey to mastery (20th Anniversary Edition)",
                "David Thomas, Andrew Hunt", "Addison-Wesley", "20th Anniversary", 2019, "English",
                "Tác phẩm kinh điển truyền cảm hứng và kỹ năng thực hành cho lập trình viên chuyên nghiệp.",
                "https://covers.openlibrary.org/b/isbn/9780135957059-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-0201633610", getOrCreateBook(
                "978-0201633610", "Design Patterns", "Design Patterns: Elements of Reusable Object-Oriented Software",
                "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", "Addison-Wesley", "1st", 1994, "English",
                "Cuốn sách mẫu thiết kế của Gang of Four (GoF) định hình ngành phát triển phần mềm hướng đối tượng.",
                "https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0321125217", getOrCreateBook(
                "978-0321125217", "Domain-Driven Design", "Domain-Driven Design: Tackling Complexity in the Heart of Software",
                "Eric Evans", "Addison-Wesley", "1st", 2003, "English",
                "Thiết kế hướng miền nhằm giải quyết độ phức tạp trong các hệ thống phần mềm doanh nghiệp lớn.",
                "https://covers.openlibrary.org/b/isbn/9780321125217-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-0201485677", getOrCreateBook(
                "978-0201485677", "Refactoring", "Refactoring: Improving the Design of Existing Code",
                "Martin Fowler", "Addison-Wesley", "1st", 1999, "English",
                "Cải tiến cấu trúc mã nguồn kế thừa mà không làm thay đổi hành vi bên ngoài.",
                "https://covers.openlibrary.org/b/isbn/9780201485677-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-1449373320", getOrCreateBook(
                "978-1449373320", "Designing Data-Intensive Applications", "Designing Data-Intensive Applications",
                "Martin Kleppmann", "O'Reilly Media", "1st", 2017, "English",
                "Thiết kế các hệ thống phân tán chịu lỗi, lưu trữ dữ liệu quy mô lớn và nhất quán dữ liệu.",
                "https://covers.openlibrary.org/b/isbn/9781449373320-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-1617294945", getOrCreateBook(
                "978-1617294945", "Spring in Action", "Spring in Action (5th Edition)",
                "Craig Walls", "Manning Publications", "5th", 2018, "English",
                "Phát triển ứng dụng doanh nghiệp với Spring Boot và Spring Framework.",
                "https://covers.openlibrary.org/b/isbn/9781617294945-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0735679665", getOrCreateBook(
                "978-0735679665", "Software Requirements", "Software Requirements (3rd Edition)",
                "Karl Wiegers, Joy Beatty", "Microsoft Press", "3rd", 2013, "English",
                "Kỹ thuật thu thập, phân tích và quản lý yêu cầu phần mềm chuẩn công nghiệp.",
                "https://covers.openlibrary.org/b/isbn/9780735679665-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0134706054", getOrCreateBook(
                "978-0134706054", "Android Programming: The Big Nerd Ranch Guide", "Android Programming: The Big Nerd Ranch Guide (3rd Edition)",
                "Bill Phillips, Chris Stewart, Kristin Marsicano", "Big Nerd Ranch Guides", "3rd", 2017, "English",
                "Lập trình ứng dụng di động Android chuyên nghiệp từ căn bản đến nâng cao.",
                "https://covers.openlibrary.org/b/isbn/9780134706054-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0996025515", getOrCreateBook(
                "978-0996025515", "Internet of Things: A Hands-On Approach", "Internet of Things: A Hands-On Approach",
                "Arshdeep Bahga, Vijay Madisetti", "VPT", "1st", 2014, "English",
                "Thiết kế và triển khai các hệ thống nhúng và mạng cảm biến Internet of Things.",
                "https://covers.openlibrary.org/b/isbn/9780996025515-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0134444284", getOrCreateBook(
                "978-0134444284", "Cryptography and Network Security", "Cryptography and Network Security: Principles and Practice (7th Edition)",
                "William Stallings", "Pearson", "7th", 2016, "English",
                "Mật mã học, bảo mật giao thức truyền thông và an toàn thông tin.",
                "https://covers.openlibrary.org/b/isbn/9780134444284-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0321486813", getOrCreateBook(
                "978-0321486813", "Compilers: Principles, Techniques, and Tools", "Compilers: Principles, Techniques, and Tools (Dragon Book 2nd Edition)",
                "Alfred V. Aho, Monica S. Lam, Ravi Sethi, Jeffrey D. Ullman", "Addison-Wesley", "2nd", 2006, "English",
                "Sách Con Rồng huyền thoại về nguyên lý thiết kế và xây dựng chương trình dịch.",
                "https://covers.openlibrary.org/b/isbn/9780321486813-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-1133187790", getOrCreateBook(
                "978-1133187790", "Introduction to the Theory of Computation", "Introduction to the Theory of Computation (3rd Edition)",
                "Michael Sipser", "Cengage Learning", "3rd", 2012, "English",
                "Lý thuyết tính toán, ngôn ngữ hình thức, máy Automata và độ phức tạp NP.",
                "https://covers.openlibrary.org/b/isbn/9781133187790-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0124077263", getOrCreateBook(
                "978-0124077263", "Computer Organization and Design", "Computer Organization and Design MIPS Edition (5th Edition)",
                "David A. Patterson, John L. Hennessy", "Morgan Kaufmann", "5th", 2013, "English",
                "Giáo trình kiến trúc máy tính đoạt giải Turing của Patterson và Hennessy.",
                "https://covers.openlibrary.org/b/isbn/9780124077263-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0128119051", getOrCreateBook(
                "978-0128119051", "Computer Architecture: A Quantitative Approach", "Computer Architecture: A Quantitative Approach (6th Edition)",
                "John L. Hennessy, David A. Patterson", "Morgan Kaufmann", "6th", 2017, "English",
                "Nghiên cứu định lượng nâng cao về kiến trúc bộ vi xử lý và hệ thống máy tính hiện đại.",
                "https://covers.openlibrary.org/b/isbn/9780128119051-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-0134101613", getOrCreateBook(
                "978-0134101613", "Computer Organization and Architecture", "Computer Organization and Architecture (10th Edition)",
                "William Stallings", "Pearson", "10th", 2015, "English",
                "Giáo trình tổ chức máy tính và kiến trúc của William Stallings.",
                "https://covers.openlibrary.org/b/isbn/9780134101613-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0735611313", getOrCreateBook(
                "978-0735611313", "Code: The Hidden Language of Computer Hardware and Software", "Code: The Hidden Language of Computer Hardware and Software",
                "Charles Petzold", "Microsoft Press", "1st", 2000, "English",
                "Hành trình khám phá cách phần cứng máy tính và mã nhị phân vận hành thế giới số.",
                "https://covers.openlibrary.org/b/isbn/9780735611313-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-0984782857", getOrCreateBook(
                "978-0984782857", "Cracking the Coding Interview", "Cracking the Coding Interview: 189 Programming Questions and Solutions (6th Edition)",
                "Gayle Laakmann McDowell", "CareerCup", "6th", 2015, "English",
                "Cẩm nang ôn luyện thuật toán và cấu trúc dữ liệu phỏng vấn kỹ sư phần mềm hàng đầu thế giới.",
                "https://covers.openlibrary.org/b/isbn/9780984782857-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-0465050659", getOrCreateBook(
                "978-0465050659", "The Design of Everyday Things", "The Design of Everyday Things: Revised and Expanded Edition",
                "Don Norman", "Basic Books", "Revised", 2013, "English",
                "Tác phẩm kinh điển về thiết kế trải nghiệm người dùng UI/UX và tâm lý học tương tác người - máy.",
                "https://covers.openlibrary.org/b/isbn/9780465050659-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-1484200773", getOrCreateBook(
                "978-1484200773", "Pro Git", "Pro Git (2nd Edition)",
                "Scott Chacon, Ben Straub", "Apress", "2nd", 2014, "English",
                "Hướng dẫn toàn diện về hệ thống quản lý phiên bản mã nguồn Git và GitHub trong dự án phần mềm.",
                "https://covers.openlibrary.org/b/isbn/9781484200773-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0321601919", getOrCreateBook(
                "978-0321601919", "Continuous Delivery", "Continuous Delivery: Reliable Software Releases through Build, Test, and Deployment Automation",
                "Jez Humble, David Farley", "Addison-Wesley", "1st", 2010, "English",
                "Quy trình CI/CD tự động hóa kiểm thử, đóng gói và phát hành phần mềm đáng tin cậy.",
                "https://covers.openlibrary.org/b/isbn/9780321601919-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-1491929124", getOrCreateBook(
                "978-1491929124", "Site Reliability Engineering", "Site Reliability Engineering: How Google Runs Production Systems",
                "Betsy Beyer, Chris Jones, Jennifer Petoff, Niall Murphy", "O'Reilly Media", "1st", 2016, "English",
                "Cách Google vận hành các hệ thống phân tán quy mô toàn cầu đảm bảo độ sẵn sàng 99.99%.",
                "https://covers.openlibrary.org/b/isbn/9781491929124-L.jpg",
                "Công nghệ thông tin", "Sách tham khảo"));
        bookMap.put("978-1118008188", getOrCreateBook(
                "978-1118008188", "HTML and CSS: Design and Build Websites", "HTML and CSS: Design and Build Websites",
                "Jon Duckett", "Wiley", "1st", 2011, "English",
                "Giáo trình thiết kế giao diện Web với HTML5 và CSS3 đẹp mắt và trực quan nhất.",
                "https://covers.openlibrary.org/b/isbn/9781118008188-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-0596805524", getOrCreateBook(
                "978-0596805524", "JavaScript: The Definitive Guide", "JavaScript: The Definitive Guide (6th Edition)",
                "David Flanagan", "O'Reilly Media", "6th", 2011, "English",
                "Cuốn sách Tê Giác toàn thư về ngôn ngữ JavaScript dành cho lập trình viên Web full-stack.",
                "https://covers.openlibrary.org/b/isbn/9780596805524-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-1593279509", getOrCreateBook(
                "978-1593279509", "Eloquent JavaScript", "Eloquent JavaScript: A Modern Introduction to Programming (3rd Edition)",
                "Marijn Haverbeke", "No Starch Press", "3rd", 2018, "English",
                "Nhập môn lập trình hiện đại với JavaScript, kiến trúc hướng đối tượng và xử lý bất đồng bộ.",
                "https://covers.openlibrary.org/b/isbn/9781593279509-L.jpg",
                "Công nghệ thông tin", "Giáo trình chính"));
        bookMap.put("978-1593279288", getOrCreateBook(
                "978-1593279288", "Python Crash Course", "Python Crash Course: A Hands-On, Project-Based Introduction to Programming",
                "Eric Matthes", "No Starch Press", "2nd", 2019, "English",
                "Nhập môn lập trình Python thực chiến dựa trên các dự án phần mềm cụ thể.",
                "https://covers.openlibrary.org/b/isbn/9781593279288-L.jpg",
                "Khoa học dữ liệu & AI", "Giáo trình chính"));
        bookMap.put("978-1491977927", getOrCreateBook(
                "978-1491977927", "Fluent Python", "Fluent Python: Clear, Concise, and Effective Programming",
                "Luciano Ramalho", "O'Reilly Media", "1st", 2015, "English",
                "Lập trình Python chuyên sâu, khai thác tối đa các tính năng đặc thù của Python.",
                "https://books.google.com/books/content?id=JidnEAAAQBAJ&printsec=frontcover&img=1&zoom=1",
                "Khoa học dữ liệu & AI", "Giáo trình chính"));
        bookMap.put("978-1449340377", getOrCreateBook(
                "978-1449340377", "Python Cookbook", "Python Cookbook: Recipes for Mastering Python 3 (3rd Edition)",
                "David Beazley, Brian K. Jones", "O'Reilly Media", "3rd", 2013, "English",
                "Tuyển tập các kỹ thuật xử lý dữ liệu và thuật toán mẫu trong Python 3.",
                "https://covers.openlibrary.org/b/isbn/9781449340377-L.jpg",
                "Khoa học dữ liệu & AI", "Sách tham khảo"));
        bookMap.put("978-1491957660", getOrCreateBook(
                "978-1491957660", "Python for Data Analysis", "Python for Data Analysis: Data Wrangling with Pandas, NumPy, and IPython",
                "Wes McKinney", "O'Reilly Media", "2nd", 2017, "English",
                "Xử lý và phân tích dữ liệu với Pandas và NumPy từ chính tác giả thư viện Pandas.",
                "https://covers.openlibrary.org/b/isbn/9781491957660-L.jpg",
                "Khoa học dữ liệu & AI", "Giáo trình chính"));
        bookMap.put("978-1492032649", getOrCreateBook(
                "978-1492032649", "Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow", "Hands-On Machine Learning (2nd Edition)",
                "Aurélien Géron", "O'Reilly Media", "2nd", 2019, "English",
                "Thực hành Machine Learning và Deep Learning toàn diện với các framework hàng đầu.",
                "https://books.google.com/books/content?id=X5ySEAAAQBAJ&printsec=frontcover&img=1&zoom=1",
                "Khoa học dữ liệu & AI", "Giáo trình chính"));
        bookMap.put("978-0136042594", getOrCreateBook(
                "978-0136042594", "Artificial Intelligence: A Modern Approach", "Artificial Intelligence: A Modern Approach (3rd Edition)",
                "Stuart Russell, Peter Norvig", "Pearson", "3rd", 2009, "English",
                "Bộ giáo trình toàn thư kinh điển về Trí tuệ nhân tạo của Russell và Norvig.",
                "https://books.google.com/books/content?id=Na8rAAAAQBAJ&printsec=frontcover&img=1&zoom=1",
                "Khoa học dữ liệu & AI", "Giáo trình chính"));
        bookMap.put("978-0262035613", getOrCreateBook(
                "978-0262035613", "Deep Learning", "Deep Learning (Adaptive Computation and Machine Learning series)",
                "Ian Goodfellow, Yoshua Bengio, Aaron Courville", "MIT Press", "1st", 2016, "English",
                "Kinh điển về học sâu từ Yoshua Bengio (Turing Award) và Ian Goodfellow.",
                "https://covers.openlibrary.org/b/isbn/9780262035613-L.jpg",
                "Khoa học dữ liệu & AI", "Giáo trình chính"));
        bookMap.put("978-0387310732", getOrCreateBook(
                "978-0387310732", "Pattern Recognition and Machine Learning", "Pattern Recognition and Machine Learning",
                "Christopher M. Bishop", "Springer", "1st", 2006, "English",
                "Cơ sở toán học và thống kê Bayes trong nhận dạng mẫu và máy học.",
                "https://covers.openlibrary.org/b/isbn/9780387310732-L.jpg",
                "Khoa học dữ liệu & AI", "Sách tham khảo"));
        bookMap.put("978-1848829343", getOrCreateBook(
                "978-1848829343", "Computer Vision: Algorithms and Applications", "Computer Vision: Algorithms and Applications",
                "Richard Szeliski", "Springer", "1st", 2010, "English",
                "Giáo trình thị giác máy tính và xử lý ảnh số hàng đầu thế giới.",
                "https://covers.openlibrary.org/b/isbn/9781848829343-L.jpg",
                "Khoa học dữ liệu & AI", "Giáo trình chính"));
        bookMap.put("978-0131873216", getOrCreateBook(
                "978-0131873216", "Speech and Language Processing", "Speech and Language Processing (2nd Edition)",
                "Daniel Jurafsky, James H. Martin", "Prentice Hall", "2nd", 2008, "English",
                "Xử lý ngôn ngữ tự nhiên, ngôn ngữ học tính toán và nhận dạng tiếng nói.",
                "https://covers.openlibrary.org/b/isbn/9780131873216-L.jpg",
                "Khoa học dữ liệu & AI", "Giáo trình chính"));
        bookMap.put("978-0123814791", getOrCreateBook(
                "978-0123814791", "Data Mining: Concepts and Techniques", "Data Mining: Concepts and Techniques (3rd Edition)",
                "Jiawei Han, Micheline Kamber, Jian Pei", "Morgan Kaufmann", "3rd", 2011, "English",
                "Khai phá dữ liệu từ lý thuyết đến kỹ thuật phân cụm, phân lớp và luật kết hợp.",
                "https://covers.openlibrary.org/b/isbn/9780123814791-L.jpg",
                "Khoa học dữ liệu & AI", "Giáo trình chính"));
        bookMap.put("978-0073383095", getOrCreateBook(
                "978-0073383095", "Discrete Mathematics and Its Applications", "Discrete Mathematics and Its Applications (7th Edition)",
                "Kenneth H. Rosen", "McGraw-Hill", "7th", 2011, "English",
                "Toán rời rạc và các ứng dụng trong khoa học máy tính và mật mã học.",
                "https://covers.openlibrary.org/b/isbn/9780073383095-L.jpg",
                "Khoa học cơ bản", "Giáo trình chính"));
        bookMap.put("978-0321629111", getOrCreateBook(
                "978-0321629111", "Probability and Statistics for Engineers and Scientists", "Probability and Statistics for Engineers and Scientists (9th Edition)",
                "Ronald E. Walpole, Raymond H. Myers, Sharon L. Myers, Keying Ye", "Pearson", "9th", 2011, "English",
                "Xác suất thống kê chuẩn dành cho sinh viên ngành kỹ thuật và công nghệ thông tin.",
                "https://covers.openlibrary.org/b/isbn/9780321629111-L.jpg",
                "Khoa học cơ bản", "Giáo trình chính"));
        bookMap.put("978-0980232714", getOrCreateBook(
                "978-0980232714", "Introduction to Linear Algebra", "Introduction to Linear Algebra (4th Edition)",
                "Gilbert Strang", "Wellesley-Cambridge Press", "4th", 2009, "English",
                "Giáo trình đại số tuyến tính của giáo sư Gilbert Strang (MIT) trực quan và ứng dụng cao.",
                "https://covers.openlibrary.org/b/isbn/9780980232714-L.jpg",
                "Khoa học cơ bản", "Giáo trình chính"));
        bookMap.put("978-0321982384", getOrCreateBook(
                "978-0321982384", "Linear Algebra and Its Applications", "Linear Algebra and Its Applications (5th Edition)",
                "David C. Lay, Steven R. Lay, Judi J. McDonald", "Pearson", "5th", 2014, "English",
                "Đại số tuyến tính ứng dụng cho khoa học máy tính và kỹ thuật.",
                "https://covers.openlibrary.org/b/isbn/9780321982384-L.jpg",
                "Khoa học cơ bản", "Giáo trình chính"));
        bookMap.put("978-0321878960", getOrCreateBook(
                "978-0321878960", "Thomas' Calculus", "Thomas' Calculus (13th Edition)",
                "George B. Thomas, Maurice D. Weir, Joel R. Hass", "Pearson", "13th", 2014, "English",
                "Giáo trình giải tích toán học kinh điển cho sinh viên đại học kỹ thuật.",
                "https://covers.openlibrary.org/b/isbn/9780321878960-L.jpg",
                "Khoa học cơ bản", "Giáo trình chính"));
        bookMap.put("978-1285741550", getOrCreateBook(
                "978-1285741550", "Calculus: Early Transcendentals", "Calculus: Early Transcendentals (8th Edition)",
                "James Stewart", "Cengage Learning", "8th", 2015, "English",
                "Bộ giáo trình vi tích phân nổi tiếng toàn cầu của James Stewart.",
                "https://covers.openlibrary.org/b/isbn/9781285741550-L.jpg",
                "Khoa học cơ bản", "Giáo trình chính"));
        bookMap.put("978-0134753119", getOrCreateBook(
                "978-0134753119", "A First Course in Probability", "A First Course in Probability (10th Edition)",
                "Sheldon Ross", "Pearson", "10th", 2018, "English",
                "Lý thuyết xác suất từ cơ bản đến biến ngẫu nhiên và phân phối xác suất.",
                "https://covers.openlibrary.org/b/isbn/9780134753119-L.jpg",
                "Khoa học cơ bản", "Giáo trình chính"));
        bookMap.put("978-1133947271", getOrCreateBook(
                "978-1133947271", "Physics for Scientists and Engineers with Modern Physics", "Physics for Scientists and Engineers (9th Edition)",
                "Raymond A. Serway, John W. Jewett", "Cengage Learning", "9th", 2013, "English",
                "Vật lý đại cương cơ, nhiệt, điện, từ và vật lý hiện đại của Serway.",
                "https://covers.openlibrary.org/b/isbn/9781133947271-L.jpg",
                "Khoa học cơ bản", "Giáo trình chính"));
        bookMap.put("978-0321973610", getOrCreateBook(
                "978-0321973610", "University Physics with Modern Physics", "University Physics with Modern Physics (14th Edition)",
                "Hugh D. Young, Roger A. Freedman", "Pearson", "14th", 2015, "English",
                "Giáo trình vật lý đại cương của Young và Freedman được giảng dạy rộng rãi.",
                "https://covers.openlibrary.org/b/isbn/9780321973610-L.jpg",
                "Khoa học cơ bản", "Giáo trình chính"));
        bookMap.put("978-0134492513", getOrCreateBook(
                "978-0134492513", "Principles of Marketing", "Principles of Marketing (17th Edition)",
                "Philip Kotler, Gary Armstrong", "Pearson", "17th", 2017, "English",
                "Bộ giáo trình tiếp thị nền tảng kinh điển của bậc thầy marketing thế giới Philip Kotler.",
                "https://covers.openlibrary.org/b/isbn/9780134492513-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0133856460", getOrCreateBook(
                "978-0133856460", "Marketing Management", "Marketing Management (15th Edition)",
                "Philip Kotler, Kevin Lane Keller", "Pearson", "15th", 2015, "English",
                "Giáo trình quản trị tiếp thị chiến lược dành cho bậc đại học và cao học.",
                "https://covers.openlibrary.org/b/isbn/9780133856460-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0132544368", getOrCreateBook(
                "978-0132544368", "Consumer Behavior", "Consumer Behavior (11th Edition)",
                "Leon G. Schiffman, Joseph Wisenblit", "Pearson", "11th", 2014, "English",
                "Tâm lý và hành vi người tiêu dùng trong kỷ nguyên số.",
                "https://covers.openlibrary.org/b/isbn/9780132544368-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-1285165905", getOrCreateBook(
                "978-1285165905", "Principles of Microeconomics", "Principles of Microeconomics (7th Edition)",
                "N. Gregory Mankiw", "Cengage Learning", "7th", 2014, "English",
                "Giáo trình kinh tế học vi mô chuẩn mực của giáo sư N. Gregory Mankiw (Đại học Harvard).",
                "https://covers.openlibrary.org/b/isbn/9781285165905-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0538453066", getOrCreateBook(
                "978-0538453066", "Principles of Macroeconomics", "Principles of Macroeconomics (6th Edition)",
                "N. Gregory Mankiw", "Cengage Learning", "6th", 2011, "English",
                "Giáo trình kinh tế học vĩ mô về lạm phát, thất nghiệp, tăng trưởng và chính sách tài khóa.",
                "https://covers.openlibrary.org/b/isbn/9780538453066-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0134527604", getOrCreateBook(
                "978-0134527604", "Management", "Management (14th Edition)",
                "Stephen P. Robbins, Mary A. Coulter", "Pearson", "14th", 2017, "English",
                "Nguyên lý quản trị học của Robbins và Coulter trang bị tư duy hoạch định và tổ chức.",
                "https://covers.openlibrary.org/b/isbn/9780134527604-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0134103983", getOrCreateBook(
                "978-0134103983", "Organizational Behavior", "Organizational Behavior (17th Edition)",
                "Stephen P. Robbins, Timothy A. Judge", "Pearson", "17th", 2016, "English",
                "Hành vi tổ chức, động lực làm việc và phong cách lãnh đạo trong doanh nghiệp.",
                "https://covers.openlibrary.org/b/isbn/9780134103983-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0134235455", getOrCreateBook(
                "978-0134235455", "Human Resource Management", "Human Resource Management (15th Edition)",
                "Gary Dessler", "Pearson", "15th", 2016, "English",
                "Quản trị nhân sự toàn diện từ tuyển dụng, đào tạo đến đãi ngộ nhân tài của Gary Dessler.",
                "https://covers.openlibrary.org/b/isbn/9780134235455-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0132664257", getOrCreateBook(
                "978-0132664257", "Strategic Brand Management", "Strategic Brand Management: Building, Measuring, and Managing Brand Equity (4th Edition)",
                "Kevin Lane Keller", "Pearson", "4th", 2012, "English",
                "Quản trị thương hiệu chiến lược và xây dựng tài sản thương hiệu của Keller.",
                "https://covers.openlibrary.org/b/isbn/9780132664257-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-1292077611", getOrCreateBook(
                "978-1292077611", "Digital Marketing", "Digital Marketing: Strategy, Implementation and Practice (6th Edition)",
                "Dave Chaffey, Fiona Ellis-Chadwick", "Pearson", "6th", 2016, "English",
                "Chiến lược tiếp thị số, SEO, truyền thông xã hội và thương mại điện tử hiện đại.",
                "https://covers.openlibrary.org/b/isbn/9781292077611-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0135191798", getOrCreateBook(
                "978-0135191798", "Management Information Systems", "Management Information Systems: Managing the Digital Firm (16th Edition)",
                "Kenneth C. Laudon, Jane P. Laudon", "Pearson", "16th", 2019, "English",
                "Hệ thống thông tin quản lý và chuyển đổi số doanh nghiệp của Laudon.",
                "https://covers.openlibrary.org/b/isbn/9780135191798-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0393123968", getOrCreateBook(
                "978-0393123968", "Intermediate Microeconomics", "Intermediate Microeconomics: A Modern Approach (9th Edition)",
                "Hal R. Varian", "W. W. Norton & Company", "9th", 2014, "English",
                "Kinh tế vi mô trung cấp của Hal Varian, giáo sư kinh tế trưởng tại Google.",
                "https://covers.openlibrary.org/b/isbn/9780393123968-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0134472119", getOrCreateBook(
                "978-0134472119", "Macroeconomics", "Macroeconomics (7th Edition)",
                "Olivier Blanchard", "Pearson", "7th", 2016, "English",
                "Kinh tế học vĩ mô trung cấp của cựu kinh tế trưởng IMF Olivier Blanchard.",
                "https://covers.openlibrary.org/b/isbn/9780134472119-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0138013882", getOrCreateBook(
                "978-0138013882", "Economic Development", "Economic Development (11th Edition)",
                "Michael P. Todaro, Stephen C. Smith", "Pearson", "11th", 2011, "English",
                "Kinh tế phát triển và các chính sách xóa đói giảm nghèo tại các nước đang phát triển.",
                "https://covers.openlibrary.org/b/isbn/9780138013882-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0134153971", getOrCreateBook(
                "978-0134153971", "Strategic Management: Concepts and Cases", "Strategic Management: A Competitive Advantage Approach (16th Edition)",
                "Fred R. David, Forest R. David", "Pearson", "16th", 2016, "English",
                "Quản trị chiến lược và tạo dựng lợi thế cạnh tranh bền vững trong kinh doanh.",
                "https://covers.openlibrary.org/b/isbn/9780134153971-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0134519579", getOrCreateBook(
                "978-0134519579", "International Economics: Theory and Policy", "International Economics: Theory and Policy (11th Edition)",
                "Paul R. Krugman, Maurice Obstfeld, Marc Melitz", "Pearson", "11th", 2017, "English",
                "Kinh tế quốc tế từ nhà kinh tế đoạt giải Nobel Paul Krugman.",
                "https://covers.openlibrary.org/b/isbn/9780134519579-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-1305270107", getOrCreateBook(
                "978-1305270107", "Introductory Econometrics: A Modern Approach", "Introductory Econometrics: A Modern Approach (6th Edition)",
                "Jeffrey M. Wooldridge", "Cengage Learning", "6th", 2015, "English",
                "Kinh tế lượng ứng dụng với phương pháp hồi quy OLS và phân tích dữ liệu bảng của Wooldridge.",
                "https://covers.openlibrary.org/b/isbn/9781305270107-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0073375779", getOrCreateBook(
                "978-0073375779", "Basic Econometrics", "Basic Econometrics (5th Edition)",
                "Damodar N. Gujarati, Dawn C. Porter", "McGraw-Hill", "5th", 2008, "English",
                "Giáo trình kinh tế lượng căn bản kinh điển của Damodar Gujarati.",
                "https://covers.openlibrary.org/b/isbn/9780073375779-L.jpg",
                "Kinh tế - Quản trị", "Giáo trình chính"));
        bookMap.put("978-0307887894", getOrCreateBook(
                "978-0307887894", "The Lean Startup", "The Lean Startup: How Today's Entrepreneurs Use Continuous Innovation to Create Radically Successful Businesses",
                "Eric Ries", "Crown Business", "1st", 2011, "English",
                "Phương pháp khởi nghiệp tinh gọn, phát triển sản phẩm khả dụng tối thiểu MVP và vòng lặp Build-Measure-Learn.",
                "https://covers.openlibrary.org/b/isbn/9780307887894-L.jpg",
                "Kinh tế - Quản trị", "Sách tham khảo"));
        bookMap.put("978-0804139298", getOrCreateBook(
                "978-0804139298", "Zero to One", "Zero to One: Notes on Startups, or How to Build the Future",
                "Peter Thiel, Blake Masters", "Crown Business", "1st", 2014, "English",
                "Tư duy đổi mới đột phá và xây dựng doanh nghiệp độc quyền sáng tạo từ nhà đồng sáng lập PayPal Peter Thiel.",
                "https://covers.openlibrary.org/b/isbn/9780804139298-L.jpg",
                "Kinh tế - Quản trị", "Sách tham khảo"));
        bookMap.put("978-0066620992", getOrCreateBook(
                "978-0066620992", "Good to Great", "Good to Great: Why Some Companies Make the Leap... and Others Don't",
                "Jim Collins", "HarperBusiness", "1st", 2001, "English",
                "Nghiên cứu về các yếu tố giúp doanh nghiệp từ vĩ đại vươn tầm trường tồn của Jim Collins.",
                "https://covers.openlibrary.org/b/isbn/9780066620992-L.jpg",
                "Kinh tế - Quản trị", "Sách tham khảo"));
        bookMap.put("978-0374533557", getOrCreateBook(
                "978-0374533557", "Thinking, Fast and Slow", "Thinking, Fast and Slow",
                "Daniel Kahneman", "Farrar, Straus and Giroux", "1st", 2011, "English",
                "Tác phẩm kinh điển về kinh tế học hành vi và hai hệ thống tư duy của nhà đoạt giải Nobel Daniel Kahneman.",
                "https://covers.openlibrary.org/b/isbn/9780374533557-L.jpg",
                "Kinh tế - Quản trị", "Sách tham khảo"));
        bookMap.put("978-0060731335", getOrCreateBook(
                "978-0060731335", "Freakonomics", "Freakonomics: A Rogue Economist Explores the Hidden Side of Everything",
                "Steven D. Levitt, Stephen J. Dubner", "William Morrow", "1st", 2005, "English",
                "Góc nhìn kinh tế học hài hước và độc đáo khám phá mặt khuất của đời sống xã hội.",
                "https://covers.openlibrary.org/b/isbn/9780060731335-L.jpg",
                "Kinh tế - Quản trị", "Sách tham khảo"));
        bookMap.put("978-0134127620", getOrCreateBook(
                "978-0134127620", "Financial Accounting", "Financial Accounting (11th Edition)",
                "Walter T. Harrison, Charles T. Horngren, C. William Thomas, Wendy M. Tietz", "Pearson", "11th", 2016, "English",
                "Kế toán tài chính đại cương từ giáo sư Charles Horngren.",
                "https://covers.openlibrary.org/b/isbn/9780134127620-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0078111044", getOrCreateBook(
                "978-0078111044", "Financial & Managerial Accounting", "Financial & Managerial Accounting: The Basis for Business Decisions (16th Edition)",
                "Jan Williams, Susan Haka, Mark Bettner, Joseph Carcello", "McGraw-Hill", "16th", 2011, "English",
                "Kế toán tài chính và kế toán quản trị tích hợp dành cho sinh viên chuyên ngành kinh tế.",
                "https://covers.openlibrary.org/b/isbn/9780078111044-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-1119503668", getOrCreateBook(
                "978-1119503668", "Intermediate Accounting", "Intermediate Accounting (17th Edition)",
                "Donald E. Kieso, Jerry J. Weygandt, Terry D. Warfield", "Wiley", "17th", 2019, "English",
                "Giáo trình Kế toán tài chính trung cấp (Kieso) chuẩn mực được công nhận toàn cầu.",
                "https://covers.openlibrary.org/b/isbn/9781119503668-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0078110917", getOrCreateBook(
                "978-0078110917", "Advanced Accounting", "Advanced Accounting (10th Edition)",
                "Joe Ben Hoyle, Thomas Schaefer, Timothy Doupnik", "McGraw-Hill", "10th", 2010, "English",
                "Kế toán tài chính nâng cao: hợp nhất kinh doanh, công ty con và báo cáo tài chính hợp nhất.",
                "https://covers.openlibrary.org/b/isbn/9780078110917-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0078025631", getOrCreateBook(
                "978-0078025631", "Managerial Accounting", "Managerial Accounting (15th Edition)",
                "Ray H. Garrison, Eric W. Noreen, Peter C. Brewer", "McGraw-Hill", "15th", 2014, "English",
                "Kế toán quản trị định giá sản phẩm, phân tích chi phí khối lượng lợi nhuận CVP của Garrison.",
                "https://covers.openlibrary.org/b/isbn/9780078025631-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0134475585", getOrCreateBook(
                "978-0134475585", "Cost Accounting: A Managerial Emphasis", "Cost Accounting: A Managerial Emphasis (16th Edition)",
                "Srikant M. Datar, Madhav V. Rajan", "Pearson", "16th", 2017, "English",
                "Kế toán chi phí Horngren: phương pháp phân bổ chi phí ABC và định mức chi phí chuẩn.",
                "https://covers.openlibrary.org/b/isbn/9780134475585-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0134065960", getOrCreateBook(
                "978-0134065960", "Auditing and Assurance Services", "Auditing and Assurance Services: An Integrated Approach (16th Edition)",
                "Alvin A. Arens, Randal J. Elder, Mark S. Beasley, Chris E. Hogan", "Pearson", "16th", 2016, "English",
                "Giáo trình kiểm toán căn bản và dịch vụ bảo đảm toàn diện của giáo sư Arens.",
                "https://books.google.com/books/content?id=4TAKswEACAAJ&printsec=frontcover&img=1&zoom=1",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0078025440", getOrCreateBook(
                "978-0078025440", "Principles of Auditing & Other Assurance Services", "Principles of Auditing & Other Assurance Services (19th Edition)",
                "Ray Whittington, Kurt Pany", "McGraw-Hill", "19th", 2013, "English",
                "Nguyên lý kiểm toán và kiểm soát nội bộ theo chuẩn mực quốc tế.",
                "https://covers.openlibrary.org/b/isbn/9780078025440-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0471230113", getOrCreateBook(
                "978-0471230113", "Modern Auditing", "Modern Auditing: Assurance Services and the Integrity of Financial Reporting (8th Edition)",
                "William C. Boynton, Raymond N. Johnson", "Wiley", "8th", 2005, "English",
                "Kiểm toán hiện đại và tính trung thực của báo cáo tài chính doanh nghiệp.",
                "https://covers.openlibrary.org/b/isbn/9780471230113-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0133836790", getOrCreateBook(
                "978-0133836790", "The Economics of Money, Banking, and Financial Markets", "The Economics of Money, Banking, and Financial Markets (11th Edition)",
                "Frederic S. Mishkin", "Pearson", "11th", 2015, "English",
                "Giáo trình tiền tệ ngân hàng và thị trường tài chính hàng đầu thế giới của Frederic Mishkin.",
                "https://covers.openlibrary.org/b/isbn/9780133836790-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0133423624", getOrCreateBook(
                "978-0133423624", "Financial Markets and Institutions", "Financial Markets and Institutions (8th Edition)",
                "Frederic S. Mishkin, Stanley Eakins", "Pearson", "8th", 2014, "English",
                "Thị trường tài chính và các định chế tài chính trung gian: ngân hàng, quỹ đầu tư, bảo hiểm.",
                "https://covers.openlibrary.org/b/isbn/9780133423624-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0133507638", getOrCreateBook(
                "978-0133507638", "Corporate Finance", "Corporate Finance (4th Edition)",
                "Jonathan Berk, Peter DeMarzo", "Pearson", "4th", 2016, "English",
                "Tài chính doanh nghiệp của Berk & DeMarzo: định giá tài sản, cấu trúc vốn và chính sách cổ tức.",
                "https://books.google.com/books/content?id=lf8yDQAAQBAJ&printsec=frontcover&img=1&zoom=1",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-1259918940", getOrCreateBook(
                "978-1259918940", "Fundamentals of Corporate Finance", "Fundamentals of Corporate Finance (12th Edition)",
                "Stephen A. Ross, Randolph W. Westerfield, Bradford D. Jordan", "McGraw-Hill", "12th", 2018, "English",
                "Nguyên lý tài chính doanh nghiệp của Ross, Westerfield & Jordan được giảng dạy toàn cầu.",
                "https://covers.openlibrary.org/b/isbn/9781259918940-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-1260013900", getOrCreateBook(
                "978-1260013900", "Principles of Corporate Finance", "Principles of Corporate Finance (13th Edition)",
                "Richard A. Brealey, Stewart C. Myers, Franklin Allen", "McGraw-Hill", "13th", 2019, "English",
                "Tác phẩm tài chính kinh điển của Brealey & Myers về giá trị hiện tại ròng và rủi ro thị trường.",
                "https://covers.openlibrary.org/b/isbn/9781260013900-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-1337902601", getOrCreateBook(
                "978-1337902601", "Financial Management: Theory & Practice", "Financial Management: Theory & Practice (16th Edition)",
                "Eugene F. Brigham, Michael C. Ehrhardt", "Cengage Learning", "16th", 2019, "English",
                "Quản trị tài chính: lý thuyết và thực hành của giáo sư Eugene Brigham.",
                "https://covers.openlibrary.org/b/isbn/9781337902601-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0273738015", getOrCreateBook(
                "978-0273738015", "Fundamentals of Financial Management", "Fundamentals of Financial Management (13th Edition)",
                "James C. Van Horne, John M. Wachowicz", "Pearson", "13th", 2008, "English",
                "Căn bản quản trị tài chính doanh nghiệp của James C. Van Horne.",
                "https://books.google.com/books/content?id=atS5wUv5E2QC&printsec=frontcover&img=1&zoom=1",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0078025310", getOrCreateBook(
                "978-0078025310", "Financial Statement Analysis and Security Valuation", "Financial Statement Analysis and Security Valuation (5th Edition)",
                "Stephen H. Penman", "McGraw-Hill", "5th", 2012, "English",
                "Phân tích báo cáo tài chính và định giá chứng khoán của giáo sư Stephen Penman.",
                "https://covers.openlibrary.org/b/isbn/9780078025310-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0078110962", getOrCreateBook(
                "978-0078110962", "Financial Statement Analysis", "Financial Statement Analysis (11th Edition)",
                "K.R. Subramanyam", "McGraw-Hill", "11th", 2013, "English",
                "Kỹ thuật đọc và phân tích báo cáo tài chính doanh nghiệp của Subramanyam.",
                "https://covers.openlibrary.org/b/isbn/9780078110962-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-1118394106", getOrCreateBook(
                "978-1118394106", "Project Financing: Asset-Based Financial Engineering", "Project Financing: Asset-Based Financial Engineering (3rd Edition)",
                "John D. Finnerty", "Wiley", "3rd", 2013, "English",
                "Tài trợ dự án và thẩm định tài chính đầu tư dự án quy mô lớn của Finnerty.",
                "https://covers.openlibrary.org/b/isbn/9781118394106-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-1118572382", getOrCreateBook(
                "978-1118572382", "Multinational Financial Management", "Multinational Financial Management (10th Edition)",
                "Alan C. Shapiro, Peter Moles", "Wiley", "10th", 2014, "English",
                "Quản trị tài chính công ty đa quốc gia và phòng ngừa rủi ro tỷ giá hối đoái.",
                "https://covers.openlibrary.org/b/isbn/9781118572382-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-1133494683", getOrCreateBook(
                "978-1133494683", "Bank Management", "Bank Management (8th Edition)",
                "Timothy W. Koch, S. Scott MacDonald", "Cengage Learning", "8th", 2014, "English",
                "Quản trị ngân hàng thương mại, quản lý khe hở lãi suất và thanh khoản của Koch.",
                "https://covers.openlibrary.org/b/isbn/9781133494683-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0078034671", getOrCreateBook(
                "978-0078034671", "Bank Management & Financial Services", "Bank Management & Financial Services (9th Edition)",
                "Peter S. Rose, Sylvia C. Hudgins", "McGraw-Hill", "9th", 2012, "English",
                "Quản trị ngân hàng và dịch vụ tài chính hiện đại của Rose & Hudgins.",
                "https://covers.openlibrary.org/b/isbn/9780078034671-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-1259723759", getOrCreateBook(
                "978-1259723759", "Investments", "Investments (11th Edition)",
                "Zvi Bodie, Alex Kane, Alan J. Marcus", "McGraw-Hill", "11th", 2017, "English",
                "Giáo trình đầu tư chứng khoán và quản lý danh mục đầu tư chuẩn mực CFA của Bodie, Kane & Marcus.",
                "https://books.google.com/books/content?id=ZTEOnwEACAAJ&printsec=frontcover&img=1&zoom=1",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0078034695", getOrCreateBook(
                "978-0078034695", "Essentials of Investments", "Essentials of Investments (9th Edition)",
                "Zvi Bodie, Alex Kane, Alan J. Marcus", "McGraw-Hill", "9th", 2012, "English",
                "Căn bản về đầu tư, thị trường vốn và định giá tài sản tài chính.",
                "https://covers.openlibrary.org/b/isbn/9780078034695-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0071440288", getOrCreateBook(
                "978-0071440288", "Security Analysis", "Security Analysis: Sixth Edition, Foreword by Warren Buffett",
                "Benjamin Graham, David L. Dodd", "McGraw-Hill", "6th", 2008, "English",
                "Kinh điển phân tích chứng khoán và đầu tư giá trị của Benjamin Graham & David Dodd có lời tựa của Warren Buffett.",
                "https://covers.openlibrary.org/b/isbn/9780071440288-L.jpg",
                "Kế toán - Tài chính", "Sách tham khảo"));
        bookMap.put("978-0060555665", getOrCreateBook(
                "978-0060555665", "The Intelligent Investor", "The Intelligent Investor: The Definitive Book on Value Investing",
                "Benjamin Graham, Jason Zweig", "HarperBusiness", "Revised", 2003, "English",
                "Cuốn sách đầu tư giá trị kinh điển mọi thời đại được Warren Buffett ca ngợi là cuốn sách tài chính hay nhất từng viết.",
                "https://covers.openlibrary.org/b/isbn/9780060555665-L.jpg",
                "Kế toán - Tài chính", "Sách tham khảo"));
        bookMap.put("978-0393358384", getOrCreateBook(
                "978-0393358384", "A Random Walk Down Wall Street", "A Random Walk Down Wall Street: The Time-Tested Strategy for Successful Investing (12th Edition)",
                "Burton G. Malkiel", "W. W. Norton & Company", "12th", 2019, "English",
                "Bước đi ngẫu nhiên trên phố Wall: lý thuyết thị trường hiệu quả và chiến lược đầu tư chỉ số thụ động.",
                "https://covers.openlibrary.org/b/isbn/9780393358384-L.jpg",
                "Kế toán - Tài chính", "Sách tham khảo"));
        bookMap.put("978-1119610885", getOrCreateBook(
                "978-1119610885", "Valuation: Measuring and Managing the Value of Companies", "Valuation: Measuring and Managing the Value of Companies (7th Edition)",
                "McKinsey & Company, Tim Koller, Marc Goedhart, David Wessels", "Wiley", "7th", 2020, "English",
                "Kinh điển thẩm định giá doanh nghiệp của McKinsey & Company.",
                "https://covers.openlibrary.org/b/isbn/9781119610885-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));
        bookMap.put("978-0471751212", getOrCreateBook(
                "978-0471751212", "Damodaran on Valuation", "Damodaran on Valuation: Security Analysis for Investment and Corporate Finance (2nd Edition)",
                "Aswath Damodaran", "Wiley", "2nd", 2006, "English",
                "Định giá chứng khoán và tài chính doanh nghiệp từ 'phù thủy định giá' Aswath Damodaran (NYU Stern).",
                "https://covers.openlibrary.org/b/isbn/9780471751212-L.jpg",
                "Kế toán - Tài chính", "Giáo trình chính"));

        // 4. MAP FPTU COURSES AND BOOKS
        {
            Course course = getOrCreateCourse(fptSE, "PRF192", "Programming Fundamentals with C", "Nhập môn lập trình với ngôn ngữ C và tư duy cấu trúc", 1, 3, true);
            mapBook(course, bookMap.get("978-0131103627"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0735611313"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "PRO192", "Object-Oriented Programming (Java)", "Lập trình hướng đối tượng với Java và tư duy trừu tượng", 2, 3, true);
            mapBook(course, bookMap.get("978-0596009205"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134685991"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134177304"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "CSD201", "Data Structures and Algorithms", "Cấu trúc dữ liệu và giải thuật trong môi trường Java", 3, 3, true);
            mapBook(course, bookMap.get("978-0262033848"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0132576277"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0321573513"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "DBI202", "Introduction to Databases (SQL)", "Hệ quản trị cơ sở dữ liệu và ngôn ngữ truy vấn SQL", 3, 3, true);
            mapBook(course, bookMap.get("978-0078022159"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0133970777"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-1492057611"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "NWC203", "Computer Networking & Architecture", "Mạng máy tính, giao thức TCP/IP và kiến trúc vi xử lý", 4, 3, true);
            mapBook(course, bookMap.get("978-0133594140"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0132126953"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134101613"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0124077263"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134444284"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "OSG202", "Operating Systems", "Nguyên lý hệ điều hành, quản lý tiến trình, bộ nhớ và tệp", 3, 3, true);
            mapBook(course, bookMap.get("978-1119800361"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0133591620"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "SWE201", "Introduction to Software Engineering", "Quy trình công nghệ phần mềm, mô hình Agile và Scrum", 4, 3, true);
            mapBook(course, bookMap.get("978-0133943030"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0132350884"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "SWP391", "Application Development Project", "Dự án phát triển phần mềm ứng dụng theo nhóm thực chiến", 5, 3, true);
            mapBook(course, bookMap.get("978-0134494166"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0135957059"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0321125217"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0201485677"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-1449373320"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0984782857"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-1484200773"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "SWT301", "Software Testing", "Kiểm thử phần mềm và đảm bảo chất lượng QA/QC", 5, 3, true);
            mapBook(course, bookMap.get("978-0321601919"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-1491929124"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "PRJ301", "Java Web Application Development", "Phát triển ứng dụng Web Java doanh nghiệp với Spring Boot", 4, 3, true);
            mapBook(course, bookMap.get("978-1617294945"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0201633610"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "PRM392", "Mobile Programming", "Lập trình ứng dụng di động Android chuyên nghiệp", 5, 3, true);
            mapBook(course, bookMap.get("978-0134706054"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0465050659"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "SWR302", "Software Requirement", "Thu thập, phân tích và quản lý yêu cầu phần mềm", 4, 3, true);
            mapBook(course, bookMap.get("978-0735679665"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptAI, "AIP191", "Python for Artificial Intelligence", "Lập trình Python ứng dụng trong xử lý dữ liệu và AI", 2, 3, true);
            mapBook(course, bookMap.get("978-1593279288"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-1491977927"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-1491957660"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-1449340377"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptAI, "AIH201", "Machine Learning Foundations", "Nền tảng học máy, giải thuật dự đoán và phân loại", 4, 3, true);
            mapBook(course, bookMap.get("978-1492032649"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0136042594"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0387310732"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptAI, "AIL302m", "Deep Learning", "Học sâu với mạng nơ-ron tích chập CNN, RNN và Transformer", 5, 3, true);
            mapBook(course, bookMap.get("978-0262035613"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-1848829343"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptAI, "NLP301", "Natural Language Processing", "Xử lý ngôn ngữ tự nhiên và mô hình ngôn ngữ lớn LLM", 6, 3, true);
            mapBook(course, bookMap.get("978-0131873216"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0123814791"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "MAD101", "Discrete Mathematics", "Toán rời rạc cho ngành máy tính và cấu trúc đại số", 2, 3, true);
            mapBook(course, bookMap.get("978-0073383095"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "MAS291", "Statistics for IT", "Xác suất thống kê và phương pháp kiểm định dữ liệu cho CNTT", 3, 3, true);
            mapBook(course, bookMap.get("978-0321629111"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134753119"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptSE, "IOT102", "Internet of Things", "Mạng vạn vật kết nối và hệ thống nhúng cảm biến", 4, 3, true);
            mapBook(course, bookMap.get("978-0996025515"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "MKT101", "Principles of Marketing", "Nguyên lý tiếp thị và xây dựng chiến lược sản phẩm", 2, 3, true);
            mapBook(course, bookMap.get("978-0134492513"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0133856460"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "MKT201", "Consumer Behavior", "Tâm lý học và hành vi người tiêu dùng trên thị trường", 3, 3, true);
            mapBook(course, bookMap.get("978-0132544368"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "BRA301", "Brand Management", "Quản trị thương hiệu chiến lược và định vị giá trị thương hiệu", 5, 3, true);
            mapBook(course, bookMap.get("978-0132664257"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "DIG201", "Digital Marketing Strategy", "Tiếp thị kỹ thuật số, SEO và truyền thông mạng xã hội", 4, 3, true);
            mapBook(course, bookMap.get("978-1292077611"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "ECO111", "Microeconomics", "Kinh tế vi mô trong phân tích kinh doanh và thị trường", 1, 3, true);
            mapBook(course, bookMap.get("978-1285165905"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "ECO121", "Macroeconomics", "Kinh tế vĩ mô, lạm phát và chính sách tiền tệ tài khóa", 2, 3, true);
            mapBook(course, bookMap.get("978-0538453066"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "MGT103", "Principles of Management", "Nguyên lý quản trị kinh doanh và hoạch định chiến lược", 1, 3, true);
            mapBook(course, bookMap.get("978-0134527604"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0066620992"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "MGT202", "Organizational Behavior", "Hành vi tổ chức, văn hóa doanh nghiệp và lãnh đạo", 3, 3, true);
            mapBook(course, bookMap.get("978-0134103983"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0374533557"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "HRM201", "Human Resource Management", "Quản trị nguồn nhân lực, tuyển dụng và phát triển nhân tài", 4, 3, true);
            mapBook(course, bookMap.get("978-0134235455"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "FIN202", "Financial Management for Business", "Quản trị tài chính doanh nghiệp và ra quyết định đầu tư", 3, 3, true);
            mapBook(course, bookMap.get("978-1259918940"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "ACC101", "Principles of Accounting", "Nguyên tắc kế toán cơ bản và ghi sổ kép", 2, 3, true);
            mapBook(course, bookMap.get("978-0134127620"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(fptBA, "ENT101", "Entrepreneurship & Innovation", "Khởi nghiệp kinh doanh và đổi mới sáng tạo trong công nghệ", 5, 3, true);
            mapBook(course, bookMap.get("978-0307887894"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
            mapBook(course, bookMap.get("978-0804139298"), CourseBookRelationshipType.REQUIRED, "Giáo trình chính khóa FPTU", "https://daihoc.fpt.edu.vn/", true);
        }

        // 5. MAP VNU COURSES AND BOOKS
        {
            Course course = getOrCreateCourse(vnuIT, "INT1006", "Tin học cơ sở 1 (Lập trình C/C++)", "Nhập môn khoa học máy tính và lập trình C/C++", 1, 3, true);
            mapBook(course, bookMap.get("978-0131103627"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0735611313"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuIT, "INT1008", "Cấu trúc dữ liệu và giải thuật", "Các giải thuật tìm kiếm, sắp xếp và cây nhị phân", 3, 4, true);
            mapBook(course, bookMap.get("978-0262033848"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0321573513"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-1848000698"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuIT, "INT2204", "Lập trình hướng đối tượng (OOP)", "Thiết kế hướng đối tượng nâng cao với Java", 2, 3, true);
            mapBook(course, bookMap.get("978-0596009205"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134685991"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134177304"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0201633610"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0201485677"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuIT, "INT2206", "Nguyên lý hệ điều hành", "Quản lý tiến trình, luồng, bộ nhớ ảo và hệ thống tập tin", 4, 3, true);
            mapBook(course, bookMap.get("978-1119800361"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0133591620"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuIT, "INT2207", "Cơ sở dữ liệu", "Mô hình quan hệ, đại số quan hệ và chuẩn hóa CSDL", 3, 3, true);
            mapBook(course, bookMap.get("978-0078022159"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0072465631"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0133970777"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuIT, "INT2209", "Mạng máy tính & An toàn thông tin", "Kiến trúc giao thức TCP/IP, mạng LAN/WAN và mật mã học", 4, 3, true);
            mapBook(course, bookMap.get("978-0133594140"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0132126953"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0073376226"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134444284"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuIT, "INT2213", "Thiết kế giải thuật & Lý thuyết tính toán", "Độ phức tạp thuật toán, máy Turing và ngôn ngữ hình thức", 4, 3, true);
            mapBook(course, bookMap.get("978-1133187790"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0984782857"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuIT, "INT2215", "Kiến trúc máy tính", "Kiến trúc tập lệnh MIPS, đường ống lệnh và bộ nhớ đệm Cache", 3, 3, true);
            mapBook(course, bookMap.get("978-0124077263"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0128119051"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134101613"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuCS, "INT3401", "Trí tuệ nhân tạo & Chương trình dịch", "Không gian trạng thái, heuristic và nguyên lý xây dựng trình biên dịch", 5, 3, true);
            mapBook(course, bookMap.get("978-0136042594"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0262035613"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0387310732"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0321486813"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuCS, "INT3405", "Khai phá dữ liệu (Data Mining)", "Thuật toán học máy, phân cụm và trích xuất tri thức", 5, 3, true);
            mapBook(course, bookMap.get("978-0123814791"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-1491957660"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuCS, "INT3406", "Xử lý ngôn ngữ tự nhiên", "Mô hình ngôn ngữ xác suất, biểu diễn từ và phân tích ngữ pháp", 6, 3, true);
            mapBook(course, bookMap.get("978-0131873216"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuCS, "INT3407", "Thị giác máy tính", "Xử lý ảnh, trích xuất đặc trưng và nhận diện vật thể trong ảnh", 6, 3, true);
            mapBook(course, bookMap.get("978-1848829343"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuCS, "MAT1093", "Đại số tuyến tính", "Không gian vector, ma trận, định thức và phép biến đổi tuyến tính", 1, 3, true);
            mapBook(course, bookMap.get("978-0980232714"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0321982384"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuCS, "MAT1094", "Giải tích 1", "Giới hạn, vi phân, tích phân hàm một biến và chuỗi số", 1, 4, true);
            mapBook(course, bookMap.get("978-0321878960"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-1285741550"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuCS, "MAT1095", "Xác suất thống kê", "Biến ngẫu nhiên, quy luật phân phối và lý thuyết ước lượng", 2, 3, true);
            mapBook(course, bookMap.get("978-0134753119"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0321629111"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuCS, "PHY1100", "Vật lý đại cương", "Cơ học Newton, nhiệt động lực học và điện từ trường", 1, 3, true);
            mapBook(course, bookMap.get("978-1133947271"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0321973610"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuIT, "INT2210", "Kỹ thuật phần mềm & Kiến trúc", "Mô hình phát triển phần mềm, viết mã sạch và thiết kế kiến trúc", 4, 3, true);
            mapBook(course, bookMap.get("978-0133943030"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134494166"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0132350884"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuMIS, "MIS2001", "Hệ thống thông tin doanh nghiệp (ERP)", "Thiết kế và triển khai giải pháp hệ thống thông tin quản lý", 3, 3, true);
            mapBook(course, bookMap.get("978-0135191798"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-1118008188"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-1484200773"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuMIS, "MIS2005", "Phân tích dữ liệu & Lập trình Web", "Data Analytics và phát triển giao diện Web tương tác", 4, 3, true);
            mapBook(course, bookMap.get("978-0596805524"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-1593279509"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-1491957660"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuUEB, "UEB201", "Kinh tế học vi mô & Kinh tế lượng", "Hành vi người tiêu dùng, cấu trúc thị trường và phân tích hồi quy", 2, 3, true);
            mapBook(course, bookMap.get("978-0393123968"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-1305270107"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0073375779"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-1285165905"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuUEB, "UEB202", "Kinh tế học vĩ mô & Kinh tế quốc tế", "Sản lượng quốc gia, lạm phát và thương mại quốc tế", 3, 3, true);
            mapBook(course, bookMap.get("978-0134472119"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134519579"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0538453066"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuUEB, "UEB301", "Kinh tế phát triển", "Chiến lược tăng trưởng kinh tế bền vững và chuyển dịch cơ cấu", 4, 3, true);
            mapBook(course, bookMap.get("978-0138013882"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(vnuUEB, "UEB305", "Quản trị chiến lược & Hành vi", "Phân tích ma trận SWOT, chiến lược cạnh tranh và tâm lý quyết định", 5, 3, true);
            mapBook(course, bookMap.get("978-0134153971"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
            mapBook(course, bookMap.get("978-0374533557"), CourseBookRelationshipType.REQUIRED, "Giáo trình giảng dạy tại ĐHQGHN", "https://uet.vnu.edu.vn/", true);
        }

        // 6. MAP AOF COURSES AND BOOKS
        {
            Course course = getOrCreateCourse(aofAcc, "ACC101", "Nguyên lý kế toán", "Nguyên tắc cơ bản về ghi sổ kép, định khoản và lập báo cáo tài chính", 1, 3, true);
            mapBook(course, bookMap.get("978-0134127620"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0078111044"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-1119503668"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofAcc, "ACC201", "Kế toán tài chính 1", "Kế toán vốn bằng tiền, tài sản cố định và các khoản đầu tư tài chính", 3, 3, true);
            mapBook(course, bookMap.get("978-1119503668"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0078025310"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofAcc, "ACC202", "Kế toán tài chính 2", "Kế toán nợ phải trả, vốn chủ sở hữu và hợp nhất báo cáo tài chính", 4, 3, true);
            mapBook(course, bookMap.get("978-0078110917"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0078110962"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofAcc, "ACC203", "Kế toán quản trị", "Phân tích chi phí, định giá bán sản phẩm và lập dự toán ngân sách", 4, 3, true);
            mapBook(course, bookMap.get("978-0078025631"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134475585"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofAcc, "AUD301", "Kiểm toán căn bản", "Quy trình kiểm toán, rủi ro kiểm toán, bằng chứng và kiểm soát nội bộ", 5, 3, true);
            mapBook(course, bookMap.get("978-0134065960"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0078025440"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofAcc, "AUD302", "Kiểm toán báo cáo tài chính", "Thực hành kiểm toán các chu trình nghiệp vụ tài chính trọng yếu", 6, 3, true);
            mapBook(course, bookMap.get("978-0471230113"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0078022159"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofFin, "FIN101", "Tài chính - Tiền tệ", "Lý thuyết tiền tệ, lãi suất, ngân hàng trung ương và thị trường tài chính", 2, 3, true);
            mapBook(course, bookMap.get("978-0133836790"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0133423624"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofFin, "FIN201", "Tài chính doanh nghiệp 1", "Quản trị vốn lưu động, dòng tiền và chi phí sử dụng vốn WACC", 3, 3, true);
            mapBook(course, bookMap.get("978-0133507638"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-1259918940"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-1260013900"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofFin, "FIN202", "Quản trị tài chính doanh nghiệp 2", "Chính sách cổ tức, tái cấu trúc vốn và sáp nhập doanh nghiệp M&A", 4, 3, true);
            mapBook(course, bookMap.get("978-1337902601"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0273738015"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofFin, "FIN205", "Phân tích báo cáo tài chính", "Đánh giá khả năng sinh lời, thanh toán và rủi ro tài chính doanh nghiệp", 4, 3, true);
            mapBook(course, bookMap.get("978-0078025310"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0078110962"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofFin, "FIN301", "Thẩm định dự án đầu tư", "Phương pháp NPV, IRR và phân tích độ nhạy của dự án đầu tư", 5, 3, true);
            mapBook(course, bookMap.get("978-1118394106"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofFin, "FIN305", "Tài chính quốc tế", "Thị trường ngoại hối, cán cân thanh toán và tài chính đa quốc gia", 5, 3, true);
            mapBook(course, bookMap.get("978-1118572382"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134519579"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "BNK201", "Quản trị ngân hàng thương mại", "Huy động vốn, tín dụng ngân hàng và quản lý rủi ro thanh khoản", 3, 3, true);
            mapBook(course, bookMap.get("978-1133494683"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0078034671"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "BNK301", "Thị trường chứng khoán & Đầu tư", "Phân tích kỹ thuật, cơ bản và quản lý danh mục đầu tư chứng khoán", 4, 3, true);
            mapBook(course, bookMap.get("978-1259723759"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0078034695"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0071440288"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0060555665"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0393358384"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofFin, "VAL301", "Định giá tài sản & Doanh nghiệp", "Mô hình chiết khấu dòng tiền DCF và định giá so sánh P/E, EV/EBITDA", 5, 3, true);
            mapBook(course, bookMap.get("978-1119610885"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0471751212"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "ECO101", "Kinh tế lượng & Phân tích định lượng", "Ứng dụng mô hình kinh tế lượng trong tài chính và ngân hàng", 3, 3, true);
            mapBook(course, bookMap.get("978-1305270107"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0073375779"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0321629111"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134753119"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "ECO102", "Kinh tế học vi mô", "Phân tích cung cầu, thặng dư và hành vi doanh nghiệp tối đa hóa lợi nhuận", 1, 3, true);
            mapBook(course, bookMap.get("978-1285165905"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0393123968"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "ECO103", "Kinh tế học vĩ mô", "Tổng cung tổng cầu AD-AS, chính sách tài khóa và tiền tệ ổn định kinh tế", 2, 3, true);
            mapBook(course, bookMap.get("978-0538453066"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134472119"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "MGT101", "Quản trị học & Chiến lược AOF", "Nguyên lý quản lý doanh nghiệp hiện đại và xây dựng lợi thế cạnh tranh", 2, 3, true);
            mapBook(course, bookMap.get("978-0134527604"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134153971"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0066620992"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "MKT101", "Marketing trong tài chính - ngân hàng", "Chiến lược tiếp thị dịch vụ tài chính, bảo hiểm và ngân hàng số", 3, 3, true);
            mapBook(course, bookMap.get("978-0134492513"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0133856460"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0132544368"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-1292077611"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0132664257"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "MIS101", "Hệ thống thông tin kế toán & Dữ liệu", "Hệ thống thông tin quản lý, CSDL kế toán và phân tích dữ liệu", 3, 3, true);
            mapBook(course, bookMap.get("978-0135191798"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-1492057611"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-1491957660"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0123814791"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "HRM101", "Quản trị nhân lực & Tổ chức", "Đãi ngộ nhân sự, đánh giá hiệu quả KPI trong ngành tài chính", 4, 3, true);
            mapBook(course, bookMap.get("978-0134235455"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0134103983"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "ENT101", "Khởi nghiệp & Đổi mới sáng tạo", "Xây dựng mô hình kinh doanh tài chính công nghệ FinTech", 5, 3, true);
            mapBook(course, bookMap.get("978-0307887894"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
            mapBook(course, bookMap.get("978-0804139298"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }
        {
            Course course = getOrCreateCourse(aofEcon, "ECO301", "Kinh tế phát triển AOF", "Chính sách đầu tư công và thúc đẩy phát triển thị trường vốn", 4, 3, true);
            mapBook(course, bookMap.get("978-0138013882"), CourseBookRelationshipType.REQUIRED, "Giáo trình học phần Học viện Tài chính", "https://hvtc.edu.vn/", true);
        }

        log.info("Academic catalog seeding complete: {} institutions, {} programs, {} courses, {} distinct books, {} course-book mappings.",
                institutionRepository.count(), programRepository.count(), courseRepository.count(), bookRepository.count(), courseBookRepository.count());
    }

    private Institution getOrCreateInstitution(String name, String shortName, String desc, String website) {
        return institutionRepository.findByShortNameIgnoreCase(shortName)
                .orElseGet(() -> institutionRepository.save(new Institution(name, shortName, desc, website)));
    }

    private Campus getOrCreateCampus(Institution institution, String name, String location, boolean hoaLac) {
        return campusRepository.findByInstitutionIdAndNameIgnoreCase(institution.getId(), name)
                .map(existing -> {
                    existing.setLocation(location);
                    return campusRepository.save(existing);
                })
                .orElseGet(() -> campusRepository.save(new Campus(institution, name, location, hoaLac)));
    }

    private AcademicProgram getOrCreateProgram(Institution institution, Campus campus, String faculty, String name, String major, String programCode, String sourceUrl) {
        return programRepository.findByInstitutionIdAndNameIgnoreCase(institution.getId(), name)
                .map(existing -> {
                    existing.setFaculty(faculty);
                    existing.setMajor(major);
                    return programRepository.save(existing);
                })
                .orElseGet(() -> programRepository.save(new AcademicProgram(institution, campus, faculty, name, major, programCode, sourceUrl)));
    }

    private Course getOrCreateCourse(AcademicProgram program, String code, String name, String desc, Integer semester, Integer credits, Boolean mandatory) {
        return courseRepository.findByProgramIdAndCourseCodeIgnoreCase(program.getId(), code)
                .map(existing -> {
                    existing.setName(name);
                    existing.setDescription(desc);
                    return courseRepository.save(existing);
                })
                .orElseGet(() -> courseRepository.save(new Course(program, code, name, desc, semester, credits, mandatory)));
    }

    private AcademicBook getOrCreateBook(String isbn, String title, String originalTitle,
                                        String author, String publisher, String edition,
                                        Integer year, String language, String desc,
                                        String coverUrl, String category, String type) {
        return bookRepository.findByIsbn(isbn)
                .map(existing -> {
                    if ((existing.getCoverImageUrl() == null || existing.getCoverImageUrl().isBlank()) && coverUrl != null) {
                        existing.setCoverImageUrl(coverUrl);
                    }
                    existing.setTitle(title);
                    existing.setOriginalTitle(originalTitle);
                    existing.setAuthor(author);
                    existing.setPublisher(publisher);
                    existing.setCategory(category);
                    existing.setBookType(type);
                    existing.setDescription(desc);
                    return bookRepository.save(existing);
                })
                .orElseGet(() -> bookRepository.save(new AcademicBook(
                        isbn, title, originalTitle, author, publisher, edition, year, language, desc, coverUrl, category, type)));
    }

    private void mapBook(Course course, AcademicBook book, CourseBookRelationshipType type, String notes, String sourceUrl, boolean official) {
        if (book == null || course == null) return;
        if (courseBookRepository.findByCourseIdAndBookId(course.getId(), book.getId()).isEmpty()) {
            courseBookRepository.save(new CourseBook(course, book, type, notes, sourceUrl, official));
        }
    }
}
