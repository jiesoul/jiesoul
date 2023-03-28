CREATE TABLE article
-- 文章
(
    id INTEGER PRIMARY KEY NOT NULL,    -- 博文ID
    push_data DATETIME,                 -- 发布日期
    author VARCHAR(32),                 -- 发表用户
    title VARCHAR(1024),                -- 博文标题
    like_count INT,                     -- 点赞数
    comment_count INT,                  -- 评论数
    read_count INT,                     -- 浏览量
    top_flag VARCHAR(1),                -- 是否置顶
    create_time DATETIME,               -- 创建时间
    article_summary VARCHAR(1024)       -- 文章摘要
);