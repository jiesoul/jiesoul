CREATE TABLE users
-- 用户
(
    id INTEGER PRIMARY KEY NOT NULL,   -- 用户ID
    username VARCHAR(128),                 -- 用户名
    nickname VARCHAR(128),             -- 用户昵称
    password VARCHAR(64),                        -- 用户密码
    email VARCHAR(64),                      -- 用户邮箱
    avatar VARCHAR(128),                    -- 用户头像
    create_time DATETIME,                   -- 注册时间
    birthday DATE,                          -- 用户生日
    age INT,                                -- 用户年龄
    roles VARCHAR(128),                      -- 用户角色 admin
    phone VARCHAR(32)                 -- 用户手机号
);

