CREATE TABLE category
-- 分类
(
    id INTEGER PRIMARY KEY,  -- 分类ID
    name VARCHAR(64) unique,    -- 分类名称
    description VARCHAR(128),    -- 分类描述
    pid BIGINT   -- 父分类ID
);