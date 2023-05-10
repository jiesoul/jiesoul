CREATE TABLE article
-- 文章
(
    id VARCHAR PRIMARY KEY NOT NULL,    -- 博文ID
    author VARCHAR(32),                 -- 发表用户
    title VARCHAR(1024),                -- 博文标题
    create_time DATETIME,               -- 创建时间
    like_count INT DEFAULT 0,           -- 点赞数
    comment_count INT DEFAULT 0,        -- 评论数
    read_count INT DEFAULT 0,           -- 浏览量
    top_flag INT DEFAULT 0,               -- 是否置顶
    category_id INT DEFAULT 0,           -- 分类
    push_time DATETIME,                 -- 发布时间
    push_flag INT DEFAULT 0,
    summary VARCHAR(1024),               -- 文章摘要
    tags VARCHAR(1024)                   -- 标签
);