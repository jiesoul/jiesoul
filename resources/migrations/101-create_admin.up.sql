INSERT INTO users(username, password, email, create_time, roles, age, birthday, phone) 
    VALUES ('jiesoul', 'bcrypt+sha512$fb9025b12d7049f875c482e352608d51$12$4cf00b1ad5e2afd40805f298f91025814b95cdec76a49ccc',
            'jiesoul@qq.com',CURRENT_TIMESTAMP,'amdmin,user', '1', '2022-08-01', '1388888888');