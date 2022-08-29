CREATE TABLE oauth_token
-- API Token
(
    id INTEGER PRIMARY KEY NOT NULL,  -- ID
    user_id INTEGER,                        -- 用户ID
    oauth_name VARCHAR(60),            -- OAuth 名称
    oauth_id VARCHAR(60),
    oauth_access_token VARCHAR(512), 
    create_time DATETIME,              -- 创建时间
    expires_time DATETIME                 -- 到期时间
);

