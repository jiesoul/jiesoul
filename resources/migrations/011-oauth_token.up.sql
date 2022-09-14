CREATE TABLE oauth_token
-- API Token
(
    id INTEGER PRIMARY KEY,  -- ID
    user_id INTEGER,                        -- 用户ID
    oauth_name VARCHAR(60) unique,            -- OAuth 名称
    oauth_id VARCHAR(60) unique,
    oauth_access_token VARCHAR(512) unique, 
    create_time DATETIME,              -- 创建时间
    expires_time DATETIME                 -- 到期时间
);

