-- Remove type column from categories table
ALTER TABLE bookstore.categories DROP COLUMN IF EXISTS type;
