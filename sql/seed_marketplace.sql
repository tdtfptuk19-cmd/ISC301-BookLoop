USE BookLoopDB;
SET NOCOUNT ON;
PRINT '=======================================================';
PRINT 'Seeding 100 Active Marketplace Listings from Textbooks ';
PRINT '=======================================================';

-- 1. Ensure 6 Demo Student Seller Accounts

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'fptu.seller1@bookloop.vn')
    INSERT INTO users (full_name, email, password, university, campus, phone, role, enabled, created_at, updated_at)
    VALUES (N'Nguyễn Hoàng Nam', 'fptu.seller1@bookloop.vn', '$2a$10$FGK3mdwqotMH3B9ky2ULOehJu/T.WJ5JF9mz13UFMrIWFOdLfMBWO', N'Trường Đại học FPT', N'FPT University Hòa Lạc', '0981112233', 'STUDENT', 1, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'fptu.seller2@bookloop.vn')
    INSERT INTO users (full_name, email, password, university, campus, phone, role, enabled, created_at, updated_at)
    VALUES (N'Trần Minh Đức', 'fptu.seller2@bookloop.vn', '$2a$10$FGK3mdwqotMH3B9ky2ULOehJu/T.WJ5JF9mz13UFMrIWFOdLfMBWO', N'Trường Đại học FPT', N'FPT University Hòa Lạc', '0981112244', 'STUDENT', 1, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'vnu.seller1@bookloop.vn')
    INSERT INTO users (full_name, email, password, university, campus, phone, role, enabled, created_at, updated_at)
    VALUES (N'Lê Thanh Thảo', 'vnu.seller1@bookloop.vn', '$2a$10$FGK3mdwqotMH3B9ky2ULOehJu/T.WJ5JF9mz13UFMrIWFOdLfMBWO', N'Đại học Quốc gia Hà Nội', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', '0982223344', 'STUDENT', 1, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'vnu.seller2@bookloop.vn')
    INSERT INTO users (full_name, email, password, university, campus, phone, role, enabled, created_at, updated_at)
    VALUES (N'Phạm Quang Huy', 'vnu.seller2@bookloop.vn', '$2a$10$FGK3mdwqotMH3B9ky2ULOehJu/T.WJ5JF9mz13UFMrIWFOdLfMBWO', N'Đại học Quốc gia Hà Nội', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', '0982223355', 'STUDENT', 1, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'aof.seller1@bookloop.vn')
    INSERT INTO users (full_name, email, password, university, campus, phone, role, enabled, created_at, updated_at)
    VALUES (N'Đỗ Mai Anh', 'aof.seller1@bookloop.vn', '$2a$10$FGK3mdwqotMH3B9ky2ULOehJu/T.WJ5JF9mz13UFMrIWFOdLfMBWO', N'Học viện Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', '0983334455', 'STUDENT', 1, GETDATE(), GETDATE());

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'aof.seller2@bookloop.vn')
    INSERT INTO users (full_name, email, password, university, campus, phone, role, enabled, created_at, updated_at)
    VALUES (N'Vũ Tuấn Kiệt', 'aof.seller2@bookloop.vn', '$2a$10$FGK3mdwqotMH3B9ky2ULOehJu/T.WJ5JF9mz13UFMrIWFOdLfMBWO', N'Học viện Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', '0983334466', 'STUDENT', 1, GETDATE(), GETDATE());

-- 2. Insert 100 Marketplace Listings & Promotion Payments

-- Listing 1: 978-0131103627 - The C Programming Language
BEGIN
    DECLARE @bid_0 BIGINT = 1;
    DECLARE @sid_0 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_0 AND seller_id = @sid_0)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_0, @sid_0, N'The C Programming Language', N'Brian W. Kernighan, Dennis M. Ritchie', N'Prentice Hall', N'2nd', '978-0131103627', 'PRF192',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://m.media-amazon.com/images/I/411S7Ez9TTL._SX377_BO1,204,203,200_.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            1, DATEADD(day, 7, GETDATE()), GETDATE(), GETDATE()
        );
        
        DECLARE @pid_0 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 1 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_0, @pid_0, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-001', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_0, @pid_0, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-001', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 2: 978-0596009205 - Head First Java
BEGIN
    DECLARE @bid_1 BIGINT = 2;
    DECLARE @sid_1 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_1 AND seller_id = @sid_1)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_1, @sid_1, N'Head First Java', N'Kathy Sierra, Bert Bates', N'O''Reilly Media', N'2nd', '978-0596009205', 'PRO192',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 95000, 'GOOD', 'AVAILABLE', 'https://m.media-amazon.com/images/I/81U5RkFRGlL._SL1500_.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            1, DATEADD(day, 7, GETDATE()), GETDATE(), GETDATE()
        );
        
        DECLARE @pid_1 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 1 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_1, @pid_1, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-002', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_1, @pid_1, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-002', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 3: 978-0134685991 - Effective Java
BEGIN
    DECLARE @bid_2 BIGINT = 3;
    DECLARE @sid_2 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_2 AND seller_id = @sid_2)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_2, @sid_2, N'Effective Java', N'Joshua Bloch', N'Addison-Wesley', N'3rd', '978-0134685991', 'PRO192',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 125000, 'LIKE_NEW', 'AVAILABLE', 'https://m.media-amazon.com/images/I/71oDkgbO7HL._SL1500_.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_2 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_2, @pid_2, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-003', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_2, @pid_2, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-003', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 4: 978-0262033848 - Introduction to Algorithms
BEGIN
    DECLARE @bid_3 BIGINT = 4;
    DECLARE @sid_3 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_3 AND seller_id = @sid_3)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_3, @sid_3, N'Introduction to Algorithms', N'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein', N'MIT Press', N'3rd', '978-0262033848', 'CSD201',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 70000, 'ACCEPTABLE', 'AVAILABLE', 'https://m.media-amazon.com/images/I/61ZXunAMYyL._SL1233_.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_3 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_3, @pid_3, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-004', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_3, @pid_3, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-004', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 5: 978-0078022159 - Database System Concepts
BEGIN
    DECLARE @bid_4 BIGINT = 5;
    DECLARE @sid_4 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_4 AND seller_id = @sid_4)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_4, @sid_4, N'Database System Concepts', N'Abraham Silberschatz, Henry F. Korth, S. Sudarshan', N'McGraw-Hill', N'7th', '978-0078022159', 'DBI202',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 140000, 'NEW', 'AVAILABLE', 'https://m.media-amazon.com/images/I/71Y8KzN+p9L._SL1500_.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_4 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_4, @pid_4, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-005', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_4, @pid_4, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-005', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 6: 978-0133594140 - Computer Networking: A Top-Dow
BEGIN
    DECLARE @bid_5 BIGINT = 6;
    DECLARE @sid_5 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_5 AND seller_id = @sid_5)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_5, @sid_5, N'Computer Networking: A Top-Down Approach', N'James F. Kurose, Keith W. Ross', N'Pearson', N'5th', '978-0133594140', 'NWC203',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://m.media-amazon.com/images/I/71E4lJz6n9L._SL1200_.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_5 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_5, @pid_5, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-006', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_5, @pid_5, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-006', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 7: 978-1119800361 - Operating System Concepts
BEGIN
    DECLARE @bid_6 BIGINT = 7;
    DECLARE @sid_6 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_6 AND seller_id = @sid_6)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_6, @sid_6, N'Operating System Concepts', N'Abraham Silberschatz, Peter B. Galvin, Greg Gagne', N'Wiley', N'10th', '978-1119800361', 'OSG202',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 100000, 'GOOD', 'AVAILABLE', 'https://m.media-amazon.com/images/I/81RrFzjbdFL._SL1500_.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_6 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_6, @pid_6, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-007', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_6, @pid_6, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-007', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 8: 978-0134494166 - Clean Architecture
BEGIN
    DECLARE @bid_7 BIGINT = 8;
    DECLARE @sid_7 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_7 AND seller_id = @sid_7)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_7, @sid_7, N'Clean Architecture', N'Robert C. Martin', N'Prentice Hall', N'10th', '978-0134494166', 'SWP391',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 125000, 'LIKE_NEW', 'AVAILABLE', 'https://m.media-amazon.com/images/I/81I2a93S5+L._SL1500_.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_7 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_7, @pid_7, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-008', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_7, @pid_7, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-008', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 9: 978-0131489066 - Clean Code: A Handbook of Agil
BEGIN
    DECLARE @bid_8 BIGINT = 9;
    DECLARE @sid_8 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_8 AND seller_id = @sid_8)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_8, @sid_8, N'Clean Code: A Handbook of Agile Software Craftsmanship', N'Robert C. Martin', N'Prentice Hall', N'1st', '978-0131489066', 'SWP391',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 80000, 'ACCEPTABLE', 'AVAILABLE', 'https://m.media-amazon.com/images/I/71T7aD3EOTL._SL1500_.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_8 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_8, @pid_8, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-009', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_8, @pid_8, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-009', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 10: 978-1492032649 - Hands-On Machine Learning with
BEGIN
    DECLARE @bid_9 BIGINT = 10;
    DECLARE @sid_9 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_9 AND seller_id = @sid_9)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_9, @sid_9, N'Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow', N'Aurélien Géron', N'O''Reilly Media', N'2nd', '978-1492032649', 'AIH201',
            N'Khoa học dữ liệu & AI', N'FPT University Hòa Lạc', 150000, 'NEW', 'AVAILABLE', 'https://m.media-amazon.com/images/I/81b2y2DODLL._SL1500_.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_9 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_9, @pid_9, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-010', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_9, @pid_9, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-010', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 11: 978-0136042594 - Artificial Intelligence: A Mod
BEGIN
    DECLARE @bid_10 BIGINT = 11;
    DECLARE @sid_10 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_10 AND seller_id = @sid_10)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_10, @sid_10, N'Artificial Intelligence: A Modern Approach', N'Stuart Russell, Peter Norvig', N'Pearson', N'3rd', '978-0136042594', 'AIH201',
            N'Khoa học dữ liệu & AI', N'FPT University Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://m.media-amazon.com/images/I/71rBqf0VCTL._SL1500_.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_10 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_10, @pid_10, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-011', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_10, @pid_10, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-011', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 12: 978-0134492513 - Principles of Marketing
BEGIN
    DECLARE @bid_11 BIGINT = 14;
    DECLARE @sid_11 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_11 AND seller_id = @sid_11)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_11, @sid_11, N'Principles of Marketing', N'Philip Kotler, Gary Armstrong', N'Pearson', N'17th', '978-0134492513', 'MKT101',
            N'Kinh tế - Quản trị', N'FPT University Hòa Lạc', 90000, 'GOOD', 'AVAILABLE', 'https://m.media-amazon.com/images/I/61rxNnGr+LL._SL1000_.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_11 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_11, @pid_11, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-012', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_11, @pid_11, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-012', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 13: 978-1337617406 - Principles of Microeconomics
BEGIN
    DECLARE @bid_12 BIGINT = 15;
    DECLARE @sid_12 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_12 AND seller_id = @sid_12)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_12, @sid_12, N'Principles of Microeconomics', N'N. Gregory Mankiw', N'Cengage Learning', N'9th', '978-1337617406', 'ECO111',
            N'Kinh tế - Quản trị', N'FPT University Hòa Lạc', 110000, 'LIKE_NEW', 'AVAILABLE', 'https://m.media-amazon.com/images/I/81bH9j+k0wL._SL1500_.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_12 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_12, @pid_12, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-013', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_12, @pid_12, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-013', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 14: 978-1337617390 - Principles of Macroeconomics
BEGIN
    DECLARE @bid_13 BIGINT = 16;
    DECLARE @sid_13 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_13 AND seller_id = @sid_13)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_13, @sid_13, N'Principles of Macroeconomics', N'N. Gregory Mankiw', N'Cengage Learning', N'9th', '978-1337617390', 'ECO121',
            N'Kinh tế - Quản trị', N'FPT University Hòa Lạc', 60000, 'ACCEPTABLE', 'AVAILABLE', 'https://m.media-amazon.com/images/I/81Wn0P8n67L._SL1500_.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_13 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_13, @pid_13, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-014', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_13, @pid_13, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-014', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 15: 978-0134177304 - Core Java Volume I--Fundamenta
BEGIN
    DECLARE @bid_14 BIGINT = 23;
    DECLARE @sid_14 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_14 AND seller_id = @sid_14)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_14, @sid_14, N'Core Java Volume I--Fundamentals', N'Cay S. Horstmann', N'Prentice Hall', N'10th', '978-0134177304', 'PRO192',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 160000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134177304-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_14 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_14, @pid_14, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-015', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_14, @pid_14, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-015', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 16: 978-0132576277 - Data Structures and Algorithm 
BEGIN
    DECLARE @bid_15 BIGINT = 24;
    DECLARE @sid_15 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_15 AND seller_id = @sid_15)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_15, @sid_15, N'Data Structures and Algorithm Analysis in Java', N'Mark Allen Weiss', N'Pearson', N'3rd', '978-0132576277', 'CSD201',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780132576277-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_15 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_15, @pid_15, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-016', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_15, @pid_15, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-016', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 17: 978-0321573513 - Algorithms
BEGIN
    DECLARE @bid_16 BIGINT = 25;
    DECLARE @sid_16 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_16 AND seller_id = @sid_16)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_16, @sid_16, N'Algorithms', N'Robert Sedgewick, Kevin Wayne', N'Addison-Wesley', N'4th', '978-0321573513', 'CSD201',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 90000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780321573513-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_16 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_16, @pid_16, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-017', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_16, @pid_16, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-017', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 18: 978-0133970777 - Fundamentals of Database Syste
BEGIN
    DECLARE @bid_17 BIGINT = 27;
    DECLARE @sid_17 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_17 AND seller_id = @sid_17)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_17, @sid_17, N'Fundamentals of Database Systems', N'Ramez Elmasri, Shamkant B. Navathe', N'Pearson', N'7th', '978-0133970777', 'DBI202',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 125000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780133970777-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_17 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_17, @pid_17, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-018', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_17, @pid_17, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-018', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 19: 978-1492057611 - Learning SQL
BEGIN
    DECLARE @bid_18 BIGINT = 29;
    DECLARE @sid_18 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_18 AND seller_id = @sid_18)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_18, @sid_18, N'Learning SQL', N'Alan Beaulieu', N'O''Reilly Media', N'3rd', '978-1492057611', 'DBI202',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 70000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781492057611-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_18 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_18, @pid_18, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-019', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_18, @pid_18, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-019', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 20: 978-0132126953 - Computer Networks
BEGIN
    DECLARE @bid_19 BIGINT = 30;
    DECLARE @sid_19 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_19 AND seller_id = @sid_19)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_19, @sid_19, N'Computer Networks', N'Andrew S. Tanenbaum, David J. Wetherall', N'Pearson', N'5th', '978-0132126953', 'NWC203',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 170000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780132126953-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_19 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_19, @pid_19, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-020', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_19, @pid_19, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-020', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 21: 978-0133591620 - Modern Operating Systems
BEGIN
    DECLARE @bid_20 BIGINT = 32;
    DECLARE @sid_20 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_20 AND seller_id = @sid_20)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_20, @sid_20, N'Modern Operating Systems', N'Andrew S. Tanenbaum, Herbert Bos', N'Pearson', N'4th', '978-0133591620', 'OSG202',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780133591620-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_20 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_20, @pid_20, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-021', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_20, @pid_20, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-021', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 22: 978-0133943030 - Software Engineering
BEGIN
    DECLARE @bid_21 BIGINT = 33;
    DECLARE @sid_21 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_21 AND seller_id = @sid_21)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_21, @sid_21, N'Software Engineering', N'Ian Sommerville', N'Pearson', N'10th', '978-0133943030', 'SWE201',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 95000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780133943030-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_21 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_21, @pid_21, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-022', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_21, @pid_21, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-022', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 23: 978-0132350884 - Clean Code
BEGIN
    DECLARE @bid_22 BIGINT = 34;
    DECLARE @sid_22 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_22 AND seller_id = @sid_22)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_22, @sid_22, N'Clean Code', N'Robert C. Martin', N'Prentice Hall', N'1st', '978-0132350884', 'SWE201',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 125000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_22 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_22, @pid_22, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-023', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_22, @pid_22, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-023', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 24: 978-0135957059 - The Pragmatic Programmer
BEGIN
    DECLARE @bid_23 BIGINT = 35;
    DECLARE @sid_23 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_23 AND seller_id = @sid_23)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_23, @sid_23, N'The Pragmatic Programmer', N'David Thomas, Andrew Hunt', N'Addison-Wesley', N'20th Anniversary', '978-0135957059', 'SWP391',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 80000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780135957059-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_23 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_23, @pid_23, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-024', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_23, @pid_23, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-024', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 25: 978-0201633610 - Design Patterns
BEGIN
    DECLARE @bid_24 BIGINT = 36;
    DECLARE @sid_24 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_24 AND seller_id = @sid_24)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_24, @sid_24, N'Design Patterns', N'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', N'Addison-Wesley', N'1st', '978-0201633610', 'PRJ301',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 140000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_24 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_24, @pid_24, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-025', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_24, @pid_24, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-025', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 26: 978-0321125217 - Domain-Driven Design
BEGIN
    DECLARE @bid_25 BIGINT = 37;
    DECLARE @sid_25 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_25 AND seller_id = @sid_25)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_25, @sid_25, N'Domain-Driven Design', N'Eric Evans', N'Addison-Wesley', N'1st', '978-0321125217', 'SWP391',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780321125217-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_25 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_25, @pid_25, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-026', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_25, @pid_25, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-026', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 27: 978-0201485677 - Refactoring
BEGIN
    DECLARE @bid_26 BIGINT = 38;
    DECLARE @sid_26 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_26 AND seller_id = @sid_26)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_26, @sid_26, N'Refactoring', N'Martin Fowler', N'Addison-Wesley', N'1st', '978-0201485677', 'SWP391',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 100000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780201485677-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_26 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_26, @pid_26, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-027', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_26, @pid_26, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-027', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 28: 978-1449373320 - Designing Data-Intensive Appli
BEGIN
    DECLARE @bid_27 BIGINT = 39;
    DECLARE @sid_27 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_27 AND seller_id = @sid_27)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_27, @sid_27, N'Designing Data-Intensive Applications', N'Martin Kleppmann', N'O''Reilly Media', N'1st', '978-1449373320', 'SWP391',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 125000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781449373320-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_27 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_27, @pid_27, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-028', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_27, @pid_27, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-028', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 29: 978-1617294945 - Spring in Action
BEGIN
    DECLARE @bid_28 BIGINT = 40;
    DECLARE @sid_28 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_28 AND seller_id = @sid_28)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_28, @sid_28, N'Spring in Action', N'Craig Walls', N'Manning Publications', N'5th', '978-1617294945', 'PRJ301',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 75000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781617294945-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_28 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_28, @pid_28, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-029', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_28, @pid_28, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-029', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 30: 978-0735679665 - Software Requirements
BEGIN
    DECLARE @bid_29 BIGINT = 41;
    DECLARE @sid_29 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_29 AND seller_id = @sid_29)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_29, @sid_29, N'Software Requirements', N'Karl Wiegers, Joy Beatty', N'Microsoft Press', N'3rd', '978-0735679665', 'SWR302',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 150000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780735679665-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_29 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_29, @pid_29, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-030', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_29, @pid_29, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-030', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 31: 978-0134706054 - Android Programming: The Big N
BEGIN
    DECLARE @bid_30 BIGINT = 42;
    DECLARE @sid_30 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_30 AND seller_id = @sid_30)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_30, @sid_30, N'Android Programming: The Big Nerd Ranch Guide', N'Bill Phillips, Chris Stewart, Kristin Marsicano', N'Big Nerd Ranch Guides', N'3rd', '978-0134706054', 'PRM392',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134706054-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_30 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_30, @pid_30, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-031', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_30, @pid_30, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-031', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 32: 978-0996025515 - Internet of Things: A Hands-On
BEGIN
    DECLARE @bid_31 BIGINT = 43;
    DECLARE @sid_31 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_31 AND seller_id = @sid_31)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_31, @sid_31, N'Internet of Things: A Hands-On Approach', N'Arshdeep Bahga, Vijay Madisetti', N'VPT', N'1st', '978-0996025515', 'IOT102',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 105000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780996025515-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_31 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_31, @pid_31, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-032', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_31, @pid_31, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-032', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 33: 978-0134444284 - Cryptography and Network Secur
BEGIN
    DECLARE @bid_32 BIGINT = 44;
    DECLARE @sid_32 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'fptu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_32 AND seller_id = @sid_32)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_32, @sid_32, N'Cryptography and Network Security', N'William Stallings', N'Pearson', N'7th', '978-0134444284', 'NWC203',
            N'Công nghệ thông tin', N'FPT University Hòa Lạc', 125000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134444284-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_32 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_32, @pid_32, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-033', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_32, @pid_32, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-033', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 34: 978-6046712345 - Giáo trình Cấu trúc dữ liệu và
BEGIN
    DECLARE @bid_33 BIGINT = 12;
    DECLARE @sid_33 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_33 AND seller_id = @sid_33)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_33, @sid_33, N'Giáo trình Cấu trúc dữ liệu và giải thuật (ĐHQGHN)', N'Đinh Mạnh Tường', N'NXB Đại học Quốc gia Hà Nội', N'1st', '978-6046712345', 'INT1008',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780131103627-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            1, DATEADD(day, 7, GETDATE()), GETDATE(), GETDATE()
        );
        
        DECLARE @pid_33 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 1 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_33, @pid_33, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-034', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_33, @pid_33, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-034', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 35: 978-6046719876 - Giáo trình Lập trình hướng đối
BEGIN
    DECLARE @bid_34 BIGINT = 13;
    DECLARE @sid_34 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_34 AND seller_id = @sid_34)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_34, @sid_34, N'Giáo trình Lập trình hướng đối tượng với Java', N'Trương Anh Hoàng', N'NXB Đại học Quốc gia Hà Nội', N'1st', '978-6046719876', 'INT2204',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 80000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            1, DATEADD(day, 7, GETDATE()), GETDATE(), GETDATE()
        );
        
        DECLARE @pid_34 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 1 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_34, @pid_34, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-035', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_34, @pid_34, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-035', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 36: 978-1848000698 - The Algorithm Design Manual
BEGIN
    DECLARE @bid_35 BIGINT = 26;
    DECLARE @sid_35 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_35 AND seller_id = @sid_35)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_35, @sid_35, N'The Algorithm Design Manual', N'Steven S. Skiena', N'Springer', N'2nd', '978-1848000698', 'INT1008',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 140000, 'NEW', 'AVAILABLE', 'https://books.google.com/books/content?id=TrXd-gxPhVYC&printsec=frontcover&img=1&zoom=1', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_35 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_35, @pid_35, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-036', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_35, @pid_35, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-036', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 37: 978-0072465631 - Database Management Systems
BEGIN
    DECLARE @bid_36 BIGINT = 28;
    DECLARE @sid_36 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_36 AND seller_id = @sid_36)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_36, @sid_36, N'Database Management Systems', N'Raghu Ramakrishnan, Johannes Gehrke', N'McGraw-Hill', N'3rd', '978-0072465631', 'INT2207',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 130000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780072465631-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_36 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_36, @pid_36, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-037', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_36, @pid_36, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-037', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 38: 978-0073376226 - Data Communications and Networ
BEGIN
    DECLARE @bid_37 BIGINT = 31;
    DECLARE @sid_37 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_37 AND seller_id = @sid_37)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_37, @sid_37, N'Data Communications and Networking', N'Behrouz A. Forouzan', N'McGraw-Hill', N'5th', '978-0073376226', 'INT2209',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 100000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780073376226-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_37 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_37, @pid_37, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-038', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_37, @pid_37, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-038', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 39: 978-0321486813 - Compilers: Principles, Techniq
BEGIN
    DECLARE @bid_38 BIGINT = 45;
    DECLARE @sid_38 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_38 AND seller_id = @sid_38)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_38, @sid_38, N'Compilers: Principles, Techniques, and Tools', N'Alfred V. Aho, Monica S. Lam, Ravi Sethi, Jeffrey D. Ullman', N'Addison-Wesley', N'2nd', '978-0321486813', 'INT3401',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780321486813-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_38 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_38, @pid_38, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-039', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_38, @pid_38, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-039', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 40: 978-1133187790 - Introduction to the Theory of 
BEGIN
    DECLARE @bid_39 BIGINT = 46;
    DECLARE @sid_39 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_39 AND seller_id = @sid_39)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_39, @sid_39, N'Introduction to the Theory of Computation', N'Michael Sipser', N'Cengage Learning', N'3rd', '978-1133187790', 'INT2213',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 75000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781133187790-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_39 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_39, @pid_39, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-040', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_39, @pid_39, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-040', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 41: 978-0124077263 - Computer Organization and Desi
BEGIN
    DECLARE @bid_40 BIGINT = 47;
    DECLARE @sid_40 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_40 AND seller_id = @sid_40)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_40, @sid_40, N'Computer Organization and Design', N'David A. Patterson, John L. Hennessy', N'Morgan Kaufmann', N'5th', '978-0124077263', 'INT2215',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 150000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780124077263-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_40 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_40, @pid_40, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-041', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_40, @pid_40, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-041', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 42: 978-0128119051 - Computer Architecture: A Quant
BEGIN
    DECLARE @bid_41 BIGINT = 48;
    DECLARE @sid_41 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_41 AND seller_id = @sid_41)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_41, @sid_41, N'Computer Architecture: A Quantitative Approach', N'John L. Hennessy, David A. Patterson', N'Morgan Kaufmann', N'6th', '978-0128119051', 'INT2215',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 130000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780128119051-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_41 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_41, @pid_41, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-042', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_41, @pid_41, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-042', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 43: 978-0134101613 - Computer Organization and Arch
BEGIN
    DECLARE @bid_42 BIGINT = 49;
    DECLARE @sid_42 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_42 AND seller_id = @sid_42)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_42, @sid_42, N'Computer Organization and Architecture', N'William Stallings', N'Pearson', N'10th', '978-0134101613', 'INT2215',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 105000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134101613-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_42 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_42, @pid_42, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-043', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_42, @pid_42, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-043', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 44: 978-0735611313 - Code: The Hidden Language of C
BEGIN
    DECLARE @bid_43 BIGINT = 50;
    DECLARE @sid_43 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_43 AND seller_id = @sid_43)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_43, @sid_43, N'Code: The Hidden Language of Computer Hardware and Software', N'Charles Petzold', N'Microsoft Press', N'1st', '978-0735611313', 'INT1006',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780735611313-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_43 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_43, @pid_43, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-044', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_43, @pid_43, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-044', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 45: 978-0984782857 - Cracking the Coding Interview
BEGIN
    DECLARE @bid_44 BIGINT = 51;
    DECLARE @sid_44 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_44 AND seller_id = @sid_44)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_44, @sid_44, N'Cracking the Coding Interview', N'Gayle Laakmann McDowell', N'CareerCup', N'6th', '978-0984782857', 'INT2213',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 70000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780984782857-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_44 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_44, @pid_44, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-045', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_44, @pid_44, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-045', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 46: 978-1484200773 - Pro Git
BEGIN
    DECLARE @bid_45 BIGINT = 53;
    DECLARE @sid_45 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_45 AND seller_id = @sid_45)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_45, @sid_45, N'Pro Git', N'Scott Chacon, Ben Straub', N'Apress', N'2nd', '978-1484200773', 'MIS2001',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 160000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781484200773-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_45 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_45, @pid_45, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-046', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_45, @pid_45, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-046', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 47: 978-1118008188 - HTML and CSS: Design and Build
BEGIN
    DECLARE @bid_46 BIGINT = 56;
    DECLARE @sid_46 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_46 AND seller_id = @sid_46)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_46, @sid_46, N'HTML and CSS: Design and Build Websites', N'Jon Duckett', N'Wiley', N'1st', '978-1118008188', 'MIS2001',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 130000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781118008188-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_46 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_46, @pid_46, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-047', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_46, @pid_46, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-047', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 48: 978-0596805524 - JavaScript: The Definitive Gui
BEGIN
    DECLARE @bid_47 BIGINT = 57;
    DECLARE @sid_47 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_47 AND seller_id = @sid_47)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_47, @sid_47, N'JavaScript: The Definitive Guide', N'David Flanagan', N'O''Reilly Media', N'6th', '978-0596805524', 'MIS2005',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 90000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780596805524-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_47 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_47, @pid_47, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-048', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_47, @pid_47, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-048', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 49: 978-1593279509 - Eloquent JavaScript
BEGIN
    DECLARE @bid_48 BIGINT = 58;
    DECLARE @sid_48 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_48 AND seller_id = @sid_48)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_48, @sid_48, N'Eloquent JavaScript', N'Marijn Haverbeke', N'No Starch Press', N'3rd', '978-1593279509', 'MIS2005',
            N'Công nghệ thông tin', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781593279509-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_48 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_48, @pid_48, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-049', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_48, @pid_48, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-049', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 50: 978-1491957660 - Python for Data Analysis
BEGIN
    DECLARE @bid_49 BIGINT = 62;
    DECLARE @sid_49 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_49 AND seller_id = @sid_49)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_49, @sid_49, N'Python for Data Analysis', N'Wes McKinney', N'O''Reilly Media', N'2nd', '978-1491957660', 'MIS2005',
            N'Khoa học dữ liệu & AI', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 80000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781491957660-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_49 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_49, @pid_49, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-050', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_49, @pid_49, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-050', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 51: 978-0262035613 - Deep Learning
BEGIN
    DECLARE @bid_50 BIGINT = 63;
    DECLARE @sid_50 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_50 AND seller_id = @sid_50)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_50, @sid_50, N'Deep Learning', N'Ian Goodfellow, Yoshua Bengio, Aaron Courville', N'MIT Press', N'1st', '978-0262035613', 'INT3401',
            N'Khoa học dữ liệu & AI', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 170000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780262035613-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_50 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_50, @pid_50, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-051', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_50, @pid_50, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-051', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 52: 978-0387310732 - Pattern Recognition and Machin
BEGIN
    DECLARE @bid_51 BIGINT = 64;
    DECLARE @sid_51 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_51 AND seller_id = @sid_51)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_51, @sid_51, N'Pattern Recognition and Machine Learning', N'Christopher M. Bishop', N'Springer', N'1st', '978-0387310732', 'INT3401',
            N'Khoa học dữ liệu & AI', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 130000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780387310732-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_51 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_51, @pid_51, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-052', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_51, @pid_51, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-052', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 53: 978-1848829343 - Computer Vision: Algorithms an
BEGIN
    DECLARE @bid_52 BIGINT = 65;
    DECLARE @sid_52 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_52 AND seller_id = @sid_52)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_52, @sid_52, N'Computer Vision: Algorithms and Applications', N'Richard Szeliski', N'Springer', N'1st', '978-1848829343', 'INT3407',
            N'Khoa học dữ liệu & AI', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 95000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781848829343-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_52 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_52, @pid_52, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-053', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_52, @pid_52, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-053', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 54: 978-0131873216 - Speech and Language Processing
BEGIN
    DECLARE @bid_53 BIGINT = 66;
    DECLARE @sid_53 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_53 AND seller_id = @sid_53)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_53, @sid_53, N'Speech and Language Processing', N'Daniel Jurafsky, James H. Martin', N'Prentice Hall', N'2nd', '978-0131873216', 'INT3406',
            N'Khoa học dữ liệu & AI', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780131873216-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_53 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_53, @pid_53, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-054', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_53, @pid_53, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-054', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 55: 978-0123814791 - Data Mining: Concepts and Tech
BEGIN
    DECLARE @bid_54 BIGINT = 67;
    DECLARE @sid_54 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_54 AND seller_id = @sid_54)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_54, @sid_54, N'Data Mining: Concepts and Techniques', N'Jiawei Han, Micheline Kamber, Jian Pei', N'Morgan Kaufmann', N'3rd', '978-0123814791', 'INT3405',
            N'Khoa học dữ liệu & AI', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 75000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780123814791-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_54 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_54, @pid_54, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-055', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_54, @pid_54, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-055', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 56: 978-0321629111 - Probability and Statistics for
BEGIN
    DECLARE @bid_55 BIGINT = 69;
    DECLARE @sid_55 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_55 AND seller_id = @sid_55)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_55, @sid_55, N'Probability and Statistics for Engineers and Scientists', N'Ronald E. Walpole, Raymond H. Myers, Sharon L. Myers, Keying Ye', N'Pearson', N'9th', '978-0321629111', 'MAT1095',
            N'Khoa học cơ bản', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 110000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780321629111-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_55 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_55, @pid_55, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-056', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_55, @pid_55, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-056', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 57: 978-0980232714 - Introduction to Linear Algebra
BEGIN
    DECLARE @bid_56 BIGINT = 70;
    DECLARE @sid_56 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_56 AND seller_id = @sid_56)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_56, @sid_56, N'Introduction to Linear Algebra', N'Gilbert Strang', N'Wellesley-Cambridge Press', N'4th', '978-0980232714', 'MAT1093',
            N'Khoa học cơ bản', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 100000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780980232714-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_56 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_56, @pid_56, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-057', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_56, @pid_56, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-057', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 58: 978-0321982384 - Linear Algebra and Its Applica
BEGIN
    DECLARE @bid_57 BIGINT = 71;
    DECLARE @sid_57 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_57 AND seller_id = @sid_57)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_57, @sid_57, N'Linear Algebra and Its Applications', N'David C. Lay, Steven R. Lay, Judi J. McDonald', N'Pearson', N'5th', '978-0321982384', 'MAT1093',
            N'Khoa học cơ bản', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 70000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780321982384-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_57 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_57, @pid_57, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-058', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_57, @pid_57, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-058', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 59: 978-0321878960 - Thomas' Calculus
BEGIN
    DECLARE @bid_58 BIGINT = 72;
    DECLARE @sid_58 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_58 AND seller_id = @sid_58)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_58, @sid_58, N'Thomas'' Calculus', N'George B. Thomas, Maurice D. Weir, Joel R. Hass', N'Pearson', N'13th', '978-0321878960', 'MAT1094',
            N'Khoa học cơ bản', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 85000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780321878960-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_58 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_58, @pid_58, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-059', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_58, @pid_58, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-059', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 60: 978-1285741550 - Calculus: Early Transcendental
BEGIN
    DECLARE @bid_59 BIGINT = 73;
    DECLARE @sid_59 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_59 AND seller_id = @sid_59)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_59, @sid_59, N'Calculus: Early Transcendentals', N'James Stewart', N'Cengage Learning', N'8th', '978-1285741550', 'MAT1094',
            N'Khoa học cơ bản', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 45000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781285741550-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_59 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_59, @pid_59, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-060', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_59, @pid_59, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-060', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 61: 978-0134753119 - A First Course in Probability
BEGIN
    DECLARE @bid_60 BIGINT = 74;
    DECLARE @sid_60 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_60 AND seller_id = @sid_60)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_60, @sid_60, N'A First Course in Probability', N'Sheldon Ross', N'Pearson', N'10th', '978-0134753119', 'MAT1095',
            N'Khoa học cơ bản', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 120000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134753119-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_60 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_60, @pid_60, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-061', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_60, @pid_60, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-061', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 62: 978-1133947271 - Physics for Scientists and Eng
BEGIN
    DECLARE @bid_61 BIGINT = 75;
    DECLARE @sid_61 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_61 AND seller_id = @sid_61)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_61, @sid_61, N'Physics for Scientists and Engineers with Modern Physics', N'Raymond A. Serway, John W. Jewett', N'Cengage Learning', N'9th', '978-1133947271', 'PHY1100',
            N'Khoa học cơ bản', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 100000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781133947271-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_61 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_61, @pid_61, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-062', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_61, @pid_61, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-062', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 63: 978-0321973610 - University Physics with Modern
BEGIN
    DECLARE @bid_62 BIGINT = 76;
    DECLARE @sid_62 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_62 AND seller_id = @sid_62)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_62, @sid_62, N'University Physics with Modern Physics', N'Hugh D. Young, Roger A. Freedman', N'Pearson', N'14th', '978-0321973610', 'PHY1100',
            N'Khoa học cơ bản', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 75000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780321973610-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_62 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_62, @pid_62, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-063', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_62, @pid_62, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-063', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 64: 978-1285165905 - Principles of Microeconomics
BEGIN
    DECLARE @bid_63 BIGINT = 79;
    DECLARE @sid_63 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_63 AND seller_id = @sid_63)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_63, @sid_63, N'Principles of Microeconomics', N'N. Gregory Mankiw', N'Cengage Learning', N'7th', '978-1285165905', 'UEB201',
            N'Kinh tế - Quản trị', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 100000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781285165905-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_63 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_63, @pid_63, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-064', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_63, @pid_63, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-064', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 65: 978-0538453066 - Principles of Macroeconomics
BEGIN
    DECLARE @bid_64 BIGINT = 80;
    DECLARE @sid_64 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_64 AND seller_id = @sid_64)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_64, @sid_64, N'Principles of Macroeconomics', N'N. Gregory Mankiw', N'Cengage Learning', N'6th', '978-0538453066', 'UEB202',
            N'Kinh tế - Quản trị', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 65000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780538453066-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_64 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_64, @pid_64, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-065', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_64, @pid_64, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-065', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 66: 978-0135191798 - Management Information Systems
BEGIN
    DECLARE @bid_65 BIGINT = 86;
    DECLARE @sid_65 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_65 AND seller_id = @sid_65)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_65, @sid_65, N'Management Information Systems', N'Kenneth C. Laudon, Jane P. Laudon', N'Pearson', N'16th', '978-0135191798', 'MIS2001',
            N'Kinh tế - Quản trị', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 145000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780135191798-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_65 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_65, @pid_65, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-066', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_65, @pid_65, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-066', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 67: 978-0393123968 - Intermediate Microeconomics
BEGIN
    DECLARE @bid_66 BIGINT = 87;
    DECLARE @sid_66 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'vnu.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_66 AND seller_id = @sid_66)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_66, @sid_66, N'Intermediate Microeconomics', N'Hal R. Varian', N'W. W. Norton & Company', N'9th', '978-0393123968', 'UEB201',
            N'Kinh tế - Quản trị', N'VNU Hòa Lạc (Đô thị ĐHQGHN)', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780393123968-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_66 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_66, @pid_66, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-067', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_66, @pid_66, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-067', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 68: 978-6049221111 - Giáo trình Nguyên lý kế toán (
BEGIN
    DECLARE @bid_67 BIGINT = 17;
    DECLARE @sid_67 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_67 AND seller_id = @sid_67)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_67, @sid_67, N'Giáo trình Nguyên lý kế toán (Học viện Tài chính)', N'GS.TS. Ngô Thế Chi, PGS.TS. Trương Thị Thủy', N'NXB Tài chính', N'2021', '978-6049221111', 'ACC101',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 130000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781118875056-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            1, DATEADD(day, 7, GETDATE()), GETDATE(), GETDATE()
        );
        
        DECLARE @pid_67 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 1 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_67, @pid_67, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-068', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_67, @pid_67, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-068', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 69: 978-6049222222 - Giáo trình Kế toán tài chính d
BEGIN
    DECLARE @bid_68 BIGINT = 18;
    DECLARE @sid_68 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_68 AND seller_id = @sid_68)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_68, @sid_68, N'Giáo trình Kế toán tài chính doanh nghiệp (Tập 1)', N'Học viện Tài chính - Bộ môn Kế toán doanh nghiệp', N'NXB Tài chính', N'2022', '978-6049222222', 'ACC201',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 110000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781119503408-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            1, DATEADD(day, 7, GETDATE()), GETDATE(), GETDATE()
        );
        
        DECLARE @pid_68 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 1 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_68, @pid_68, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-069', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_68, @pid_68, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-069', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 70: 978-6049223333 - Giáo trình Tài chính doanh ngh
BEGIN
    DECLARE @bid_69 BIGINT = 19;
    DECLARE @sid_69 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_69 AND seller_id = @sid_69)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_69, @sid_69, N'Giáo trình Tài chính doanh nghiệp (Học viện Tài chính)', N'PGS.TS. Bùi Văn Vần, PGS.TS. Vũ Văn Ninh', N'NXB Tài chính', N'2021', '978-6049223333', 'FIN201',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 90000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781259918940-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_69 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_69, @pid_69, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-070', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_69, @pid_69, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-070', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 71: 978-6049224444 - Giáo trình Lý thuyết Tài chính
BEGIN
    DECLARE @bid_70 BIGINT = 20;
    DECLARE @sid_70 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_70 AND seller_id = @sid_70)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_70, @sid_70, N'Giáo trình Lý thuyết Tài chính - Tiền tệ', N'PGS.TS. Bạch Đức Hiển', N'NXB Tài chính', N'2020', '978-6049224444', 'FIN101',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 120000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780133836790-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_70 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_70, @pid_70, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-071', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_70, @pid_70, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-071', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 72: 978-6049225555 - Giáo trình Kiểm toán căn bản
BEGIN
    DECLARE @bid_71 BIGINT = 21;
    DECLARE @sid_71 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_71 AND seller_id = @sid_71)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_71, @sid_71, N'Giáo trình Kiểm toán căn bản', N'PGS.TS. Nguyễn Viết Lợi', N'NXB Tài chính', N'2021', '978-6049225555', 'AUD301',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 60000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134065823-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_71 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_71, @pid_71, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-072', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_71, @pid_71, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-072', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 73: 978-6049226666 - Giáo trình Thuế và Quản lý thu
BEGIN
    DECLARE @bid_72 BIGINT = 22;
    DECLARE @sid_72 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_72 AND seller_id = @sid_72)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_72, @sid_72, N'Giáo trình Thuế và Quản lý thuế', N'Học viện Tài chính - Khoa Thuế & Hải quan', N'NXB Tài chính', N'2022', '978-6049226666', 'TAX201',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 140000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780073526874-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_72 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_72, @pid_72, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-073', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_72, @pid_72, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-073', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 74: 978-0133856460 - Marketing Management
BEGIN
    DECLARE @bid_73 BIGINT = 77;
    DECLARE @sid_73 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_73 AND seller_id = @sid_73)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_73, @sid_73, N'Marketing Management', N'Philip Kotler, Kevin Lane Keller', N'Pearson', N'15th', '978-0133856460', 'MKT101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 105000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780133856460-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_73 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_73, @pid_73, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-074', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_73, @pid_73, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-074', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 75: 978-0132544368 - Consumer Behavior
BEGIN
    DECLARE @bid_74 BIGINT = 78;
    DECLARE @sid_74 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_74 AND seller_id = @sid_74)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_74, @sid_74, N'Consumer Behavior', N'Leon G. Schiffman, Joseph Wisenblit', N'Pearson', N'11th', '978-0132544368', 'MKT101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 90000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780132544368-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_74 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_74, @pid_74, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-075', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_74, @pid_74, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-075', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 76: 978-0134527604 - Management
BEGIN
    DECLARE @bid_75 BIGINT = 81;
    DECLARE @sid_75 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_75 AND seller_id = @sid_75)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_75, @sid_75, N'Management', N'Stephen P. Robbins, Mary A. Coulter', N'Pearson', N'14th', '978-0134527604', 'MGT101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134527604-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_75 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_75, @pid_75, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-076', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_75, @pid_75, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-076', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 77: 978-0134103983 - Organizational Behavior
BEGIN
    DECLARE @bid_76 BIGINT = 82;
    DECLARE @sid_76 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_76 AND seller_id = @sid_76)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_76, @sid_76, N'Organizational Behavior', N'Stephen P. Robbins, Timothy A. Judge', N'Pearson', N'17th', '978-0134103983', 'HRM101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 65000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134103983-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_76 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_76, @pid_76, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-077', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_76, @pid_76, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-077', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 78: 978-0134235455 - Human Resource Management
BEGIN
    DECLARE @bid_77 BIGINT = 83;
    DECLARE @sid_77 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_77 AND seller_id = @sid_77)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_77, @sid_77, N'Human Resource Management', N'Gary Dessler', N'Pearson', N'15th', '978-0134235455', 'HRM101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 145000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134235455-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_77 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_77, @pid_77, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-078', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_77, @pid_77, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-078', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 79: 978-0132664257 - Strategic Brand Management
BEGIN
    DECLARE @bid_78 BIGINT = 84;
    DECLARE @sid_78 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_78 AND seller_id = @sid_78)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_78, @sid_78, N'Strategic Brand Management', N'Kevin Lane Keller', N'Pearson', N'4th', '978-0132664257', 'MKT101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 105000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780132664257-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_78 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_78, @pid_78, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-079', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_78, @pid_78, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-079', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 80: 978-1292077611 - Digital Marketing
BEGIN
    DECLARE @bid_79 BIGINT = 85;
    DECLARE @sid_79 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_79 AND seller_id = @sid_79)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_79, @sid_79, N'Digital Marketing', N'Dave Chaffey, Fiona Ellis-Chadwick', N'Pearson', N'6th', '978-1292077611', 'MKT101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 75000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781292077611-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_79 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_79, @pid_79, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-080', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_79, @pid_79, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-080', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 81: 978-0134472119 - Macroeconomics
BEGIN
    DECLARE @bid_80 BIGINT = 88;
    DECLARE @sid_80 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_80 AND seller_id = @sid_80)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_80, @sid_80, N'Macroeconomics', N'Olivier Blanchard', N'Pearson', N'7th', '978-0134472119', 'ECO103',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134472119-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_80 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_80, @pid_80, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-081', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_80, @pid_80, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-081', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 82: 978-0138013882 - Economic Development
BEGIN
    DECLARE @bid_81 BIGINT = 89;
    DECLARE @sid_81 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_81 AND seller_id = @sid_81)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_81, @sid_81, N'Economic Development', N'Michael P. Todaro, Stephen C. Smith', N'Pearson', N'11th', '978-0138013882', 'ECO301',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 60000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780138013882-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_81 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_81, @pid_81, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-082', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_81, @pid_81, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-082', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 83: 978-0134153971 - Strategic Management: Concepts
BEGIN
    DECLARE @bid_82 BIGINT = 90;
    DECLARE @sid_82 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_82 AND seller_id = @sid_82)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_82, @sid_82, N'Strategic Management: Concepts and Cases', N'Fred R. David, Forest R. David', N'Pearson', N'16th', '978-0134153971', 'MGT101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 155000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134153971-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_82 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_82, @pid_82, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-083', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_82, @pid_82, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-083', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 84: 978-0134519579 - International Economics: Theor
BEGIN
    DECLARE @bid_83 BIGINT = 91;
    DECLARE @sid_83 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller1@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_83 AND seller_id = @sid_83)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_83, @sid_83, N'International Economics: Theory and Policy', N'Paul R. Krugman, Maurice Obstfeld, Marc Melitz', N'Pearson', N'11th', '978-0134519579', 'FIN305',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 105000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134519579-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_83 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_83, @pid_83, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-084', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_83, @pid_83, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-084', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 85: 978-1305270107 - Introductory Econometrics: A M
BEGIN
    DECLARE @bid_84 BIGINT = 92;
    DECLARE @sid_84 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_84 AND seller_id = @sid_84)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_84, @sid_84, N'Introductory Econometrics: A Modern Approach', N'Jeffrey M. Wooldridge', N'Cengage Learning', N'6th', '978-1305270107', 'ECO101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 80000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781305270107-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_84 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_84, @pid_84, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-085', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_84, @pid_84, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-085', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 86: 978-0073375779 - Basic Econometrics
BEGIN
    DECLARE @bid_85 BIGINT = 93;
    DECLARE @sid_85 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_85 AND seller_id = @sid_85)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_85, @sid_85, N'Basic Econometrics', N'Damodar N. Gujarati, Dawn C. Porter', N'McGraw-Hill', N'5th', '978-0073375779', 'ECO101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 115000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780073375779-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_85 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_85, @pid_85, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-086', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_85, @pid_85, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-086', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 87: 978-0307887894 - The Lean Startup
BEGIN
    DECLARE @bid_86 BIGINT = 94;
    DECLARE @sid_86 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_86 AND seller_id = @sid_86)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_86, @sid_86, N'The Lean Startup', N'Eric Ries', N'Crown Business', N'1st', '978-0307887894', 'ENT101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 55000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780307887894-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_86 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_86, @pid_86, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-087', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_86, @pid_86, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-087', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 88: 978-0804139298 - Zero to One
BEGIN
    DECLARE @bid_87 BIGINT = 95;
    DECLARE @sid_87 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_87 AND seller_id = @sid_87)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_87, @sid_87, N'Zero to One', N'Peter Thiel, Blake Masters', N'Crown Business', N'1st', '978-0804139298', 'ENT101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 125000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780804139298-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_87 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_87, @pid_87, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-088', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_87, @pid_87, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-088', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 89: 978-0066620992 - Good to Great
BEGIN
    DECLARE @bid_88 BIGINT = 96;
    DECLARE @sid_88 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_88 AND seller_id = @sid_88)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_88, @sid_88, N'Good to Great', N'Jim Collins', N'HarperBusiness', N'1st', '978-0066620992', 'MGT101',
            N'Kinh tế - Quản trị', N'Học viện Tài chính - Cơ sở Hòa Lạc', 105000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780066620992-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_88 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_88, @pid_88, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-089', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_88, @pid_88, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-089', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 90: 978-0134127620 - Financial Accounting
BEGIN
    DECLARE @bid_89 BIGINT = 99;
    DECLARE @sid_89 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_89 AND seller_id = @sid_89)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_89, @sid_89, N'Financial Accounting', N'Walter T. Harrison, Charles T. Horngren, C. William Thomas, Wendy M. Tietz', N'Pearson', N'11th', '978-0134127620', 'ACC101',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 90000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134127620-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_89 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_89, @pid_89, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-090', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_89, @pid_89, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-090', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 91: 978-0078111044 - Financial & Managerial Account
BEGIN
    DECLARE @bid_90 BIGINT = 100;
    DECLARE @sid_90 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_90 AND seller_id = @sid_90)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_90, @sid_90, N'Financial & Managerial Accounting', N'Jan Williams, Susan Haka, Mark Bettner, Joseph Carcello', N'McGraw-Hill', N'16th', '978-0078111044', 'ACC101',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 120000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780078111044-L.jpg', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_90 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_90, @pid_90, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-091', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_90, @pid_90, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-091', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 92: 978-1119503668 - Intermediate Accounting
BEGIN
    DECLARE @bid_91 BIGINT = 101;
    DECLARE @sid_91 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_91 AND seller_id = @sid_91)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_91, @sid_91, N'Intermediate Accounting', N'Donald E. Kieso, Jerry J. Weygandt, Terry D. Warfield', N'Wiley', N'17th', '978-1119503668', 'ACC101',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 70000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9781119503668-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_91 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_91, @pid_91, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-092', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_91, @pid_91, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-092', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 93: 978-0078110917 - Advanced Accounting
BEGIN
    DECLARE @bid_92 BIGINT = 102;
    DECLARE @sid_92 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_92 AND seller_id = @sid_92)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_92, @sid_92, N'Advanced Accounting', N'Joe Ben Hoyle, Thomas Schaefer, Timothy Doupnik', N'McGraw-Hill', N'10th', '978-0078110917', 'ACC202',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 140000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780078110917-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_92 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_92, @pid_92, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-093', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_92, @pid_92, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-093', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 94: 978-0078025631 - Managerial Accounting
BEGIN
    DECLARE @bid_93 BIGINT = 103;
    DECLARE @sid_93 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_93 AND seller_id = @sid_93)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_93, @sid_93, N'Managerial Accounting', N'Ray H. Garrison, Eric W. Noreen, Peter C. Brewer', N'McGraw-Hill', N'15th', '978-0078025631', 'ACC203',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 110000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780078025631-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_93 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_93, @pid_93, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-094', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_93, @pid_93, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-094', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 95: 978-0134475585 - Cost Accounting: A Managerial 
BEGIN
    DECLARE @bid_94 BIGINT = 104;
    DECLARE @sid_94 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_94 AND seller_id = @sid_94)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_94, @sid_94, N'Cost Accounting: A Managerial Emphasis', N'Srikant M. Datar, Madhav V. Rajan', N'Pearson', N'16th', '978-0134475585', 'ACC203',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 95000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780134475585-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_94 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_94, @pid_94, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-095', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_94, @pid_94, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-095', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 96: 978-0134065960 - Auditing and Assurance Service
BEGIN
    DECLARE @bid_95 BIGINT = 105;
    DECLARE @sid_95 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_95 AND seller_id = @sid_95)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_95, @sid_95, N'Auditing and Assurance Services', N'Alvin A. Arens, Randal J. Elder, Mark S. Beasley, Chris E. Hogan', N'Pearson', N'16th', '978-0134065960', 'AUD301',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 120000, 'LIKE_NEW', 'AVAILABLE', 'https://books.google.com/books/content?id=4TAKswEACAAJ&printsec=frontcover&img=1&zoom=1', N'Sách còn rất mới, giấy sáng, đầy đủ tất cả phụ lục và bài tập thực hành.', N'Mình hoàn thành môn học điểm A nên pass lại cho các bạn khóa dưới lấy vía. Hỗ trợ giao trực tiếp tại khuôn viên trường.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_95 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_95, @pid_95, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-096', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_95, @pid_95, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-096', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 97: 978-0078025440 - Principles of Auditing & Other
BEGIN
    DECLARE @bid_96 BIGINT = 106;
    DECLARE @sid_96 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_96 AND seller_id = @sid_96)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_96, @sid_96, N'Principles of Auditing & Other Assurance Services', N'Ray Whittington, Kurt Pany', N'McGraw-Hill', N'19th', '978-0078025440', 'AUD301',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 65000, 'ACCEPTABLE', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780078025440-L.jpg', N'Gáy sách hơi sờn nhẹ theo thời gian, có ghi chú bút chì bài tập một số trang, tẩy sạch được.', N'Sách học một kỳ đầy đủ các chương, không rách rời. Pass giá rẻ hỗ trợ sinh viên tiết kiệm chi phí học tập.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_96 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_96, @pid_96, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-097', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_96, @pid_96, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-097', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 98: 978-0471230113 - Modern Auditing
BEGIN
    DECLARE @bid_97 BIGINT = 107;
    DECLARE @sid_97 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_97 AND seller_id = @sid_97)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_97, @sid_97, N'Modern Auditing', N'William C. Boynton, Raymond N. Johnson', N'Wiley', N'8th', '978-0471230113', 'AUD302',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 150000, 'NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780471230113-L.jpg', N'Sách mới 100% chưa qua sử dụng, còn nguyên mùi giấy mới, không tì vết.', N'Mình mua dư một cuốn đầu kỳ nhưng chuyển nhóm môn nên không dùng tới. Pass lại nguyên giá bìa giảm sâu cho bạn nào cần.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_97 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_97, @pid_97, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-098', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_97, @pid_97, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-098', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 99: 978-0133836790 - The Economics of Money, Bankin
BEGIN
    DECLARE @bid_98 BIGINT = 108;
    DECLARE @sid_98 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_98 AND seller_id = @sid_98)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_98, @sid_98, N'The Economics of Money, Banking, and Financial Markets', N'Frederic S. Mishkin', N'Pearson', N'11th', '978-0133836790', 'FIN101',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 110000, 'LIKE_NEW', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780133836790-L.jpg', N'Sách đẹp 98%, không quăn mép, gáy sách chắc chắn, không có vết mực bẩn.', N'Sách chỉ dùng lướt qua một kỳ thi môn, giữ gìn rất kỹ trong bọc nilon. Nội dung bám sát bài giảng trên lớp.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_98 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_98, @pid_98, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-099', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_98, @pid_98, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-099', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

-- Listing 100: 978-0133423624 - Financial Markets and Institut
BEGIN
    DECLARE @bid_99 BIGINT = 109;
    DECLARE @sid_99 BIGINT = (SELECT TOP 1 id FROM users WHERE email = 'aof.seller2@bookloop.vn');
    
    IF NOT EXISTS (SELECT 1 FROM products WHERE academic_book_id = @bid_99 AND seller_id = @sid_99)
    BEGIN
        INSERT INTO products (
            academic_book_id, seller_id, title, author, publisher, edition, isbn, course_code,
            category, campus, price, condition_grade, status, image_url, defect_notes, description,
            is_promoted, promoted_until, created_at, updated_at
        ) VALUES (
            @bid_99, @sid_99, N'Financial Markets and Institutions', N'Frederic S. Mishkin, Stanley Eakins', N'Pearson', N'8th', '978-0133423624', 'FIN101',
            N'Kế toán - Tài chính', N'Học viện Tài chính - Cơ sở Hòa Lạc', 80000, 'GOOD', 'AVAILABLE', 'https://covers.openlibrary.org/b/isbn/9780133423624-L.jpg', N'Có highlight vài trang lý thuyết trọng tâm chương 2 và 3, bìa bọc cẩn thận.', N'Giáo trình học kỳ trước mình học, có đánh dấu các phần thầy cô dặn thi. Tặng kèm tài liệu tổng hợp kiến thức môn học.',
            0, NULL, GETDATE(), GETDATE()
        );
        
        DECLARE @pid_99 BIGINT = SCOPE_IDENTITY();
        
        -- Create Promotion Payment record
        IF 0 = 1
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, admin_note, verified_by_id, created_at, verified_at
            ) VALUES (
                @sid_99, @pid_99, 15000.00, 7, 'ADMIN_DEMO_WAIVER', 'PAID',
                'DEMO-PRM-100', N'Phí đăng bài mẫu phục vụ kiểm thử môi trường phát triển',
                N'DEMO_ADMIN_WAIVER: Phê duyệt hiển thị mẫu cho môi trường phát triển (Demo Seed)',
                4, GETDATE(), GETDATE()
            );
        END
        ELSE
        BEGIN
            INSERT INTO promotion_payments (
                seller_id, product_id, fee_amount, duration_days, payment_method, status,
                transaction_reference, payment_notes, created_at
            ) VALUES (
                @sid_99, @pid_99, 15000.00, 7, 'BANK_TRANSFER', 'PENDING',
                'DEMO-PRM-100', N'Chờ thanh toán phí quảng cáo', GETDATE()
            );
        END
    END
END;

PRINT '=== VERIFICATION SUMMARY ===';
SELECT 
    COUNT(*) as total_marketplace_listings,
    SUM(CASE WHEN academic_book_id IS NOT NULL THEN 1 ELSE 0 END) as linked_to_academic_books,
    COUNT(DISTINCT academic_book_id) as distinct_academic_books,
    SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) as available_listings,
    SUM(CASE WHEN is_promoted = 1 THEN 1 ELSE 0 END) as promoted_listings,
    MIN(price) as min_price,
    MAX(price) as max_price,
    AVG(price) as avg_price
FROM products
WHERE seller_id IN (SELECT id FROM users WHERE email LIKE '%@bookloop.vn');

PRINT '=== DISTRIBUTION BY CAMPUS ===';
SELECT campus, COUNT(*) as count_by_campus
FROM products
WHERE seller_id IN (SELECT id FROM users WHERE email LIKE '%@bookloop.vn')
GROUP BY campus;

PRINT '=== DISTRIBUTION BY CONDITION ===';
SELECT condition_grade, COUNT(*) as count_by_condition
FROM products
WHERE seller_id IN (SELECT id FROM users WHERE email LIKE '%@bookloop.vn')
GROUP BY condition_grade;

PRINT '=== PROMOTION PAYMENTS SUMMARY ===';
SELECT status, payment_method, COUNT(*) as payment_count
FROM promotion_payments
WHERE transaction_reference LIKE 'DEMO-PRM-%'
GROUP BY status, payment_method;
