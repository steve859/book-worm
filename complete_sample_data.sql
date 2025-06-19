-- ============================================================================
-- BOOKWORM APPLICATION: COMPREHENSIVE SAMPLE DATA SCRIPT (Final Version)
-- Description: This script TRUNCATES all existing data and inserts a clean,
-- comprehensive, and logical dataset for full application testing.
-- ============================================================================

-- Step 1: Disable foreign key checks to allow truncation of tables in any order.
SET FOREIGN_KEY_CHECKS = 0;

-- Step 2: Truncate all tables to ensure a clean slate.
TRUNCATE TABLE `authors_books`;

TRUNCATE TABLE `books_categories`;

TRUNCATE TABLE `books_import_receipts`;

TRUNCATE TABLE `books_invoices`;

TRUNCATE TABLE `users_roles`;

TRUNCATE TABLE `roles_permissions`;

TRUNCATE TABLE `payment_receipts_invoices`;

TRUNCATE TABLE `monthly_debt_report_details`;

TRUNCATE TABLE `monthly_inventory_report_details`;

TRUNCATE TABLE `authors`;

TRUNCATE TABLE `books`;

TRUNCATE TABLE `categories`;

TRUNCATE TABLE `import_receipts`;

TRUNCATE TABLE `invoices`;

TRUNCATE TABLE `parameters`;

TRUNCATE TABLE `permissions`;

TRUNCATE TABLE `roles`;

-- TRUNCATE TABLE `users`;

TRUNCATE TABLE `payment_receipts`;

TRUNCATE TABLE `monthly_debt_reports`;

TRUNCATE TABLE `monthly_inventory_reports`;

TRUNCATE TABLE `invalidated_token`;

-- Step 3: Insert fresh data.

-- Parameters
INSERT INTO
    `parameters` (`param_name`, `param_value`)
VALUES ('minImportQuantity', 50),
    ('maxStockQuantity', 500),
    ('minStockBeforeImport', 20),
    ('minInvoiceAmount', 10000),
    ('customersCanHaveDebt', 1),
    ('maxDebtAmount', 500000.0),
    ('sell_price_ratio', 1.05);

-- Roles and Permissions
INSERT INTO
    `roles` (
        `role_name`,
        `role_description`
    )
VALUES (
        'ADMIN',
        'Administrator with full system access'
    ),
    ('USER', 'Customer account');

INSERT INTO
    `permissions` (
        `permission_name`,
        `permission_description`
    )
VALUES (
        'MANAGE_USERS',
        'Create, update, and delete user accounts'
    ),
    (
        'MANAGE_BOOKS',
        'Add, edit, and delete books from the catalog'
    ),
    (
        'MANAGE_IMPORTS',
        'Create and manage import receipts'
    ),
    (
        'MANAGE_EXPORTS',
        'Create and manage sales invoices'
    ),
    (
        'VIEW_REPORTS',
        'View monthly debt and inventory reports'
    );

INSERT INTO
    `roles_permissions` (
        `roles_role_name`,
        `permissions_permission_name`
    )
VALUES ('ADMIN', 'MANAGE_USERS'),
    ('ADMIN', 'MANAGE_BOOKS'),
    ('ADMIN', 'MANAGE_IMPORTS'),
    ('ADMIN', 'MANAGE_EXPORTS'),
    ('ADMIN', 'VIEW_REPORTS');

-- Users (10 total: 2 admins, 8 users, password for all is "123456")
INSERT INTO
    `users` (
        `user_id`,
        `username`,
        `password`,
        `first_name`,
        `last_name`,
        `email`,
        `phone`,
        `dob`,
        `debt_amount_user`
    )
VALUES (
        'admin001',
        'mainadmin',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Main',
        'Admin',
        'main.admin@bookworm.com',
        '0123456780',
        '1990-01-01',
        0.00
    ),
    (
        'admin002',
        'staff',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Staff',
        'Member',
        'staff.member@bookworm.com',
        '0123456781',
        '1995-05-10',
        0.00
    ),
    (
        'user001',
        'customer1',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'An',
        'Nguyễn',
        'customer1@email.com',
        '0987654321',
        '2000-10-20',
        100000.00
    ),
    (
        'user002',
        'customer2',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Bình',
        'Trần',
        'customer2@email.com',
        '0987654322',
        '1998-12-15',
        0.00
    ),
    (
        'user003',
        'customer3',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Cường',
        'Lê',
        'customer3@email.com',
        '0987654323',
        '2001-01-30',
        285750.00
    ),
    (
        'user004',
        'customer4',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Dung',
        'Phạm',
        'customer4@email.com',
        '0987654324',
        '1999-03-25',
        0.00
    ),
    (
        'user005',
        'customer5',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Giang',
        'Võ',
        'customer5@email.com',
        '0987654325',
        '2002-07-07',
        0.00
    ),
    (
        'user006',
        'customer6',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Hương',
        'Đặng',
        'customer6@email.com',
        '0987654326',
        '2003-11-11',
        0.00
    ),
    (
        'user007',
        'customer7',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Khanh',
        'Hoàng',
        'customer7@email.com',
        '0987654327',
        '1997-08-19',
        0.00
    ),
    (
        'user008',
        'customer8',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Linh',
        'Doãn',
        'customer8@email.com',
        '0987654328',
        '2000-05-05',
        0.00
    );

INSERT INTO
    `users_roles` (
        `users_user_id`,
        `roles_role_name`
    )
VALUES ('admin-001', 'ADMIN'),
    ('admin001', 'ADMIN'),
    ('admin002', 'ADMIN'),
    ('user001', 'USER'),
    ('user002', 'USER'),
    ('user003', 'USER'),
    ('user004', 'USER'),
    ('user005', 'USER'),
    ('user006', 'USER'),
    ('user007', 'USER'),
    ('user008', 'USER');

-- Authors and Categories
INSERT INTO
    `authors` (`author_id`, `author_name`)
VALUES (1, 'Tô Hoài'),
    (2, 'Nam Cao'),
    (3, 'Nguyễn Du'),
    (4, 'J.K. Rowling'),
    (5, 'George Orwell'),
    (6, 'Haruki Murakami'),
    (7, 'Dale Carnegie'),
    (8, 'Stephen Hawking'),
    (9, 'Antoine de Saint-Exupéry'),
    (10, 'Paulo Coelho');

INSERT INTO
    `categories` (
        `category_id`,
        `category_name`
    )
VALUES (1, 'Văn học Việt Nam'),
    (2, 'Sách thiếu nhi'),
    (3, 'Tiểu thuyết'),
    (4, 'Fantasy'),
    (5, 'Khoa học viễn tưởng'),
    (6, 'Self-help'),
    (7, 'Khoa học phổ thông'),
    (8, 'Kinh điển');

-- Books (20 types)
INSERT INTO
    `books` (
        `book_id`,
        `book_name`,
        `published_year`,
        `import_price`,
        `sell_price`,
        `quantity`
    )
VALUES (
        1,
        'Dế Mèn phiêu lưu ký',
        1941,
        70000.00,
        73500.00,
        130
    ),
    (
        2,
        'Lão Hạc',
        1943,
        45000.00,
        47250.00,
        145
    ),
    (
        3,
        'Truyện Kiều',
        1820,
        90000.00,
        94500.00,
        70
    ),
    (
        4,
        'Harry Potter and the Sorcerer''s Stone',
        1997,
        170000.00,
        178500.00,
        40
    ),
    (
        5,
        '1984',
        1949,
        110000.00,
        115500.00,
        98
    ),
    (
        6,
        'Rừng Na Uy',
        1987,
        135000.00,
        141750.00,
        60
    ),
    (
        7,
        'Đắc nhân tâm',
        1936,
        85000.00,
        89250.00,
        110
    ),
    (
        8,
        'Lược sử thời gian',
        1988,
        150000.00,
        157500.00,
        85
    ),
    (
        9,
        'Hoàng tử bé',
        1943,
        60000.00,
        63000.00,
        100
    ),
    (
        10,
        'Nhà giả kim',
        1988,
        95000.00,
        99750.00,
        80
    ),
    (
        11,
        'Số đỏ',
        1936,
        75000.00,
        78750.00,
        50
    ),
    (
        12,
        'Vang bóng một thời',
        1940,
        68000.00,
        71400.00,
        50
    ),
    (
        13,
        'Animal Farm',
        1945,
        98000.00,
        102900.00,
        50
    ),
    (
        14,
        'Kafka bên bờ biển',
        2002,
        145000.00,
        152250.00,
        50
    ),
    (
        15,
        '7 Thói Quen Của Bạn Trẻ Thành Đạt',
        1989,
        105000.00,
        110250.00,
        50
    ),
    (
        16,
        'Vũ trụ trong vỏ hạt dẻ',
        2001,
        160000.00,
        168000.00,
        50
    ),
    (
        17,
        'Xứ Cát (Dune)',
        1965,
        180000.00,
        189000.00,
        30
    ),
    (
        18,
        'Bố Già',
        1969,
        125000.00,
        131250.00,
        40
    ),
    (
        19,
        'Những người khốn khổ',
        1862,
        195000.00,
        204750.00,
        25
    ),
    (
        20,
        'Đi tìm lẽ sống',
        1946,
        88000.00,
        92400.00,
        50
    );

INSERT INTO
    `authors_books` (`book_id`, `author_id`)
VALUES (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 5),
    (6, 6),
    (7, 7),
    (8, 8),
    (9, 9),
    (10, 10),
    (11, 2),
    (12, 2),
    (13, 5),
    (14, 6),
    (15, 7),
    (16, 8),
    (17, 10),
    (18, 10),
    (19, 3),
    (20, 7);

INSERT INTO
    `books_categories` (`book_id`, `category_id`)
VALUES (1, 1),
    (1, 2),
    (2, 1),
    (2, 3),
    (3, 1),
    (3, 8),
    (4, 4),
    (5, 5),
    (5, 8),
    (6, 3),
    (7, 6),
    (8, 7),
    (9, 2),
    (9, 3),
    (10, 3),
    (10, 8),
    (11, 1),
    (11, 3),
    (12, 1),
    (12, 3),
    (13, 5),
    (13, 8),
    (14, 3),
    (15, 6),
    (16, 7),
    (17, 5),
    (18, 3),
    (18, 8),
    (19, 8),
    (20, 6);

-- Import Receipts (5 total)
INSERT INTO
    `import_receipts` (
        `import_receipt_id`,
        `admin_id`,
        `import_date`,
        `total_amount`
    )
VALUES (
        1,
        'admin002',
        '2025-05-10',
        26250000.00
    ),
    (
        2,
        'admin002',
        '2025-05-25',
        25250000.00
    ),
    (
        3,
        'admin001',
        '2025-06-05',
        36050000.00
    ),
    (
        4,
        'admin001',
        '2025-06-20',
        34350000.00
    ),
    (
        5,
        'admin002',
        '2025-07-01',
        15175000.00
    );

INSERT INTO
    `books_import_receipts` (
        `id`,
        `book_id`,
        `import_receipt_id`,
        `quantity`,
        `import_price`
    )
VALUES (1, 1, 1, 150, 70000),
    (2, 2, 1, 150, 45000),
    (3, 3, 1, 75, 90000),
    (4, 4, 1, 50, 170000),
    (5, 5, 2, 100, 110000),
    (6, 7, 2, 120, 85000),
    (7, 9, 2, 100, 60000),
    (8, 6, 3, 60, 135000),
    (9, 8, 3, 90, 150000),
    (10, 10, 3, 80, 95000),
    (11, 12, 3, 50, 68000),
    (12, 11, 4, 50, 75000),
    (13, 13, 4, 50, 98000),
    (14, 14, 4, 50, 145000),
    (15, 15, 4, 50, 105000),
    (16, 16, 4, 50, 160000),
    (17, 17, 5, 30, 189000),
    (18, 18, 5, 40, 131250),
    (19, 19, 5, 25, 204750),
    (20, 20, 5, 50, 92400);

-- Invoices (6 total)
INSERT INTO
    `invoices` (
        `invoice_id`,
        `user_id`,
        `admin_id`,
        `create_at`,
        `total_amount`,
        `paid_amount`,
        `debt_amount`
    )
VALUES (
        1,
        'user001',
        'admin002',
        '2025-06-10',
        1706250.00,
        1500000.00,
        206250.00
    ),
    (
        2,
        'user002',
        'admin002',
        '2025-06-12',
        165375.00,
        165375.00,
        0.00
    ),
    (
        3,
        'user003',
        'admin001',
        '2025-06-15',
        1417500.00,
        1000000.00,
        417500.00
    ),
    (
        4,
        'user001',
        'admin002',
        '2025-06-25',
        220500.00,
        220500.00,
        0.00
    ),
    (
        5,
        'user003',
        'admin001',
        '2025-07-05',
        102900.00,
        0.00,
        102900.00
    ),
    (
        6,
        'user004',
        'admin002',
        '2025-07-08',
        131250.00,
        131250.00,
        0.00
    );

INSERT INTO
    `books_invoices` (
        `id`,
        `invoice_id`,
        `book_id`,
        `quantity`,
        `sell_price`
    )
VALUES (1, 1, 1, 20, 73500),
    (2, 1, 2, 5, 47250),
    (3, 2, 5, 1, 115500),
    (4, 2, 7, 1, 89250),
    (5, 3, 6, 10, 141750),
    (6, 4, 9, 2, 63000),
    (7, 5, 13, 1, 102900),
    (8, 6, 18, 1, 131250);

-- Payment Receipts (3 total)
INSERT INTO
    `payment_receipts` (
        `payment_receipt_id`,
        `payer_id`,
        `admin_id`,
        `create_at`,
        `total_amount`
    )
VALUES (
        1,
        'user001',
        'admin001',
        '2025-06-20',
        106250.00
    ),
    (
        2,
        'user003',
        'admin001',
        '2025-06-30',
        234650.00
    ),
    (
        3,
        'user003',
        'admin001',
        '2025-07-10',
        100000.00
    );

INSERT INTO
    `payment_receipts_invoices` (
        `payment_receipt_id`,
        `invoice_id`,
        `paid_amount`
    )
VALUES (1, 1, 106250.00),
    (2, 3, 234650.00),
    (3, 5, 100000.00);

-- Monthly Inventory Reports (Example for June 2025)
INSERT INTO
    `monthly_inventory_reports` (
        `inventory_report_id`,
        `book_id`,
        `book_name`,
        `report_month`,
        `opening_stock`,
        `stock_increase`,
        `stock_decrease`,
        `closing_stock`
    )
VALUES (
        1,
        1,
        'Dế Mèn phiêu lưu ký',
        '2025-06-30',
        150,
        0,
        20,
        130
    ),
    (
        2,
        2,
        'Lão Hạc',
        '2025-06-30',
        150,
        0,
        5,
        145
    ),
    (
        3,
        5,
        '1984',
        '2025-06-30',
        100,
        0,
        2,
        98
    ),
    (
        4,
        6,
        'Rừng Na Uy',
        '2025-06-30',
        0,
        60,
        10,
        50
    ),
    (
        5,
        7,
        'Đắc nhân tâm',
        '2025-06-30',
        120,
        0,
        1,
        119
    ),
    (
        6,
        8,
        'Lược sử thời gian',
        '2025-06-30',
        0,
        90,
        0,
        90
    ),
    (
        7,
        9,
        'Hoàng tử bé',
        '2025-06-30',
        100,
        0,
        2,
        98
    ),
    (
        8,
        10,
        'Nhà giả kim',
        '2025-06-30',
        0,
        80,
        0,
        80
    );

INSERT INTO
    `monthly_inventory_report_details` (
        `inventory_report_detail_id`,
        `inventory_report_id`,
        `book_id`,
        `report_date`,
        `amount`,
        `type`
    )
VALUES (
        1,
        1,
        1,
        '2025-06-10',
        20,
        'Export'
    ),
    (
        2,
        2,
        2,
        '2025-06-10',
        5,
        'Export'
    ),
    (
        3,
        3,
        5,
        '2025-06-12',
        2,
        'Export'
    ),
    (
        4,
        4,
        6,
        '2025-06-05',
        60,
        'Import'
    ),
    (
        5,
        4,
        6,
        '2025-06-15',
        10,
        'Export'
    ),
    (
        6,
        5,
        7,
        '2025-06-12',
        1,
        'Export'
    ),
    (
        7,
        6,
        8,
        '2025-06-05',
        90,
        'Import'
    ),
    (
        8,
        7,
        9,
        '2025-06-25',
        2,
        'Export'
    ),
    (
        9,
        8,
        10,
        '2025-06-05',
        80,
        'Import'
    );

-- Monthly Debt Report for June 2025 for User 1 & 3
INSERT INTO
    `monthly_debt_reports` (
        `debt_report_id`,
        `user_id`,
        `report_month`,
        `opening_debt`,
        `debt_increase`,
        `debt_payment`,
        `closing_debt`
    )
VALUES (
        1,
        'user001',
        '2025-06-30',
        0.00,
        206250.00,
        106250.00,
        100000.00
    ),
    (
        2,
        'user003',
        '2025-06-30',
        0.00,
        417500.00,
        234650.00,
        182850.00
    );

INSERT INTO
    `monthly_debt_report_details` (
        `debt_report_detail_id`,
        `debt_report_id`,
        `user_id`,
        `report_date`,
        `amount`,
        `type`
    )
VALUES (
        1,
        1,
        'user001',
        '2025-06-10',
        206250.00,
        'Debt'
    ),
    (
        2,
        1,
        'user001',
        '2025-06-20',
        106250.00,
        'Payment'
    ),
    (
        3,
        2,
        'user003',
        '2025-06-15',
        417500.00,
        'Debt'
    ),
    (
        4,
        2,
        'user003',
        '2025-06-30',
        234650.00,
        'Payment'
    );

ALTER TABLE monthly_debt_reports
ADD CONSTRAINT unique_user_month UNIQUE (user_id, report_month);

ALTER TABLE monthly_inventory_reports
ADD CONSTRAINT unique_book_month UNIQUE (book_id, report_month);

/* ---------- BOOK & AUTHOR / CATEGORY (N-N) ---------- */
ALTER TABLE authors_books
ADD CONSTRAINT fk_ab_book FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE ON UPDATE CASCADE,
ADD CONSTRAINT fk_ab_author FOREIGN KEY (author_id) REFERENCES authors (author_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE books_categories
ADD CONSTRAINT fk_bc_book FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE ON UPDATE CASCADE,
ADD CONSTRAINT fk_bc_category FOREIGN KEY (category_id) REFERENCES categories (category_id) ON DELETE CASCADE ON UPDATE CASCADE;

/* ---------- IMPORT RECEIPTS ---------- */
ALTER TABLE books_import_receipts
ADD CONSTRAINT fk_bir_book FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE ON UPDATE CASCADE,
ADD CONSTRAINT fk_bir_receipt FOREIGN KEY (import_receipt_id) REFERENCES import_receipts (import_receipt_id) ON DELETE CASCADE ON UPDATE CASCADE;

/* ---------- INVOICES ---------- */
ALTER TABLE books_invoices
ADD CONSTRAINT fk_binv_book FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE ON UPDATE CASCADE,
ADD CONSTRAINT fk_binv_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (invoice_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE invoices
ADD CONSTRAINT fk_invoice_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
ADD CONSTRAINT fk_invoice_admin FOREIGN KEY (admin_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE;

/* ---------- PAYMENT RECEIPTS ---------- */
ALTER TABLE payment_receipts_invoices
ADD CONSTRAINT fk_prinv_receipt FOREIGN KEY (payment_receipt_id) REFERENCES payment_receipts (payment_receipt_id) ON DELETE CASCADE ON UPDATE CASCADE,
ADD CONSTRAINT fk_prinv_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (invoice_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE payment_receipts
ADD CONSTRAINT fk_pr_user FOREIGN KEY (payer_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
ADD CONSTRAINT fk_pr_admin FOREIGN KEY (admin_id) REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE;

/* ---------- MONTHLY REPORT DETAILS ---------- */
ALTER TABLE monthly_debt_report_details
ADD CONSTRAINT fk_mdrd_report FOREIGN KEY (debt_report_id) REFERENCES monthly_debt_reports (debt_report_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE monthly_inventory_report_details
ADD CONSTRAINT fk_mird_report FOREIGN KEY (inventory_report_id) REFERENCES monthly_inventory_reports (inventory_report_id) ON DELETE CASCADE ON UPDATE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;