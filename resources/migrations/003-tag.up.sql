CREATE TABLE tag
-- 标签
(
    id INTEGER PRIMARY KEY, -- 标签ID 
    name VARCHAR(64) unique,    -- 标签名称 
    description VARCHAR(128)    -- 标签描述 
    );