CREATE TABLE article_comment
-- 评论
(
    id INTEGER PRIMARY KEY,  -- 评论ID
    create_time DATETIME,    -- 评论日期
    like_count INT,    -- 点赞数
    username TEXT,    -- 发表用户
    user_email TEXT,  -- 用户邮箱
    article_id BIGINT,    -- 评论文章ID
    content VARCHAR(3072),    -- 评论内容
    pid BIGINT    -- 父评论ID 
);