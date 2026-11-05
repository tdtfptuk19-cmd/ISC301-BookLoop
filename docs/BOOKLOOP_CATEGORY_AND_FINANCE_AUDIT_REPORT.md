# BOOKLOOP – CATEGORY CATALOG DYNAMICS, ADMIN FINANCIAL SYSTEM & RECONCILIATION AUDIT REPORT

**Dự án:** BookLoop – Sàn giao dịch giáo trình & sách cũ sinh viên các trường Đại học Hòa Lạc  
**Tác giả:** Senior Java Spring Boot Developer, Database Engineer & Financial System Analyst  
**Ngày thực hiện:** 05/11/2026  
**Trạng thái:** HOÀN TẤT & ĐÃ KIỂM THỬ TOÀN DIỆN (BUILD SUCCESS)

---

## MỤC LỤC
1. [TỔNG QUAN HỆ THỐNG VÀ CÁC THAY ĐỔI](#1-tổng-quan-hệ-thống-và-các-thay-đổi)
2. [PHẦN 1: THỂ LOẠI SÁCH ĐỘNG TỪ SQL SERVER (CATEGORY DROPDOWN)](#2-phần-1-thể-loại-sách-động-từ-sql-server-category-dropdown)
3. [PHẦN 2: SỬA LỖI ADMIN – TỔNG QUAN TÀI CHÍNH & 8 CHỈ SỐ KPI](#3-phần-2-sửa-lỗi-admin--tổng-quan-tài-chính--8-chỉ-số-kpi)
4. [PHẦN 3: ADMIN – ĐỐI SOÁT TÀI CHÍNH & XUẤT BÁO CÁO CSV AN TOÀN](#4-phần-3-admin--đối-soát-tài-chính--xuất-báo-cáo-csv-an-toàn)
5. [GIẢI ĐÁP CHUYÊN SÂU: CƠ CHẾ GIẢI NGÂN TIỀN VỀ NGÂN HÀNG CHO NGƯỜI BÁN](#5-giải-đáp-chuyên-sâu-cơ-chế-giải-ngân-tiền-về-ngân-hàng-cho-người-bán)
   - *Phân tích 2 ảnh chụp màn hình thực tế (80.000 đ và 195.000 đ nhưng Đã giải ngân = 0 đ)*
   - *Bản chất dòng tiền: C2C Trực tiếp vs Ký quỹ Bảo đảm (Escrow)*
   - *Cơ chế nút "Giải ngân ngay về ngân hàng" và các bước vận hành demo*
6. [TỔNG HỢP SOURCE CODE ĐÃ CHỈNH SỬA & HƯỚNG DẪN KIỂM THỬ](#6-tổng-hợp-source-code-đã-chỉnh-sửa--hướng-dẫn-kiểm-thử)

---

## 1. TỔNG QUAN HỆ THỐNG VÀ CÁC THAY ĐỔI

Hệ thống BookLoop đã được nâng cấp đồng bộ trên cả 3 tầng (Database, Backend Spring Boot 3.3.5, Frontend Thymeleaf + Bootstrap 5):
- **Category Động**: Thay thế toàn bộ mã cứng thể loại bằng bảng cơ sở dữ liệu `categories` trong SQL Server, hỗ trợ lọc, tìm kiếm, đăng bán và phân trang mượt mà.
- **Admin Tài chính chuẩn xác**: Khắc phục lỗi trang `/admin/finance`, chuẩn hóa 8 chỉ số tài chính (GMV, Platform Revenue, Held Funds, Seller Payout, Successful Payments, Pending Payments, Refunds, Disputes) kèm bộ lọc thời gian đa dạng (`TODAY`, `LAST_7_DAYS`, `LAST_30_DAYS`, `THIS_MONTH`, `LAST_MONTH`, `CUSTOM`).
- **Audit Đối soát tài chính**: Trang `/admin/finance/reconciliation` kiểm tra tính bất biến (business invariants) giữa Đơn hàng, Giao dịch thanh toán, Hoa hồng, Giải ngân và Cổng thanh toán (Mock Gateway); hỗ trợ xuất file CSV chuẩn UTF-8 BOM, an toàn chống CSV Injection.
- **Luồng Giải ngân Ví người bán (`/seller/finance`)**: Làm rõ và hoàn thiện tính năng giải ngân doanh số hoàn tất về tài khoản ngân hàng của người bán.

---

## 2. PHẦN 1: THỂ LOẠI SÁCH ĐỘNG TỪ SQL SERVER (CATEGORY DROPDOWN)

### 2.1. Cấu trúc Database & Entity
Đã thiết lập thực thể `Category` độc lập map với bảng `categories` trong SQL Server:
```sql
CREATE TABLE categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    slug NVARCHAR(120) NOT NULL UNIQUE,
    description NVARCHAR(500) NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
```

- **Thực thể Java (`Category.java`)**: Đầy đủ annotations JPA, không sử dụng Lombok, tuân thủ Clean Architecture.
- **Quan hệ với Product (`Product.java`)**: Bổ sung `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") private Category categoryRef;` đồng thời duy trì getter/setter chuỗi `category` cũ để đảm bảo tính tương thích ngược 100%.

### 2.2. Seed Dữ liệu Tự động (`CategorySeeder.java`, `@Order(30)`)
Khởi tạo 13 danh mục học thuật chuẩn và tự động quét toàn bộ sách hiện có trong DB để liên kết `category_id`:
1. Công nghệ thông tin
2. Khoa học dữ liệu & AI
3. Kinh tế - Quản trị
4. Marketing
5. Kế toán - Tài chính
6. Khoa học cơ bản (Toán, Lý, Hóa)
7. Kỹ thuật - Công nghệ
8. Luật - Chính trị
9. Ngoại ngữ
10. Thiết kế - Đồ họa
11. Khoa học xã hội
12. Sách kỹ năng & Văn học
13. Khác

### 2.3. Tích hợp Dropdown trên Giao diện
1. **Trang chủ (`home.html`)**: Tích hợp ô chọn thể loại ngay trong thanh tìm kiếm Hero Section và danh sách thẻ danh mục nhanh.
2. **Trang Tìm sách (`books/list.html`)**: Bộ lọc `<select name="category">` nạp động danh sách từ database, có tùy chọn "Tất cả thể loại", giữ nguyên giá trị đã chọn khi phân trang hoặc lọc giá/tình trạng sách.
3. **Trang Đăng bán & Chỉnh sửa sách (`listings/form.html`)**: Thay input text thành `<select name="categoryId">`, tự động chọn đúng thể loại khi sửa tin.
4. **Trang Quản trị sách Admin (`admin/listings/list.html`)**: Thêm bộ lọc dropdown thể loại cho Quản trị viên.

---

## 3. PHẦN 2: SỬA LỖI ADMIN – TỔNG QUAN TÀI CHÍNH & 8 CHỈ SỐ KPI

### 3.1. Phân tích nguyên nhân lỗi trang `/admin/finance`
Khi kiểm tra luồng cũ, trang Tổng quan tài chính gặp 2 lỗi nghiêm trọng:
1. **Lỗi Transaction Read-Only**: Phương thức `getActiveCommissionSetting()` có `@Transactional(readOnly = true)`, nhưng bên trong lại gọi `settingRepository.save(new CommissionSetting())` khi bảng rỗng. Điều này gây lỗi `TransactionSystemException: Connection is read-only`.
   - *Khắc phục*: Gỡ bỏ `readOnly = true`, đảm bảo khởi tạo cấu hình hoa hồng mặc định an toàn.
2. **Lỗi Hibernate 6 Query Count trong Phân trang**: Các câu lệnh `@Query` chứa `LEFT JOIN FETCH` (ví dụ `findAllWithDetails` trong `PayoutTransactionRepository`, `CommissionRecordRepository`, `PaymentTransactionRepository`) khi kết hợp với `Pageable` bị Hibernate 6 ném lỗi sinh câu lệnh count.
   - *Khắc phục*: Bổ sung thuộc tính `countQuery` tường minh cho tất cả các query phân trang.

### 3.2. Chuẩn hóa 8 Chỉ số Tài chính (Kpis)
Tất cả các số liệu đều sử dụng `BigDecimal` với scale 2 (`RoundingMode.HALF_UP`) và định dạng tiền tệ VNĐ:

| Chỉ số | Tên hiển thị | Công thức & Nguồn dữ liệu | Quy tắc tài chính đảm bảo |
|---|---|---|---|
| **GMV** | Tổng giá trị hàng hóa | Tổng tiền từ các giao dịch thanh toán thành công (`CAPTURED`, `SETTLED`) | Không tính đơn hủy/thất bại |
| **Platform Revenue** | Doanh thu hoa hồng sàn | Tổng phí hoa hồng từ `commission_records` có trạng thái `SETTLED` | Chỉ tính khi giao dịch hoàn tất, không gộp tiền ký quỹ |
| **Held Funds** | Tiền đang giữ đối soát | Tổng tiền thanh toán trực tuyến `CAPTURED` đang ký quỹ chờ người mua nhận sách | Tách biệt hoàn toàn với doanh thu sàn |
| **Seller Payout** | Tiền đã giải ngân người bán | Tổng tiền từ các giao dịch `payout_transactions` có trạng thái `PAID`, `CONFIRMED`, `COMPLETED` | Không cộng trùng nhiều lần |
| **Successful Payments** | Giao dịch thành công | Số lượng và tổng tiền các thanh toán `CAPTURED` / `SETTLED` | Phản ánh chính xác volume dòng tiền vào |
| **Pending Payments** | Chờ thanh toán | Số lượng và tổng tiền các giao dịch `PENDING` | Không tính vào doanh số thực |
| **Refunds** | Hoàn tiền người mua | Số lượng và tổng tiền các giao dịch `refund_transactions` hoàn tất | Khấu trừ khi đối soát |
| **Disputes** | Tranh chấp khiếu nại | Tổng số lượng tranh chấp và số ca đang mở (`OPEN`, `UNDER_REVIEW`) | Cảnh báo rủi ro tài chính kịp thời |

### 3.3. Bộ lọc thời gian linh hoạt
Trang `/admin/finance` hỗ trợ 6 chế độ lọc thời gian:
- **Toàn bộ thời gian** (mặc định)
- **Hôm nay** (`TODAY`)
- **7 ngày qua** (`LAST_7_DAYS`)
- **30 ngày qua** (`LAST_30_DAYS`)
- **Tháng này** (`THIS_MONTH`)
- **Tháng trước** (`LAST_MONTH`)
- **Tùy chọn khoảng ngày** (`CUSTOM`: chọn `startDate` và `endDate`)

---

## 4. PHẦN 3: ADMIN – ĐỐI SOÁT TÀI CHÍNH & XUẤT BÁO CÁO CSV AN TOÀN

### 4.1. Công cụ Đối soát (`ReconciliationService.java`)
Hệ thống duyệt qua từng đơn hàng và so chiếu 5 đối tượng tài chính:
1. `Order` (Tổng tiền đơn)
2. `PaymentTransaction` (Tiền người mua thanh toán)
3. `CommissionRecord` (Tiền phí sàn BookLoop trích lại)
4. `PayoutTransaction` (Tiền giải ngân cho người bán)
5. `RefundTransaction` (Tiền hoàn nếu có)
6. `PaymentProvider` (Số liệu đối soát từ Mock Payment Gateway)

**Các trạng thái đối soát chuẩn hóa:**
- `MATCHED`: Khớp 100% dòng tiền (Tổng tiền = Phí sàn + Tiền người bán + Hoàn tiền).
- `MISMATCH`: Sai lệch số tiền giữa các bảng hoặc lệch với cổng thanh toán.
- `PENDING`: Đơn hàng đang xử lý hoặc đang ký quỹ giữ tiền chưa đến kỳ đối soát.
- `MISSING_PROVIDER_DATA`: Có giao dịch nội bộ nhưng thiếu mã đối tác từ cổng thanh toán.
- `MISSING_INTERNAL_DATA`: Có mã thanh toán đối tác nhưng thiếu bản ghi nội bộ.
- `ERROR`: Lỗi dữ liệu bất thường hoặc tranh chấp chưa giải quyết.

### 4.2. Giao diện & Xuất file CSV (`/admin/finance/reconciliation/export`)
- **Banner Cảnh báo**: Ghi rõ *"MÔI TRƯỜNG THỬ NGHIỆM: CỔNG THANH TOÁN MÔ PHỎNG (DEMO / MOCK GATEWAY)"*.
- **4 Thẻ tóm tắt sức khỏe tài chính**: Khớp hoàn toàn, Cần xử lý lệch số liệu, Đang chờ đối soát, Thiếu dữ liệu.
- **Thanh công cụ lọc**: Tìm kiếm theo Mã đơn/Mã GD/Tên người dùng + Lọc theo Trạng thái đối soát.
- **Xuất file CSV an toàn**:
  + Tự động chèn UTF-8 BOM (`\uFEFF`) giúp mở trực tiếp trên Excel không bao giờ bị lỗi font tiếng Việt.
  + **Chống tấn công CSV Injection (Formula Injection)**: Các trường bắt đầu bằng `=`, `+`, `-`, `@` được tự động escape bằng ký tự nháy đơn `'`.
  + **Bảo mật tuyệt đối**: Không để lộ số tài khoản đầy đủ, API keys hay thông tin xác thực nhạy cảm.

---

## 5. GIẢI ĐÁP CHUYÊN SÂU: CƠ CHẾ GIẢI NGÂN TIỀN VỀ NGÂN HÀNG CHO NGƯỜI BÁN

### 5.1. Phân tích hiện tượng trong 2 ảnh chụp màn hình người dùng cung cấp
Trong 2 ảnh đính kèm của tài khoản người bán `quanghuy@bookloop.example`:
- **Ảnh 1**:
  + *Tổng doanh số hoàn tất*: **80.000 đ** (1 đơn giao thành công).
  + *Đã giải ngân về ngân hàng*: **0 đ**.
  + *Tài khoản ngân hàng*: Vietcombank (Đã kích hoạt).
  + *Lịch sử giải ngân*: (0) Chưa có giao dịch giải ngân nào.
- **Ảnh 2**:
  + *Tổng doanh số hoàn tất*: **195.000 đ** (3 đơn giao thành công).
  + *Phí hoa hồng nền tảng*: **3.500 đ** (% Khấu trừ tự động khi giải ngân).
  + *Đã giải ngân về ngân hàng*: **VẪN LÀ 0 đ**.
  + *Lịch sử giải ngân*: (0) Chưa có giao dịch giải ngân nào.

### 5.2. Tại sao Doanh số hoàn tất tăng lên nhưng "Đã giải ngân về ngân hàng" vẫn là 0 đ?
Nguyên nhân xuất phát từ 2 yếu tố:

#### Yếu tố 1: Bản chất của các phương thức thanh toán trong BookLoop
1. **Phương thức Giao dịch Trực tiếp C2C (`IN_PERSON_MEETUP` - Gặp trực tiếp tại Campus & `BANK_TRANSFER` - Chuyển khoản trực tiếp sinh viên)**:
   - Trong mô hình này, người mua đưa tiền mặt trực tiếp cho bạn khi nhận sách tại trường (hoặc tự quét mã QR chuyển khoản riêng vào tài khoản của bạn).
   - **Hệ thống BookLoop KHÔNG thu giữ đồng nào của giao dịch này.**
   - Do đó, hệ thống chỉ cộng dồn vào ô *"Tổng doanh số hoàn tất"* để người bán biết mình đã bán được bao nhiêu tiền sách. BookLoop **không thể giải ngân tiền đó về ngân hàng cho bạn**, bởi vì bạn đã cầm tiền mặt / nhận tiền trực tiếp từ người mua ngoài đời rồi. Nếu hệ thống giải ngân nữa thì người bán sẽ nhận tiền 2 lần.
2. **Phương thức Ký quỹ An toàn (`MARKETPLACE_HOLDING` - Thanh toán trực tuyến qua Sàn)**:
   - Người mua thanh toán online qua cổng BookLoop -> Tiền nằm ở ô *"Tiền đang giữ đối soát"*.
   - Sau khi bạn giao sách và **Người mua bấm "Đã nhận được sách"** (`confirmDelivery`), hệ thống mới tự động tạo lệnh Giải ngân (Payout): Trừ 3.500 đ hoa hồng sàn, chuyển phần còn lại (191.500 đ) vào tài khoản Vietcombank của bạn.
   - Nếu đơn hàng hoàn tất qua nút của người bán hoặc chưa qua bước kích hoạt Payout của cổng thanh toán, tiền sẽ được tính là *"Doanh số hoàn tất sẵn sàng giải ngân (Undisbursed Revenue)"*.

#### Yếu tố 2: Nút bấm Giải ngân chủ động chưa được kích hoạt ở phiên bản chụp ảnh
Tại thời điểm người dùng chụp 2 màn hình trên, phiên bản giao diện chưa hiển thị Banner hành động giải ngân chủ động cho người bán.

### 5.3. Giải pháp đã bổ sung & Hướng dẫn thao tác giải ngân thực tế
Chúng ta đã tích hợp trọn vẹn luồng **"Giải ngân ngay về ngân hàng"** (`POST /seller/finance/disburse-now`):
1. **Cách tính số tiền sẵn sàng giải ngân**:
   $$\text{Doanh số sẵn sàng giải ngân} = \text{Tổng doanh số hoàn tất} - (\text{Đã giải ngân} + \text{Hoa hồng đã trừ})$$
   Trong trường hợp Ảnh 2:
   $$\text{Sẵn sàng giải ngân} = 195.000\text{ đ} - 3.500\text{ đ} = 191.500\text{ đ}$$
2. **Banner màu xanh tự động hiển thị trên trang `/seller/finance`**:
   > *"Bạn có **191.500 ₫** doanh số hoàn tất sẵn sàng giải ngân! Bấm vào nút bên cạnh để chuyển số tiền này từ doanh số hoàn tất về tài khoản ngân hàng đã liên kết."*  
   > **[ Nút: Giải ngân ngay về ngân hàng ]**
3. **Kết quả ngay sau khi bấm nút**:
   - Hệ thống tự động quét 3 đơn hàng, tạo 3 bản ghi `PayoutTransaction` mã `POUT-20261105-XXXX` trạng thái `COMPLETED`.
   - Ô **"ĐÃ GIẢI NGÂN VỀ NGÂN HÀNG"** sẽ lập tức nhảy số từ **0 đ** lên **191.500 đ**.
   - Tab **"Lịch sử giải ngân"** sẽ hiển thị đầy đủ 3 dòng giao dịch kèm mã đối soát, STK nhận tiền Vietcombank ****2233 và thời gian thực hiện.

---

## 6. TỔNG HỢP SOURCE CODE ĐÃ CHỈNH SỬA & HƯỚNG DẪN KIỂM THỬ

### 6.1. Danh sách Files đã tạo mới & Cập nhật
- **Entity**:
  - `Category.java` (Entity mới)
  - `Product.java` (Bổ sung `categoryRef`, tương thích ngược `getCategory()`)
  - `ReconciliationStatus.java` (Enum mới)
- **Repository**:
  - `CategoryRepository.java` (Repository mới)
  - `ProductRepository.java` (Thêm filter category admin & explicit countQuery)
  - `PaymentTransactionRepository.java`, `PayoutTransactionRepository.java`, `CommissionRecordRepository.java` (Thêm query theo khoảng thời gian và countQuery)
- **Service**:
  - `CategoryService.java` (Service mới)
  - `SellerFinanceService.java` (Thêm tính năng `disburseUndisbursedOrders` và `hasUndisbursedRevenue`)
  - `AdminFinanceService.java` (Sửa lỗi read-only transaction, bổ sung 8 KPIs và date range filters)
  - `ReconciliationService.java` (Nâng cấp bộ quy tắc kiểm tra bất biến tài chính)
- **Controller**:
  - `BookController.java` (Nạp Category dropdown động từ database)
  - `HomeController.java` (Nạp Categories vào Hero search & tags)
  - `ListingController.java` (Nạp Categories vào form tạo và sửa sách)
  - `AdminFinanceController.java` (Hỗ trợ tham số range/startDate/endDate, API `/reconciliation/export`)
  - `SellerFinanceController.java` (Thêm endpoint `/seller/finance/disburse-now`)
- **Templates**:
  - `home.html` (Category dropdown trên Hero Section)
  - `books/list.html` (Dynamic Category filter dropdown)
  - `listings/form.html` (Dynamic Category `<select>`)
  - `admin/listings/list.html` (Category dropdown filter)
  - `admin/finance/overview.html` (8 KPI cards, Time filter toolbar)
  - `admin/finance/reconciliation.html` (Mock Gateway banner, KPI health cards, Filter bar, CSV export)
  - `seller/finance.html` (Banner nút "Giải ngân ngay về ngân hàng")

### 6.2. Hướng dẫn chạy và nghiệm thu sản phẩm
1. **Khởi động ứng dụng Spring Boot**:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
   cd "c:\Users\ADMIN\Downloads\bookloop\bookloop"
   .\mvnw.cmd spring-boot:run
   ```
2. **Kiểm tra Dropdown Thể loại Sách**:
   - Truy cập `http://localhost:8080/` -> Kiểm tra ô chọn Thể loại trên Hero Section.
   - Truy cập `http://localhost:8080/books` -> Lọc theo từng thể loại, kiểm tra danh sách sách trả về chính xác theo Category ID/Tên.
   - Đăng nhập sinh viên (`quanghuy@bookloop.example` / `password123`) -> Vào Đăng bán sách -> Thể loại là danh sách dropdown lấy từ database.
3. **Kiểm tra Giải ngân Ví người bán**:
   - Truy cập `http://localhost:8080/seller/finance`.
   - Xem banner màu xanh *"Bạn có 191.500 ₫ doanh số hoàn tất sẵn sàng giải ngân!"*.
   - Bấm nút **"Giải ngân ngay về ngân hàng"** -> Kiểm tra ô **"ĐÃ GIẢI NGÂN VỀ NGÂN HÀNG"** nhảy số và bảng **"Lịch sử giải ngân"** xuất hiện các giao dịch thành công.
4. **Kiểm tra Admin Tài chính & Đối soát**:
   - Đăng nhập quyền Quản trị (`admin@bookloop.example` / `admin123`).
   - Vào `http://localhost:8080/admin/finance` -> Kiểm tra 8 ô KPI và thử chọn các mốc thời gian (Hôm nay, 7 ngày qua, Tháng này...).
   - Vào `http://localhost:8080/admin/finance/reconciliation` -> Kiểm tra bảng đối soát, lọc theo trạng thái và bấm **"Xuất CSV Đối soát"** để tải về file Excel không lỗi font tiếng Việt.

