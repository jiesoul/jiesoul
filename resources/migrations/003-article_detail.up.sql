CREATE TABLE article_detail
-- 文章详情
(
    article_detail_id INTEGER PRIMARY KEY,  -- 文章详情id
    content_md TEXT,    -- 文章markdown内容
    content_html TEXT,   -- 文章html内容
    article_id BIGINT    -- 文章id
);
