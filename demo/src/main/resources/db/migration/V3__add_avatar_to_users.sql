-- Add avatar column to users table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS avatar VARCHAR(255);

-- Update existing rows with default avatar if needed
-- UPDATE users SET avatar = 'default-avatar.png' WHERE avatar IS NULL;
