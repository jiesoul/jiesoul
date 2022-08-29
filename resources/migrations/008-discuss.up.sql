CREATE TABLE discuss
-- 评论
(
    discuss_id INTEGER PRIMARY KEY,  -- 评论ID
    create_time DATETIME,    -- 评论日期
    like_count INT,    -- 点赞数
    discuss_user BIGINT,    -- 发表用户
    article_id BIGINT,    -- 评论文章ID
    content VARCHAR(3072),    -- 评论内容
    parent_id BIGINT    -- 父评论ID 
);