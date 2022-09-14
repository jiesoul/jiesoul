CREATE TABLE api_token
-- API Token
(
    id INTEGER PRIMARY KEY,   -- ID
    user_id INTEGER,                 -- 用户ID
    api_key VARCHAR(512) unique,   
    api_secret VARCHAR(512) unique,           -- key
    create_time DATETIME,                   -- 创建时间
    expires_time DATETIME                 -- 到期时间
);

