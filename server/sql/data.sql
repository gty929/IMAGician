PRAGMA foreign_keys = ON;

INSERT INTO users(username, fullname, email, password)
VALUES 
('awdeorio', 'Andrew DeOrio', 'awdeorio@umich.edu', 'sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8'),
('jflinn', 'Jason Flinn', 'jflinn@umich.edu', 'sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8'),
('michjc', 'Michael Cafarella', 'michjc@umich.edu', 'sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8'),
('jag', 'H.V. Jagadish', 'jag@umich.edu', 'sha512$a45ffdcc71884853a2cba9e6bc55e812$c739cef1aec45c6e345c8463136dc1ae2fe19963106cf748baf87c7102937aa96928aa1db7fe1d8da6bd343428ff3167f4500c8a61095fb771957b4367868fb8');


INSERT INTO posts(filename, owner)
VALUES
('122a7d27ca1d7420a1072f695d9290fad4501a41.jpg', 'awdeorio'),
('ad7790405c539894d25ab8dcf0b79eed3341e109.jpg', 'jflinn'),
('9887e06812ef434d291e4936417d125cd594b38a.jpg', 'awdeorio'),
('2ec7cf8ae158b3b1f40065abfb33e81143707842.jpg', 'jag');

INSERT INTO likes(owner, postid)
VALUES
('awdeorio', 1),
('michjc', 1),
('jflinn', 1),
('awdeorio', 2),
('michjc', 2),
('awdeorio', 3);

INSERT INTO following(username1, username2)
VALUES
('awdeorio', 'jflinn'),
('awdeorio', 'michjc'),
('jflinn', 'awdeorio'),
('jflinn', 'michjc'),
('michjc', 'awdeorio'),
('michjc', 'jag'),
('jag', 'michjc');

INSERT INTO comments(owner, postid, text)
VALUES
('awdeorio', 3, '#chickensofinstagram'),
('jflinn', 3, 'I <3 chickens'),
('michjc', 3, 'Cute overload!'),
('awdeorio', 2, 'Sick #crossword'),
('jflinn', 1, 'Walking the plank #chickensofinstagram'),
('awdeorio', 1, 'This was after trying to teach them to do a #crossword'),
('jag', 4, 'Saw this on the diag yesterday!');
