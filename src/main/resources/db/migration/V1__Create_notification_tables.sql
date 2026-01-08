-- Notification Service Database Schema
-- V1: Initial notification tables

-- Notification Preference Table
CREATE TABLE notification_preference (
    preference_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    account_code VARCHAR(50) NULL,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(200) NOT NULL,
    is_enabled BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Indexes for notification preference
CREATE INDEX idx_notification_pref_user ON notification_preference(user_id);
CREATE INDEX idx_notification_pref_account ON notification_preference(account_code);
CREATE INDEX idx_notification_pref_type ON notification_preference(notification_type);
CREATE INDEX idx_notification_pref_enabled ON notification_preference(is_enabled);

-- Notification Table
CREATE TABLE notification (
    notification_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    notification_type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(200) NOT NULL,
    subject VARCHAR(500) NULL,
    message NVARCHAR(MAX) NOT NULL,
    reference_type VARCHAR(50) NULL,
    reference_id VARCHAR(100) NULL,
    correlation_id VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(1000) NULL,
    sent_at DATETIME2 NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Indexes for notification
CREATE INDEX idx_notification_status ON notification(status);
CREATE INDEX idx_notification_recipient ON notification(recipient);
CREATE INDEX idx_notification_type ON notification(notification_type);
CREATE INDEX idx_notification_reference ON notification(reference_type, reference_id);
CREATE INDEX idx_notification_created_at ON notification(created_at);
CREATE INDEX idx_notification_correlation ON notification(correlation_id);

-- Trigger to update updated_at for preferences
CREATE TRIGGER trg_notification_pref_updated_at
ON notification_preference
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE notification_preference
    SET updated_at = GETDATE()
    FROM notification_preference n
    INNER JOIN inserted i ON n.preference_id = i.preference_id;
END;
GO

-- Insert sample notification preferences
INSERT INTO notification_preference (user_id, account_code, notification_type, channel, recipient)
VALUES
    ('admin', NULL, 'RISK_ALERT', 'EMAIL', 'admin@tms.local'),
    ('admin', NULL, 'SETTLEMENT_FAILED', 'EMAIL', 'admin@tms.local'),
    ('trader1', 'ACC001', 'TRADE_BOOKED', 'EMAIL', 'trader1@tms.local'),
    ('trader1', 'ACC001', 'RISK_ALERT', 'EMAIL', 'trader1@tms.local');
