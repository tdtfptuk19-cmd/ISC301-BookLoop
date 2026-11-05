# BOOKLOOP — SƠ ĐỒ USE CASE TOÀN HỆ THỐNG (USE CASE DIAGRAMS)

Tài liệu này cung cấp tập hợp các sơ đồ Use Case chuẩn UML (sử dụng cú pháp Mermaid) mô hình hóa trực quan ranh giới hệ thống (**System Boundary**), các tác tử (**Actors**) và toàn bộ các ca sử dụng của nền tảng **BookLoop C2C Student Marketplace**.

---

## 1. Sơ đồ Tổng quan Ranh giới Hệ thống và Phân cấp Tác tử (Actors Hierarchy)

```mermaid
classDiagram
    direction TB
    class User {
        <<Entity / Abstract Actor>>
        +Long id
        +String email
        +String fullName
        +String campus
        +Role role
        +Boolean enabled
    }

    class Guest {
        <<Actor>>
        Khách vãng lai chưa đăng nhập
    }

    class StudentBuyer {
        <<Actor: Authenticated Student>>
        Sinh viên mua sách giáo trình
    }

    class StudentSeller {
        <<Actor: Authenticated Student>>
        Sinh viên đăng bán sách & nhận tiền
    }

    class SystemAdmin {
        <<Actor: ROLE_ADMIN>>
        Quản trị viên vận hành & tài chính
    }

    class PaymentPartner {
        <<External System Actor>>
        Cổng thanh toán ký quỹ / Ngân hàng VietQR
    }

    class SystemActor {
        <<Internal Background Actor>>
        Tác tử tự động (Cron / Event / Invariant)
    }

    User <|-- StudentBuyer
    User <|-- StudentSeller
    User <|-- SystemAdmin
```

---

## 2. Phân hệ 1: Sơ đồ Use Case Khách vãng lai (Guest Use Cases)

Khách vãng lai có thể khám phá toàn bộ kho tri thức, tìm kiếm sách cũ và tra cứu giáo trình chuẩn 3 trường mà không cần đăng nhập.

```mermaid
flowchart LR
    Guest(("Khách vãng lai (Guest)"))

    subgraph System_Boundary_Guest ["BookLoop — Phân hệ Khám phá & Xác thực"]
        UC_GST_001(["UC-GST-001: Khám phá Trang chủ & Tin nổi bật"])
        UC_GST_002(["UC-GST-002: Tìm kiếm & Lọc sách theo trường / giá / môn"])
        UC_GST_003(["UC-GST-003: Xem chi tiết tin đăng sách rao bán"])
        UC_GST_004(["UC-GST-004: Tra cứu mục lục giáo trình chuẩn 3 trường"])
        UC_GST_005(["UC-GST-005: Xem giáo trình theo môn học & bản sao cũ"])
        UC_GST_006(["UC-GST-006: Xem hồ sơ công khai & đánh giá người bán"])
        UC_GST_007(["UC-GST-007: Đăng ký tài khoản sinh viên mới"])
        UC_GST_008(["UC-GST-008: Đăng nhập hệ thống (Form Login)"])
    end

    Guest --> UC_GST_001
    Guest --> UC_GST_002
    Guest --> UC_GST_003
    Guest --> UC_GST_004
    Guest --> UC_GST_005
    Guest --> UC_GST_006
    Guest --> UC_GST_007
    Guest --> UC_GST_008

    UC_GST_002 -.->|<<extend>>| UC_GST_003
    UC_GST_004 -.->|<<include>>| UC_GST_005
    UC_GST_007 -.->|<<follow>>| UC_GST_008
```

---

## 3. Phân hệ 2: Sơ đồ Use Case Sinh viên Mua (Student Buyer Use Cases)

Bao gồm toàn bộ quy trình mua hàng: giỏ hàng, đặt hàng tách đơn đa người bán, thanh toán ký quỹ bảo vệ người mua, xác nhận nhận sách, đánh giá và khiếu nại.

```mermaid
flowchart LR
    Buyer(("Sinh viên Mua (Buyer)"))
    Partner(("Cổng Ký quỹ / Ngân hàng"))

    subgraph System_Boundary_Buyer ["BookLoop — Phân hệ Mua sắm & Ký quỹ"]
        UC_BUY_001(["UC-BUY-001: Quản lý hồ sơ cá nhân & Campus"])
        UC_BUY_002(["UC-BUY-002: Thêm sách vào giỏ hàng"])
        UC_BUY_003(["UC-BUY-003: Xem & Quản lý giỏ hàng"])
        UC_BUY_004(["UC-BUY-004: Đặt hàng ngay (Buy Now)"])
        UC_BUY_005(["UC-BUY-005: Tạo đơn hàng & Tách đơn đa người bán"])
        UC_BUY_006(["UC-BUY-006: Thanh toán ký quỹ qua VietQR"])
        UC_BUY_007(["UC-BUY-007: Thanh toán mô phỏng Sandbox Demo"])
        UC_BUY_008(["UC-BUY-008: Xem danh sách đơn mua & Lịch sử"])
        UC_BUY_009(["UC-BUY-009: Xác nhận đã nhận sách & Nhả ký quỹ"])
        UC_BUY_010(["UC-BUY-010: Khiếu nại đơn hàng (Dispute)"])
        UC_BUY_011(["UC-BUY-011: Hủy đơn hàng khi chưa chuyển hàng"])
        UC_BUY_012(["UC-BUY-012: Đánh giá 1-5 sao người bán"])
        UC_BUY_013(["UC-BUY-013: Báo cáo tin vi phạm / Gian lận"])
    end

    Buyer --> UC_BUY_001
    Buyer --> UC_BUY_002
    Buyer --> UC_BUY_003
    Buyer --> UC_BUY_004
    Buyer --> UC_BUY_005
    Buyer --> UC_BUY_006
    Buyer --> UC_BUY_007
    Buyer --> UC_BUY_008
    Buyer --> UC_BUY_009
    Buyer --> UC_BUY_010
    Buyer --> UC_BUY_011
    Buyer --> UC_BUY_012
    Buyer --> UC_BUY_013

    UC_BUY_002 -.->|<<include>>| UC_BUY_003
    UC_BUY_003 -.->|<<include>>| UC_BUY_005
    UC_BUY_004 -.->|<<include>>| UC_BUY_005
    UC_BUY_005 -.->|<<include>>| UC_BUY_006
    UC_BUY_006 -.->|<<extend>>| UC_BUY_007
    UC_BUY_006 --> Partner
    UC_BUY_009 -.->|<<trigger>>| UC_BUY_012
```

---

## 4. Phân hệ 3: Sơ đồ Use Case Sinh viên Bán (Student Seller Use Cases)

Bao gồm việc đăng bán sách (khớp danh mục giáo trình), quảng cáo tin nổi bật, xử lý đơn bán, theo dõi ví số dư và nhận tiền giải ngân.

```mermaid
flowchart LR
    Seller(("Sinh viên Bán (Seller)"))

    subgraph System_Boundary_Seller ["BookLoop — Phân hệ Người bán & Ví tài chính"]
        UC_SEL_001(["UC-SEL-001: Đăng tin bán sách cũ"])
        UC_SEL_002(["UC-SEL-002: Khớp với giáo trình chính thức"])
        UC_SEL_003(["UC-SEL-003: Quản lý tin đăng (Sửa / Xóa)"])
        UC_SEL_004(["UC-SEL-004: Thanh toán đẩy tin nổi bật (10.000 ₫)"])
        UC_SEL_005(["UC-SEL-005: Xem danh sách đơn bán của tôi"])
        UC_SEL_006(["UC-SEL-006: Tiếp nhận đơn bán (Accept Order)"])
        UC_SEL_007(["UC-SEL-007: Xác nhận gửi hàng / Đi giao (Dispatch)"])
        UC_SEL_008(["UC-SEL-008: Xem ví số dư & Báo cáo doanh thu"])
        UC_SEL_009(["UC-SEL-009: Liên kết tài khoản ngân hàng (KYC)"])
    end

    Seller --> UC_SEL_001
    Seller --> UC_SEL_003
    Seller --> UC_SEL_004
    Seller --> UC_SEL_005
    Seller --> UC_SEL_006
    Seller --> UC_SEL_007
    Seller --> UC_SEL_008
    Seller --> UC_SEL_009

    UC_SEL_001 -.->|<<extend>>| UC_SEL_002
    UC_SEL_001 -.->|<<extend>>| UC_SEL_004
    UC_SEL_005 -.->|<<include>>| UC_SEL_006
    UC_SEL_006 -.->|<<follow>>| UC_SEL_007
    UC_SEL_008 -.->|<<include>>| UC_SEL_009
```

---

## 5. Phân hệ 4: Sơ đồ Use Case Quản trị viên (System Admin Use Cases)

Bao gồm kiểm duyệt người dùng, tin đăng, đơn hàng, cấu hình biểu phí, trọng tài tranh chấp và đối soát tài chính tự động.

```mermaid
flowchart LR
    Admin(("Quản trị viên (Admin)"))

    subgraph System_Boundary_Admin ["BookLoop — Phân hệ Vận hành & Quản trị Tài chính"]
        UC_ADM_001(["UC-ADM-001: Bảng điều khiển quản trị (Dashboard)"])
        UC_ADM_002(["UC-ADM-002: Quản lý & Khóa/Mở tài khoản người dùng"])
        UC_ADM_003(["UC-ADM-003: Kiểm duyệt tin đăng & Gỡ tin vi phạm"])
        UC_ADM_004(["UC-ADM-004: Giám sát đơn hàng toàn hệ thống"])
        UC_ADM_005(["UC-ADM-005: Kiểm duyệt & Ẩn đánh giá thù địch"])
        UC_ADM_006(["UC-ADM-006: Thụ lý & Xử lý báo cáo vi phạm"])
        UC_ADM_007(["UC-ADM-007: Cấu hình biểu phí sàn & Tỷ lệ hoa hồng (5%)"])
        UC_ADM_008(["UC-ADM-008: Tra cứu nhật ký kiểm toán (Audit Logs)"])
        UC_ADM_009(["UC-ADM-009: Tổng quan tài chính & Dòng tiền sàn"])
        UC_ADM_010(["UC-ADM-010: Giám sát giao dịch nạp ký quỹ (Payments)"])
        UC_ADM_011(["UC-ADM-011: Quản lý & Duyệt giải ngân (Payouts)"])
        UC_ADM_012(["UC-ADM-012: Theo dõi doanh thu hoa hồng sàn (Commissions)"])
        UC_ADM_013(["UC-ADM-013: Xử lý hoàn tiền cho người mua (Refunds)"])
        UC_ADM_014(["UC-ADM-014: Trọng tài phán quyết tranh chấp (Disputes)"])
        UC_ADM_015(["UC-ADM-015: Chạy đối soát tự động & Bất biến Delta = 0"])
    end

    Admin --> UC_ADM_001
    Admin --> UC_ADM_002
    Admin --> UC_ADM_003
    Admin --> UC_ADM_004
    Admin --> UC_ADM_005
    Admin --> UC_ADM_006
    Admin --> UC_ADM_007
    Admin --> UC_ADM_008
    Admin --> UC_ADM_009
    Admin --> UC_ADM_010
    Admin --> UC_ADM_011
    Admin --> UC_ADM_012
    Admin --> UC_ADM_013
    Admin --> UC_ADM_014
    Admin --> UC_ADM_015

    UC_ADM_014 -.->|<<trigger>>| UC_ADM_013
    UC_ADM_014 -.->|<<trigger>>| UC_ADM_011
    UC_ADM_009 -.->|<<include>>| UC_ADM_015
```

---

## 6. Phân hệ 5: Sơ đồ Tác tử Hệ thống & Cổng Thanh toán (External & System Actors)

```mermaid
flowchart TD
    Partner["External Payment Provider (Cổng TT / VietQR)"]
    Sys["System Actor (Background Services / Spring Events)"]

    subgraph System_Boundary_External ["BookLoop — Xử lý Bất đồng bộ & Luồng tiền tự động"]
        UC_PAY_001(["UC-PAY-001: Tiếp nhận & Xác thực chữ ký Webhook"])
        UC_PAY_002(["UC-PAY-002: Tạo chuỗi EMVCo VietQR động"])
        UC_SYS_001(["UC-SYS-001: Tự động gom nhóm & Tách đơn theo Seller"])
        UC_SYS_002(["UC-SYS-002: Tự động trích xuất hoa hồng 5% sàn"])
        UC_SYS_003(["UC-SYS-003: Tự động kích hoạt Payout khi đơn hoàn tất"])
        UC_SYS_004(["UC-SYS-004: Kiểm toán bất biến Delta = Inflow - Outflows == 0"])
    end

    Partner -->|HTTP POST| UC_PAY_001
    Partner -->|API Response| UC_PAY_002

    UC_SYS_001 --> Sys
    UC_SYS_002 --> Sys
    UC_SYS_003 --> Sys
    UC_SYS_004 --> Sys

    UC_PAY_001 -.->|Trigger chuyển trạng thái| UC_SYS_002
    UC_SYS_003 -.->|Cộng số dư ví Seller| Sys
```

