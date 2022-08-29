CREATE TABLE tag
-- 标签
(
    tag_id INTEGER PRIMARY KEY, -- 标签ID 
    tag_name VARCHAR(64),    -- 标签名称 
    alias_name VARCHAR(64),    -- 标签别名 
    description VARCHAR(128),    -- 标签描述 
    create_time DATETIME    -- 创建时间
    );