-- =============================================================================
-- BookLoop Phase 7: Complete Marketplace Payment System Schema
-- Microsoft SQL Server Compatible DDL Migration
-- =============================================================================

-- 1. Alter Orders Table to support single-seller grouping & lifecycle timestamps
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'seller_id')
BEGIN
    ALTER TABLE orders ADD seller_id BIGINT NULL;
    ALTER TABLE orders ADD CONSTRAINT FK_orders_seller FOREIGN KEY (seller_id) REFERENCES users(id);
    CREATE INDEX idx_orders_seller ON orders(seller_id);
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'parent_checkout_group')
BEGIN
    ALTER TABLE orders ADD parent_checkout_group NVARCHAR(64) NULL;
    CREATE INDEX idx_orders_parent_group ON orders(parent_checkout_group);
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'accepted_at')
BEGIN
    ALTER TABLE orders ADD accepted_at DATETIME2 NULL;
    ALTER TABLE orders ADD shipped_at DATETIME2 NULL;
    ALTER TABLE orders ADD received_confirmed_at DATETIME2 NULL;
    ALTER TABLE orders ADD completed_at DATETIME2 NULL;
END;

-- Update orders check constraints for expanded lifecycle status & payment methods
IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_orders_status')
BEGIN
    ALTER TABLE orders DROP CONSTRAINT CK_orders_status;
END;
ALTER TABLE orders ADD CONSTRAINT CK_orders_status CHECK (status IN (
    'PENDING', 'AWAITING_PAYMENT', 'PAID_HELD', 'CONFIRMED', 'SHIPPED', 
    'DELIVERED', 'DISPUTED', 'REFUNDED', 'COMPLETED', 'CANCELLED'
));

IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_orders_payment')
BEGIN
    ALTER TABLE orders DROP CONSTRAINT CK_orders_payment;
END;
ALTER TABLE orders ADD CONSTRAINT CK_orders_payment CHECK (payment_method IN (
    'IN_PERSON_MEETUP', 'BANK_TRANSFER', 'MARKETPLACE_HOLDING'
));

-- 2. Seller Payment Accounts (KYC & Recipient IDs for split payouts)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'seller_payment_accounts')
BEGIN
    CREATE TABLE seller_payment_accounts (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        seller_id BIGINT NOT NULL,
        provider_code NVARCHAR(50) NOT NULL,
        provider_recipient_id NVARCHAR(100) NOT NULL,
        bank_code NVARCHAR(30) NULL,
        account_number_mask NVARCHAR(50) NULL,
        account_holder_name NVARCHAR(100) NULL,
        kyc_status NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
        status NVARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT FK_seller_pay_accounts_seller FOREIGN KEY (seller_id) REFERENCES users(id)
    );
    CREATE INDEX idx_seller_pay_account_seller ON seller_payment_accounts(seller_id);
    CREATE INDEX idx_seller_pay_account_provider ON seller_payment_accounts(provider_code);
END;

-- 3. Payment Transactions (Partner Holding / Escrow)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'payment_transactions')
BEGIN
    CREATE TABLE payment_transactions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        provider_code NVARCHAR(50) NOT NULL,
        provider_payment_id NVARCHAR(100) NOT NULL,
        merchant_reference NVARCHAR(64) NOT NULL UNIQUE,
        amount DECIMAL(12,2) NOT NULL,
        currency NVARCHAR(10) NOT NULL DEFAULT 'VND',
        payment_status NVARCHAR(30) NOT NULL DEFAULT 'INITIATED',
        payment_method_type NVARCHAR(50) NULL,
        qr_code_data NVARCHAR(2000) NULL,
        payment_url NVARCHAR(2000) NULL,
        paid_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT FK_pay_tx_order FOREIGN KEY (order_id) REFERENCES orders(id)
    );
    CREATE INDEX idx_pay_tx_order ON payment_transactions(order_id);
    CREATE INDEX idx_pay_tx_provider_pay_id ON payment_transactions(provider_payment_id);
    CREATE INDEX idx_pay_tx_merchant_ref ON payment_transactions(merchant_reference);
    CREATE INDEX idx_pay_tx_status ON payment_transactions(payment_status);
END;

-- 4. Commission Settings (Admin-configured rates)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'commission_settings')
BEGIN
    CREATE TABLE commission_settings (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        commission_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0500,
        effective_from DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        effective_to DATETIME2 NULL,
        is_active BIT NOT NULL DEFAULT 1,
        notes NVARCHAR(500) NULL,
        updated_by BIGINT NULL,
        updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_comm_settings_admin FOREIGN KEY (updated_by) REFERENCES users(id)
    );
END;

-- 5. Commission Records (Immutable snapshots per order)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'commission_records')
BEGIN
    CREATE TABLE commission_records (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL UNIQUE,
        seller_id BIGINT NOT NULL,
        payment_transaction_id BIGINT NULL,
        gross_amount DECIMAL(12,2) NOT NULL,
        commission_rate_snapshot DECIMAL(5,4) NOT NULL,
        commission_amount DECIMAL(12,2) NOT NULL,
        partner_fee_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
        seller_net_amount DECIMAL(12,2) NOT NULL,
        currency NVARCHAR(10) NOT NULL DEFAULT 'VND',
        status NVARCHAR(30) NOT NULL DEFAULT 'PENDING_SETTLEMENT',
        settled_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_comm_rec_order FOREIGN KEY (order_id) REFERENCES orders(id),
        CONSTRAINT FK_comm_rec_seller FOREIGN KEY (seller_id) REFERENCES users(id),
        CONSTRAINT FK_comm_rec_pay_tx FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id)
    );
    CREATE INDEX idx_comm_rec_seller ON commission_records(seller_id);
    CREATE INDEX idx_comm_rec_status ON commission_records(status);
END;

-- 6. Payout Transactions (Disbursements to sellers)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'payout_transactions')
BEGIN
    CREATE TABLE payout_transactions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        seller_id BIGINT NOT NULL,
        commission_record_id BIGINT NULL,
        provider_code NVARCHAR(50) NOT NULL,
        provider_payout_id NVARCHAR(100) NULL,
        idempotency_key NVARCHAR(64) NOT NULL UNIQUE,
        amount DECIMAL(12,2) NOT NULL,
        currency NVARCHAR(10) NOT NULL DEFAULT 'VND',
        status NVARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
        failure_reason NVARCHAR(1000) NULL,
        retry_count INT NOT NULL DEFAULT 0,
        requested_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        completed_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_payout_tx_order FOREIGN KEY (order_id) REFERENCES orders(id),
        CONSTRAINT FK_payout_tx_seller FOREIGN KEY (seller_id) REFERENCES users(id),
        CONSTRAINT FK_payout_tx_comm_rec FOREIGN KEY (commission_record_id) REFERENCES commission_records(id)
    );
    CREATE INDEX idx_payout_tx_order ON payout_transactions(order_id);
    CREATE INDEX idx_payout_tx_seller ON payout_transactions(seller_id);
    CREATE INDEX idx_payout_tx_status ON payout_transactions(status);
END;

-- 7. Disputes (Buyer/Seller claim cases)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'disputes')
BEGIN
    CREATE TABLE disputes (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        opened_by BIGINT NOT NULL,
        reason NVARCHAR(200) NOT NULL,
        description NVARCHAR(2000) NULL,
        evidence_url NVARCHAR(500) NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'OPEN',
        resolution_note NVARCHAR(2000) NULL,
        resolved_by BIGINT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        resolved_at DATETIME2 NULL,
        CONSTRAINT FK_dispute_order FOREIGN KEY (order_id) REFERENCES orders(id),
        CONSTRAINT FK_dispute_opened_by FOREIGN KEY (opened_by) REFERENCES users(id),
        CONSTRAINT FK_dispute_resolved_by FOREIGN KEY (resolved_by) REFERENCES users(id)
    );
    CREATE INDEX idx_dispute_order ON disputes(order_id);
    CREATE INDEX idx_dispute_opened_by ON disputes(opened_by);
    CREATE INDEX idx_dispute_status ON disputes(status);
END;

-- 8. Refund Transactions (Returned funds to buyers)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'refund_transactions')
BEGIN
    CREATE TABLE refund_transactions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        payment_transaction_id BIGINT NOT NULL,
        order_id BIGINT NOT NULL,
        dispute_id BIGINT NULL,
        provider_refund_id NVARCHAR(100) NULL,
        idempotency_key NVARCHAR(64) NOT NULL UNIQUE,
        refund_amount DECIMAL(12,2) NOT NULL,
        refund_type NVARCHAR(20) NOT NULL DEFAULT 'FULL',
        reason NVARCHAR(1000) NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
        initiated_by BIGINT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        completed_at DATETIME2 NULL,
        CONSTRAINT FK_refund_tx_pay FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id),
        CONSTRAINT FK_refund_tx_order FOREIGN KEY (order_id) REFERENCES orders(id),
        CONSTRAINT FK_refund_tx_dispute FOREIGN KEY (dispute_id) REFERENCES disputes(id),
        CONSTRAINT FK_refund_tx_user FOREIGN KEY (initiated_by) REFERENCES users(id)
    );
    CREATE INDEX idx_refund_tx_pay ON refund_transactions(payment_transaction_id);
    CREATE INDEX idx_refund_tx_order ON refund_transactions(order_id);
END;

-- 9. Payment Webhook Events (Idempotency and Raw audit)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'payment_webhook_events')
BEGIN
    CREATE TABLE payment_webhook_events (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        provider_code NVARCHAR(50) NOT NULL,
        provider_event_id NVARCHAR(100) NOT NULL,
        event_type NVARCHAR(100) NOT NULL,
        signature_verified BIT NOT NULL DEFAULT 0,
        raw_payload NVARCHAR(MAX) NOT NULL,
        processing_status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
        error_message NVARCHAR(2000) NULL,
        received_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        processed_at DATETIME2 NULL
    );
    CREATE INDEX idx_webhook_prov_event ON payment_webhook_events(provider_code, provider_event_id);
    CREATE INDEX idx_webhook_status ON payment_webhook_events(processing_status);
END;

-- 10. Financial Audit Logs (Immutable audit trail)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'financial_audit_logs')
BEGIN
    CREATE TABLE financial_audit_logs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        actor_id BIGINT NULL,
        actor_email NVARCHAR(150) NULL,
        action NVARCHAR(100) NOT NULL,
        entity_type NVARCHAR(100) NOT NULL,
        entity_id BIGINT NOT NULL,
        reference_number NVARCHAR(100) NULL,
        amount DECIMAL(12,2) NULL,
        old_value NVARCHAR(1000) NULL,
        new_value NVARCHAR(1000) NULL,
        notes NVARCHAR(2000) NULL,
        ip_address NVARCHAR(64) NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
    CREATE INDEX idx_audit_actor ON financial_audit_logs(actor_id);
    CREATE INDEX idx_audit_action ON financial_audit_logs(action);
    CREATE INDEX idx_audit_entity ON financial_audit_logs(entity_type, entity_id);
    CREATE INDEX idx_audit_created ON financial_audit_logs(created_at);
END;

