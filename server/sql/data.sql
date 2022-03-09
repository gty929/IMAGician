PRAGMA foreign_keys = ON;

INSERT INTO users(username, fullname, email, password, is_deleted)
VALUES 
('awdeorio', 'Andrew DeOrio', 'awdeorio@umich.edu', 'sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0),
('jflinn', 'Jason Flinn', 'jflinn@umich.edu', 'sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0),
('michjc', 'Michael Cafarella', 'michjc@umich.edu', 'sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0),
('jag', 'H.V. Jagadish', 'jag@umich.edu', 'sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8', 0);

INSERT INTO images(tag, imgname, owner, is_anonymous, checksum, public_info, is_deleted)
VALUES
(123456, 'testimg', 'awdeorio', 0, 12345678, 'nothing', 0);

INSERT INTO authorization(imgid, username, status)
VALUES
(1, 'jflinn', 'PENDING');