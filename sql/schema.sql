-- =========================================================================
-- BookLoop — SQL Server Database Schema (Phase 3, Phase 4 & Phase 5)
-- =========================================================================
-- This script creates the BookLoopDB database and all required tables:
-- - users (authentication & profiles, enabled status)
-- - products (book listings, moderation notes)
-- - carts & cart_items (shopping cart)
-- - orders & order_items (orders & historical transaction snapshots)
-- - reviews (buyer ratings, feedback, hidden status)
-- - reports (user listing/seller reports)
-- - admin_logs (admin activity audit trail)
--
-- Safe to run multiple times (idempotent with IF NOT EXISTS checks).
-- =========================================================================

-- 1. Create the database (skip if it already exists)
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'BookLoopDB')
BEGIN
    CREATE DATABASE BookLoopDB;
END
GO

USE BookLoopDB;
GO

-- =========================================================================
-- 2. Users table
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'users') AND type = 'U')
BEGIN
    CREATE TABLE users (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        full_name       NVARCHAR(100)   NOT NULL,
        email           NVARCHAR(150)   NOT NULL,
        password        NVARCHAR(255)   NOT NULL,
        university      NVARCHAR(150)   NULL,
        campus          NVARCHAR(100)   NULL,
        phone           NVARCHAR(20)    NULL,
        avatar_url      NVARCHAR(500)   NULL,
        role            VARCHAR(20)     NOT NULL DEFAULT 'STUDENT',
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),
        updated_at      DATETIME2       NULL,

        CONSTRAINT UQ_users_email UNIQUE (email)
    );

    CREATE NONCLUSTERED INDEX IX_users_email ON users (email);
END
GO

-- =========================================================================
-- 3. Products table (book listings)
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'products') AND type = 'U')
BEGIN
    CREATE TABLE products (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        title           NVARCHAR(255)   NOT NULL,
        description     NVARCHAR(3000)  NULL,
        price           DECIMAL(12, 2)  NOT NULL,
        condition_grade VARCHAR(20)     NOT NULL,
        category        NVARCHAR(100)   NULL,
        author          NVARCHAR(150)   NULL,
        publisher       NVARCHAR(150)   NULL,
        edition         NVARCHAR(50)    NULL,
        isbn            VARCHAR(20)     NULL,
        course_code     VARCHAR(20)     NULL,
        image_url       NVARCHAR(500)   NULL,
        defect_notes    NVARCHAR(1000)  NULL,
        campus          NVARCHAR(100)   NOT NULL,
        status          VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),
        updated_at      DATETIME2       NULL,
        seller_id       BIGINT          NOT NULL,

        CONSTRAINT FK_products_seller FOREIGN KEY (seller_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

        CONSTRAINT CK_products_price CHECK (price >= 0),
        CONSTRAINT CK_products_condition CHECK (condition_grade IN ('NEW','LIKE_NEW','GOOD','ACCEPTABLE')),
        CONSTRAINT CK_products_status CHECK (status IN ('AVAILABLE','SOLD','HIDDEN'))
    );

    CREATE NONCLUSTERED INDEX IX_products_status    ON products (status);
    CREATE NONCLUSTERED INDEX IX_products_seller    ON products (seller_id);
    CREATE NONCLUSTERED INDEX IX_products_category  ON products (category);
    CREATE NONCLUSTERED INDEX IX_products_campus    ON products (campus);
    CREATE NONCLUSTERED INDEX IX_products_created   ON products (created_at DESC);
END
GO

-- =========================================================================
-- 4. Carts table
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'carts') AND type = 'U')
BEGIN
    CREATE TABLE carts (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        user_id         BIGINT          NOT NULL,
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),
        updated_at      DATETIME2       NULL,

        CONSTRAINT FK_carts_user FOREIGN KEY (user_id)
            REFERENCES users (id) ON DELETE CASCADE,
        CONSTRAINT UQ_carts_user UNIQUE (user_id)
    );
END
GO

-- =========================================================================
-- 5. Cart Items table
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'cart_items') AND type = 'U')
BEGIN
    CREATE TABLE cart_items (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        cart_id         BIGINT          NOT NULL,
        product_id      BIGINT          NOT NULL,
        quantity        INT             NOT NULL DEFAULT 1,
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),

        CONSTRAINT FK_cart_items_cart FOREIGN KEY (cart_id)
            REFERENCES carts (id) ON DELETE CASCADE,
        CONSTRAINT FK_cart_items_product FOREIGN KEY (product_id)
            REFERENCES products (id) ON DELETE CASCADE,
        CONSTRAINT UQ_cart_items_product UNIQUE (cart_id, product_id),
        CONSTRAINT CK_cart_items_quantity CHECK (quantity >= 1)
    );

    CREATE NONCLUSTERED INDEX IX_cart_items_cart ON cart_items (cart_id);
END
GO

-- =========================================================================
-- 6. Orders table
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'orders') AND type = 'U')
BEGIN
    CREATE TABLE orders (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        order_number    VARCHAR(32)     NOT NULL,
        buyer_id        BIGINT          NOT NULL,
        order_date      DATETIME2       NOT NULL DEFAULT GETDATE(),
        total_amount    DECIMAL(12, 2)  NOT NULL,
        payment_method  VARCHAR(30)     NOT NULL,
        status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
        meetup_note     NVARCHAR(1000)  NULL,
        contact_phone   VARCHAR(20)     NULL,
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),
        updated_at      DATETIME2       NULL,

        CONSTRAINT UQ_orders_number UNIQUE (order_number),
        CONSTRAINT FK_orders_buyer FOREIGN KEY (buyer_id)
            REFERENCES users (id),
        CONSTRAINT CK_orders_total CHECK (total_amount >= 0),
        CONSTRAINT CK_orders_status CHECK (status IN ('PENDING', 'AWAITING_PAYMENT', 'PAID_HELD', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'DISPUTED', 'REFUNDED', 'COMPLETED', 'CANCELLED')),
        CONSTRAINT CK_orders_payment CHECK (payment_method IN ('IN_PERSON_MEETUP', 'BANK_TRANSFER', 'MARKETPLACE_HOLDING'))
    );

    CREATE NONCLUSTERED INDEX IX_orders_buyer  ON orders (buyer_id);
    CREATE NONCLUSTERED INDEX IX_orders_status ON orders (status);
    CREATE NONCLUSTERED INDEX IX_orders_date   ON orders (order_date DESC);
END
GO

-- =========================================================================
-- 7. Order Items table (Historical snapshots)
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'order_items') AND type = 'U')
BEGIN
    CREATE TABLE order_items (
        id                 BIGINT          IDENTITY(1,1) PRIMARY KEY,
        order_id           BIGINT          NOT NULL,
        product_id         BIGINT          NULL,
        seller_id          BIGINT          NOT NULL,
        book_title         NVARCHAR(255)   NOT NULL,
        book_author        NVARCHAR(150)   NULL,
        book_image_url     NVARCHAR(500)   NULL,
        price_at_purchase  DECIMAL(12, 2)  NOT NULL,
        quantity           INT             NOT NULL DEFAULT 1,

        CONSTRAINT FK_order_items_order FOREIGN KEY (order_id)
            REFERENCES orders (id) ON DELETE CASCADE,
        CONSTRAINT FK_order_items_product FOREIGN KEY (product_id)
            REFERENCES products (id) ON DELETE SET NULL,
        CONSTRAINT FK_order_items_seller FOREIGN KEY (seller_id)
            REFERENCES users (id),
        CONSTRAINT CK_order_items_price CHECK (price_at_purchase >= 0),
        CONSTRAINT CK_order_items_quantity CHECK (quantity >= 1)
    );

    CREATE NONCLUSTERED INDEX IX_order_items_order  ON order_items (order_id);
    CREATE NONCLUSTERED INDEX IX_order_items_seller ON order_items (seller_id);
END
GO

-- =========================================================================
-- 8. Reviews table
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'reviews') AND type = 'U')
BEGIN
    CREATE TABLE reviews (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        order_id        BIGINT          NOT NULL,
        buyer_id        BIGINT          NOT NULL,
        seller_id       BIGINT          NOT NULL,
        rating          INT             NOT NULL,
        comment         NVARCHAR(2000)  NULL,
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),

        CONSTRAINT FK_reviews_order FOREIGN KEY (order_id)
            REFERENCES orders (id) ON DELETE CASCADE,
        CONSTRAINT FK_reviews_buyer FOREIGN KEY (buyer_id)
            REFERENCES users (id),
        CONSTRAINT FK_reviews_seller FOREIGN KEY (seller_id)
            REFERENCES users (id),
        CONSTRAINT UQ_reviews_order_seller UNIQUE (order_id, seller_id),
        CONSTRAINT CK_reviews_rating CHECK (rating >= 1 AND rating <= 5)
    );

    CREATE NONCLUSTERED INDEX IX_reviews_seller ON reviews (seller_id);
    CREATE NONCLUSTERED INDEX IX_reviews_buyer  ON reviews (buyer_id);
END
GO

-- =========================================================================
-- Phase 5 Schema Migrations for existing tables
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'users') AND name = 'enabled')
BEGIN
    ALTER TABLE users ADD enabled BIT NOT NULL DEFAULT 1;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'products') AND name = 'moderation_note')
BEGIN
    ALTER TABLE products ADD moderation_note NVARCHAR(1000) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'reviews') AND name = 'hidden')
BEGIN
    ALTER TABLE reviews ADD hidden BIT NOT NULL DEFAULT 0;
END
GO

-- =========================================================================
-- 9. Reports table (Phase 5)
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'reports') AND type = 'U')
BEGIN
    CREATE TABLE reports (
        id                  BIGINT          IDENTITY(1,1) PRIMARY KEY,
        reporter_id         BIGINT          NOT NULL,
        reported_product_id BIGINT          NULL,
        reported_user_id    BIGINT          NULL,
        reason              NVARCHAR(200)   NOT NULL,
        description         NVARCHAR(2000)  NULL,
        status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
        admin_note          NVARCHAR(1000)  NULL,
        handled_by_id       BIGINT          NULL,
        created_at          DATETIME2       NOT NULL DEFAULT GETDATE(),
        updated_at          DATETIME2       NULL,

        CONSTRAINT FK_reports_reporter FOREIGN KEY (reporter_id)
            REFERENCES users (id),
        CONSTRAINT FK_reports_product FOREIGN KEY (reported_product_id)
            REFERENCES products (id) ON DELETE SET NULL,
        CONSTRAINT FK_reports_user FOREIGN KEY (reported_user_id)
            REFERENCES users (id),
        CONSTRAINT FK_reports_admin FOREIGN KEY (handled_by_id)
            REFERENCES users (id),
        CONSTRAINT CK_reports_status CHECK (status IN ('PENDING','UNDER_REVIEW','RESOLVED','REJECTED'))
    );

    CREATE NONCLUSTERED INDEX IX_reports_status   ON reports (status);
    CREATE NONCLUSTERED INDEX IX_reports_reporter ON reports (reporter_id);
    CREATE NONCLUSTERED INDEX IX_reports_product  ON reports (reported_product_id);
END
GO

-- =========================================================================
-- 10. Admin Logs table (Phase 5)
-- =========================================================================
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'admin_logs') AND type = 'U')
BEGIN
    CREATE TABLE admin_logs (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        admin_id        BIGINT          NOT NULL,
        action          VARCHAR(100)    NOT NULL,
        target_type     VARCHAR(50)     NULL,
        target_id       BIGINT          NULL,
        description     NVARCHAR(1000)  NULL,
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE(),

        CONSTRAINT FK_admin_logs_admin FOREIGN KEY (admin_id)
            REFERENCES users (id)
    );

    CREATE NONCLUSTERED INDEX IX_admin_logs_admin   ON admin_logs (admin_id);
    CREATE NONCLUSTERED INDEX IX_admin_logs_created ON admin_logs (created_at DESC);
END
GO

-- =========================================================================
-- Product Enhancement: Academic Textbook Catalog & Paid Promotion
-- =========================================================================

-- Products table migration
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'products') AND name = 'is_promoted')
BEGIN
    ALTER TABLE products ADD is_promoted BIT NOT NULL DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'products') AND name = 'promoted_until')
BEGIN
    ALTER TABLE products ADD promoted_until DATETIME2 NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'products') AND name = 'academic_book_id')
BEGIN
    ALTER TABLE products ADD academic_book_id BIGINT NULL;
END
GO

-- 11. Institutions table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'institutions') AND type = 'U')
BEGIN
    CREATE TABLE institutions (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        name            NVARCHAR(200)   NOT NULL,
        short_name      NVARCHAR(50)    NOT NULL UNIQUE,
        description     NVARCHAR(2000)  NULL,
        website         NVARCHAR(300)   NULL,
        logo_url        NVARCHAR(500)   NULL,
        is_active       BIT             NOT NULL DEFAULT 1,
        created_at      DATETIME2       NOT NULL DEFAULT GETDATE()
    );
    CREATE NONCLUSTERED INDEX IX_institutions_code ON institutions (short_name);
END
GO

-- 12. Campuses table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'campuses') AND type = 'U')
BEGIN
    CREATE TABLE campuses (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        institution_id  BIGINT          NOT NULL,
        name            NVARCHAR(150)   NOT NULL,
        location        NVARCHAR(255)   NULL,
        is_hoa_lac      BIT             NOT NULL DEFAULT 1,
        CONSTRAINT FK_campuses_inst FOREIGN KEY (institution_id) REFERENCES institutions (id) ON DELETE CASCADE
    );
    CREATE NONCLUSTERED INDEX IX_campuses_inst ON campuses (institution_id);
END
GO

-- 13. Academic Programs table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'academic_programs') AND type = 'U')
BEGIN
    CREATE TABLE academic_programs (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        institution_id  BIGINT          NOT NULL,
        campus_id       BIGINT          NULL,
        faculty         NVARCHAR(150)   NULL,
        name            NVARCHAR(200)   NOT NULL,
        major           NVARCHAR(100)   NULL,
        program_code    NVARCHAR(50)    NULL,
        source_url      NVARCHAR(500)   NULL,
        CONSTRAINT FK_programs_inst FOREIGN KEY (institution_id) REFERENCES institutions (id) ON DELETE CASCADE,
        CONSTRAINT FK_programs_campus FOREIGN KEY (campus_id) REFERENCES campuses (id)
    );
    CREATE NONCLUSTERED INDEX IX_programs_inst ON academic_programs (institution_id);
END
GO

-- 14. Courses table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'courses') AND type = 'U')
BEGIN
    CREATE TABLE courses (
        id              BIGINT          IDENTITY(1,1) PRIMARY KEY,
        program_id      BIGINT          NOT NULL,
        course_code     NVARCHAR(50)    NULL,
        name            NVARCHAR(200)   NOT NULL,
        description     NVARCHAR(2000)  NULL,
        semester        INT             NULL,
        credits         INT             NULL,
        is_mandatory    BIT             NULL,
        CONSTRAINT FK_courses_prog FOREIGN KEY (program_id) REFERENCES academic_programs (id) ON DELETE CASCADE
    );
    CREATE NONCLUSTERED INDEX IX_courses_prog ON courses (program_id);
    CREATE NONCLUSTERED INDEX IX_courses_code ON courses (course_code);
END
GO

-- 15. Academic Books table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'academic_books') AND type = 'U')
BEGIN
    CREATE TABLE academic_books (
        id                  BIGINT          IDENTITY(1,1) PRIMARY KEY,
        isbn                NVARCHAR(30)    NULL,
        title               NVARCHAR(255)   NOT NULL,
        original_title      NVARCHAR(255)   NULL,
        author              NVARCHAR(200)   NULL,
        publisher           NVARCHAR(150)   NULL,
        edition             NVARCHAR(50)    NULL,
        publication_year    INT             NULL,
        language            NVARCHAR(50)    NULL,
        description         NVARCHAR(3000)  NULL,
        cover_image_url     NVARCHAR(500)   NULL,
        category            NVARCHAR(100)   NULL,
        book_type           NVARCHAR(50)    NULL
    );
    CREATE NONCLUSTERED INDEX IX_acad_books_isbn  ON academic_books (isbn);
    CREATE NONCLUSTERED INDEX IX_acad_books_title ON academic_books (title);
END
GO

-- 16. Course-Book relationship table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'course_books') AND type = 'U')
BEGIN
    CREATE TABLE course_books (
        id                  BIGINT          IDENTITY(1,1) PRIMARY KEY,
        course_id           BIGINT          NOT NULL,
        book_id             BIGINT          NOT NULL,
        relationship_type   VARCHAR(30)     NOT NULL DEFAULT 'SUGGESTED_REFERENCE',
        notes               NVARCHAR(1000)  NULL,
        source_url          NVARCHAR(500)   NULL,
        is_official         BIT             NOT NULL DEFAULT 0,
        CONSTRAINT FK_cb_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
        CONSTRAINT FK_cb_book FOREIGN KEY (book_id) REFERENCES academic_books (id) ON DELETE CASCADE,
        CONSTRAINT UQ_course_book UNIQUE (course_id, book_id)
    );
    CREATE NONCLUSTERED INDEX IX_cb_course ON course_books (course_id);
    CREATE NONCLUSTERED INDEX IX_cb_book   ON course_books (book_id);
END
GO

-- 17. Platform Settings table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'platform_settings') AND type = 'U')
BEGIN
    CREATE TABLE platform_settings (
        setting_key         NVARCHAR(100)   PRIMARY KEY,
        setting_value       NVARCHAR(2000)  NOT NULL,
        description         NVARCHAR(500)   NULL,
        updated_at          DATETIME2       NOT NULL DEFAULT GETDATE()
    );
END
GO

-- 18. Promotion Payments table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'promotion_payments') AND type = 'U')
BEGIN
    CREATE TABLE promotion_payments (
        id                      BIGINT          IDENTITY(1,1) PRIMARY KEY,
        seller_id               BIGINT          NOT NULL,
        product_id              BIGINT          NOT NULL,
        fee_amount              DECIMAL(12,2)   NOT NULL,
        duration_days           INT             NOT NULL DEFAULT 7,
        payment_method          VARCHAR(50)     NOT NULL DEFAULT 'BANK_TRANSFER',
        status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
        transaction_reference   NVARCHAR(100)   NULL,
        payment_notes           NVARCHAR(1000)  NULL,
        admin_note              NVARCHAR(1000)  NULL,
        verified_by_id          BIGINT          NULL,
        created_at              DATETIME2       NOT NULL DEFAULT GETDATE(),
        verified_at             DATETIME2       NULL,
        CONSTRAINT FK_prom_seller FOREIGN KEY (seller_id) REFERENCES users (id),
        CONSTRAINT FK_prom_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
        CONSTRAINT FK_prom_admin FOREIGN KEY (verified_by_id) REFERENCES users (id),
        CONSTRAINT CK_prom_status CHECK (status IN ('PENDING','PAID','REJECTED','CANCELLED'))
    );
    CREATE NONCLUSTERED INDEX IX_prom_seller ON promotion_payments (seller_id);
    CREATE NONCLUSTERED INDEX IX_prom_product ON promotion_payments (product_id);
    CREATE NONCLUSTERED INDEX IX_prom_status ON promotion_payments (status);
END
GO

PRINT 'BookLoopDB schema (Phase 3 + Phase 4 + Phase 5 + Enhancements) created successfully.';
GO
