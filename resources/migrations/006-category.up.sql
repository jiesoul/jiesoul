CREATE TABLE category
-- 分类
(
    category_id INTEGER PRIMARY KEY,  -- 分类ID
    category_name VARCHAR(64),    -- 分类名称
    alias_name VARCHAR(64),    -- 分类别名
    description VARCHAR(128),    -- 分类描述
    parennt_id BIGINT,    -- 父分类ID
    create_time DATETIME    -- 创建时间
);