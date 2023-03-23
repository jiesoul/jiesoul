CREATE TABLE article_comment
-- 评论
(
    id INTEGER PRIMARY KEY,  -- 评论ID
    create_time DATETIME,    -- 评论日期
    like_count INT,    -- 点赞数
    discuss_user BIGINT,    -- 发表用户
    article_id BIGINT,    -- 评论文章ID
    content VARCHAR(3072),    -- 评论内容
    pid BIGINT    -- 父评论ID 
);