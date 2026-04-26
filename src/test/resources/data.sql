INSERT INTO users (
    id,
    name,
    username,
    password,
    enabled,
    created_at,
    updated_at
)
VALUES (
    1,
    'Gustavo Teste',
    'test123',
    '$2a$10$xPFurUd1nGt9A6FETaqPbuDVCA6jvlspA4EZtwgkOwjP5lU6aHVcK',
    1,
    '2026-04-08 17:30:00',
    '2026-04-08 17:30:00'
);

INSERT INTO role(id, name) VALUES (1, 'ROLE_ADMIN');
INSERT INTO role(id, name) VALUES (2, 'ROLE_MANAGER');
INSERT INTO role(id, name) VALUES (3, 'ROLE_EMPLOYEE');

INSERT INTO users_role (users_id, role_id) VALUES (1,1);
INSERT INTO users_role (users_id, role_id) VALUES (1,2);
INSERT INTO users_role (users_id, role_id) VALUES (1,3);