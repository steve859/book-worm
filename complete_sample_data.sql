-- cd backend --
-- ./mvnw spring-boot:run --
-- ============================================================================
-- ============================================================================
-- INSERTING SAMPLE DATA FOR BOOKWORM APPLICATION
-- ============================================================================

-- 1. Parameters
INSERT INTO
    `parameters` (`param_name`, `param_value`)
VALUES ('minImportQuantity', 150),
    ('maxStockQuantity', 300),
    ('minStockBeforeImport', 20),
    ('minInvoiceAmount', 20000),
    ('customersCanHaveDebt', 1),
    ('maxDebtAmount', 20000.0),
    ('sell_price_ratio', 1.05);

-- 2. Roles
INSERT INTO
    `roles` (
        `role_name`,
        `role_description`
    )
VALUES (
        'SUPER_ADMIN',
        'Quản trị viên cấp cao nhất'
    ),
    ('ADMIN', 'Nhân viên quản lý'),
    ('USER', 'Khách hàng');

-- 3. Users (Password for all is "123456" hashed with BCrypt)
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
        'superadmin',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Super',
        'Admin',
        'super.admin@bookworm.com',
        '0123456780',
        '1990-01-01',
        0.00
    ),
    (
        'admin002',
        'admin',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Staff',
        'Member',
        'admin.staff@bookworm.com',
        '0123456781',
        '1995-05-10',
        0.00
    ),
    (
        'user001',
        'khachhang1',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'An',
        'Nguyễn Văn',
        'khach.hang1@email.com',
        '0987654321',
        '2000-10-20',
        0.00
    ),
    (
        'user002',
        'khachhang2',
        '$2a$10$N0Iasv5g33zns.OUo23zHuYAlf8lOKxIZEGtc3i2.nPaC23gA8wR6',
        'Bình',
        'Trần Thị',
        'khach.hang2@email.com',
        '0987654322',
        '1998-12-15',
        0.00
    );

-- 4. User-Role Link
INSERT INTO
    `users_roles` (
        `users_user_id`,
        `roles_role_name`
    )
VALUES ('admin001', 'SUPER_ADMIN'),
    ('admin001', 'ADMIN'),
    ('admin002', 'ADMIN'),
    ('user001', 'USER'),
    ('user002', 'USER');

-- 5. Authors
INSERT INTO
    `authors` (`author_id`, `author_name`)
VALUES (1, 'Tô Hoài'),
    (2, 'Nam Cao'),
    (3, 'Nguyễn Du'),
    (4, 'Hồ Chí Minh'),
    (5, 'Xuân Diệu'),
    (6, 'Nguyễn Nhật Ánh'),
    (7, 'J.K. Rowling');

-- 6. Categories
INSERT INTO
    `categories` (
        `category_id`,
        `category_name`
    )
VALUES (1, 'Văn học Việt Nam'),
    (2, 'Sách thiếu nhi'),
    (3, 'Truyện ngắn'),
    (4, 'Thơ ca'),
    (5, 'Lịch sử'),
    (6, 'Tiểu thuyết'),
    (7, 'Fantasy');

-- 7. Books (Initial state before any transactions)
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
        0,
        0,
        0
    ),
    (2, 'Tắt đèn', 1939, 0, 0, 0),
    (
        3,
        'Truyện Kiều',
        1820,
        0,
        0,
        0
    ),
    (
        4,
        'Nhật ký trong tù',
        1960,
        0,
        0,
        0
    ),
    (
        5,
        'Thơ Xuân Diệu',
        1938,
        0,
        0,
        0
    ),
    (
        6,
        'Vang bóng một thời',
        1940,
        0,
        0,
        0
    ),
    (
        7,
        'Kính vạn hoa',
        1995,
        0,
        0,
        0
    ),
    (
        8,
        'Harry Potter and the Sorcerer''s Stone',
        1997,
        0,
        0,
        0
    );

-- 8. Link Books to Authors & Categories
INSERT INTO
    `authors_books` (`book_id`, `author_id`)
VALUES (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 5),
    (6, 2),
    (7, 6),
    (8, 7);

INSERT INTO
    `books_categories` (`book_id`, `category_id`)
VALUES (1, 1),
    (1, 2),
    (2, 1),
    (2, 3),
    (2, 6),
    (3, 1),
    (3, 4),
    (4, 5),
    (4, 4),
    (5, 1),
    (5, 4),
    (6, 1),
    (6, 3),
    (7, 2),
    (7, 6),
    (8, 7);

-- 9. Import Receipt 1: Importing 150 'De Men' and 100 'Tat Den'
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
        17000000.00
    );

INSERT INTO
    `books_import_receipts` (
        `id`,
        `book_id`,
        `import_receipt_id`,
        `quantity`,
        `import_price`
    )
VALUES (1, 1, 1, 150, 70000.00),
    (2, 2, 1, 100, 65000.00);

-- Update books stock and prices after import 1
UPDATE `books`
SET
    `quantity` = 150,
    `import_price` = 70000.00,
    `sell_price` = 73500.00
WHERE
    `book_id` = 1;

UPDATE `books`
SET
    `quantity` = 100,
    `import_price` = 65000.00,
    `sell_price` = 68250.00
WHERE
    `book_id` = 2;

-- 10. Import Receipt 2
INSERT INTO
    `import_receipts` (
        `import_receipt_id`,
        `admin_id`,
        `import_date`,
        `total_amount`
    )
VALUES (
        2,
        'admin002',
        '2025-05-20',
        29250000.00
    );

INSERT INTO
    `books_import_receipts` (
        `id`,
        `book_id`,
        `import_receipt_id`,
        `quantity`,
        `import_price`
    )
VALUES (3, 3, 2, 50, 90000.00),
    (4, 7, 2, 100, 120000.00),
    (5, 8, 2, 75, 170000.00);

-- Update books stock and prices after import 2
UPDATE `books`
SET
    `quantity` = 50,
    `import_price` = 90000.00,
    `sell_price` = 94500.00
WHERE
    `book_id` = 3;

UPDATE `books`
SET
    `quantity` = 100,
    `import_price` = 120000.00,
    `sell_price` = 126000.00
WHERE
    `book_id` = 7;

UPDATE `books`
SET
    `quantity` = 75,
    `import_price` = 170000.00,
    `sell_price` = 178500.00
WHERE
    `book_id` = 8;

-- 11. Invoice 1 (User 1 buys books, incurs debt)
-- Total = (10 * 73500) + (5 * 68250) = 735000 + 341250 = 1076250
-- Paid = 1000000, Debt = 76250
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
        '2025-06-01',
        1076250.00,
        1000000.00,
        76250.00
    );

INSERT INTO
    `books_invoices` (
        `id`,
        `invoice_id`,
        `book_id`,
        `quantity`,
        `sell_price`
    )
VALUES (1, 1, 1, 10, 73500.00),
    (2, 1, 2, 5, 68250.00);

-- Update stock and user debt after invoice 1
UPDATE `books` SET `quantity` = `quantity` - 10 WHERE `book_id` = 1;

UPDATE `books` SET `quantity` = `quantity` - 5 WHERE `book_id` = 2;

UPDATE `users`
SET
    `debt_amount_user` = 76250.00
WHERE
    `user_id` = 'user001';

-- 12. Invoice 2 (User 2 buys books, no debt)
-- Total = (2 * 94500) + (1 * 178500) = 189000 + 178500 = 367500
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
        2,
        'user002',
        'admin002',
        '2025-06-05',
        367500.00,
        367500.00,
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
VALUES (3, 2, 3, 2, 94500.00),
    (4, 2, 8, 1, 178500.00);

-- Update stock after invoice 2
UPDATE `books` SET `quantity` = `quantity` - 2 WHERE `book_id` = 3;

UPDATE `books` SET `quantity` = `quantity` - 1 WHERE `book_id` = 8;

-- 13. Payment Receipt (User 1 pays off some debt)
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
        '2025-06-15',
        50000.00
    );

-- Update user debt after payment
UPDATE `users`
SET
    `debt_amount_user` = `debt_amount_user` - 50000.00
WHERE
    `user_id` = 'user001';
