CREATE TABLE user_token
-- 用户Token
(
    id INTEGER PRIMARY KEY NOT NULL,   -- ID
    user_id INTEGER,                 -- 用户ID
    token VARCHAR(512) unique,             -- Token
    create_time DATETIME,                   -- 创建时间
    expires_time DATETIME                 -- 到期时间
);

