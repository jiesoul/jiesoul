CREATE TABLE api_token
-- API Token
(
    id INTEGER PRIMARY KEY NOT NULL,   -- ID
    user_id INTEGER,                 -- 用户ID
    api_key VARCHAR(512),   
    api_secret VARCHAR(512),           -- key
    create_time DATETIME,                   -- 创建时间
    expires_time DATETIME                 -- 到期时间
);

