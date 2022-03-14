PRAGMA foreign_keys = ON;

INSERT INTO users(username, fullname, email, phone_number, password, is_deleted)
VALUES
('hechenxi', 'Chenxi He', 'hechenxi@umich.edu', '(734)123-4567','sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0),
('panyu', 'Yu Pan', 'panyu@umich.edu', '(734)123-4567','sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0),
('liuxs', 'Xueshen Liu', 'liuxs@umich.edu', '(734)123-4567','sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0),
('tyg', 'Tianyao Gu', 'tyg@umich.edu', '(734)123-4567','sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0),
('yyzjason', 'Yingzhuo Yu', 'tyg@umich.edu', '(734)123-4567','sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0),
('korolxu', 'Puchen Xu', 'korolxu@umich.edu', '(734)123-4567','sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0);

INSERT INTO images(tag, imgname, owner, checksum, fullname_public, email_public, phone_public, time_public, message, message_encrypted, file_path, is_deleted)
VALUES
('123456', 'placeholder_1.png', 'hechenxi','114514',1,0,0,0,'Hello world', 0,'',0),
('314159', 'placeholder_2.png', 'hechenxi','114514',1,1,0,0,'Hello world Again', 0,'2ec7cf8ae158b3b1f40065abfb33e81143707842',0),
('87wrnx', 'placeholder_3.png', 'panyu','114514',0,0,0,1,'Hello world by py', 0,'',0),
('1fserv7', 'placeholder_4.png', 'tyg','114514',0,0,0,1,'Hello world by gty', 0,'',0),
('142857', 'placeholder_4.png', 'liuxs','114514',0,0,0,1,'Hello world by lxs', 0,'',0),
('w837r9her', 'placeholder_5.png', 'liuxs','114514',0,0,0,1,'Hello world by lxs again', 0,'',0);

INSERT INTO authorization(imgtag, username, message, status, is_deleted)
VALUES
('123456', 'tyg','I want to have access', 'PENDING', 0),
('314159', 'yyzjason','I want to have access', 'PENDING', 0),
('314159', 'panyu', 'I want to have access', 'GRANTED', 0),
('142857', 'panyu', 'I want to have access', 'GRANTED', 0),
('w837r9her', 'korolxu', 'I want to have access', 'REJECTED', 0),
('314159', 'korolxu', 'I want to have access', 'REJECTED', 0);