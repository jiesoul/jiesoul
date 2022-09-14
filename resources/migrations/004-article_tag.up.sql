CREATE TABLE article_tag
-- 文章标签
(
    atr_Id INTEGER PRIMARY KEY,  -- 引用id
    article_id BIGINT,    -- 文章id
    tag_id BIGINT    -- 标签id
    );