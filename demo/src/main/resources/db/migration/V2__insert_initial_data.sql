-- Insert default roles
INSERT INTO roles (name) VALUES 
    ('ROLE_USER'),
    ('ROLE_MODERATOR'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;

-- Assign admin role to admin user
-- Lưu ý: Thay thế 1 bằng ID thực tế của user admin trong database của bạn
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' 
AND r.name = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;
