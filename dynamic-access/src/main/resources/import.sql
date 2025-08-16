-- Users
INSERT INTO app_users (id, username, password, role)
VALUES (1, 'admin', 'admin', 'admin');

INSERT INTO app_users (id, username, password, role)
VALUES (2, 'alice', 'alice', 'user');

-- Reset user sequence
SELECT setval('app_users_SEQ', (SELECT MAX(id) FROM app_users));

-- Groups
INSERT INTO app_groups (id, name)
VALUES (1, 'project-managers');

-- Reset group sequence
SELECT setval('app_groups_SEQ', (SELECT MAX(id) FROM app_groups));

-- Alice belongs to project-managers
INSERT INTO user_groups (user_id, group_id)
VALUES (2, 1);

-- Demo Projects 
INSERT INTO projects (id, name, description)
VALUES 
  (1, 'Apollo', 'Internal knowledge base migration project'),
  (2, 'Hermes', 'Next-generation messaging platform'),
  (3, 'Zephyr', 'Performance tuning and optimization effort');

-- Reset sequence to start after manually inserted IDs
SELECT setval('projects_SEQ', (SELECT MAX(id) FROM projects));

