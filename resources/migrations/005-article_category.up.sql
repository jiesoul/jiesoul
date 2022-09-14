CREATE TABLE article_category
-- 文章分类
(
    acr_id INTEGER PRIMARY KEY,  -- 引用id
    article_id BIGINT,    -- 文章id
    category_id BIGINT    -- 类目id
    );