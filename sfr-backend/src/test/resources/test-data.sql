-- Integration Test Data
INSERT INTO user_balance (id, user_id, current_balance, created_at, updated_at) 
VALUES 
    (1, 'integration-test-user-1', 1000.00, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    (2, 'integration-test-user-2', 500.00, '2025-08-20 10:00:00', '2025-08-20 10:00:00'),
    (3, 'integration-test-admin', 10000.00, '2025-08-20 10:00:00', '2025-08-20 10:00:00');

INSERT INTO balance_history (id, user_id, amount, transaction_type, description, created_at)
VALUES
    (1, 'integration-test-user-1', 1000.00, 'EARN', 'Initial deposit', '2025-08-20 10:00:00'),
    (2, 'integration-test-user-2', 500.00, 'EARN', 'Initial deposit', '2025-08-20 10:00:00'),
    (3, 'integration-test-admin', 10000.00, 'EARN', 'Admin initial balance', '2025-08-20 10:00:00');
