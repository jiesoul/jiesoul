CREATE TABLE article_tag
-- 文章tag
(
    id INTEGER PRIMARY KEY,  -- 引用id
    article_id VARCHAR(20),    -- 文章id
    tag_id BIGINT    -- tag id
    );