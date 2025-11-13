-- ========================================
-- FIX FOR PAYMENT TRANSACTIONS TABLE
-- ========================================
-- This script drops the old payment_transactions table
-- that was created with incorrect column names
-- and allows Hibernate to recreate it with the correct schema
-- ========================================

USE student_based_help_desk_001;

-- Drop the old payment_transactions table
DROP TABLE IF EXISTS payment_transactions;

-- The table will be automatically recreated by Hibernate
-- when you restart the Spring Boot application with the correct columns:
--   - transaction_number (instead of any old name)
--   - reference_number (instead of transaction_reference)
--   - and all other correct column names

SELECT 'Old payment_transactions table dropped successfully!' AS Status;
SELECT 'Now restart your Spring Boot application to recreate the table with the correct schema.' AS NextStep;

