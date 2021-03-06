PRAGMA foreign_keys = ON;

CREATE TABLE users(
  uid INTEGER PRIMARY KEY AUTOINCREMENT,
  username VARCHAR(20) UNIQUE NOT NULL,
  fullname VARCHAR(40) NOT NULL,
  email VARCHAR(40) NOT NULL,
  phone_number VARCHAR(40) NOT NULL,
  password VARCHAR(256) NOT NULL,
  created DATETIME DEFAULT CURRENT_TIMESTAMP,
  last_login DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted BIT NOT NULL
);

CREATE TABLE images(
  tag VARCHAR(64) PRIMARY KEY NOT NULL,
  imgname VARCHAR(64) NOT NULL,
  owner VARCHAR(20) NOT NULL,
  checksum VARCHAR(256) NOT NULL,
  created DATETIME DEFAULT CURRENT_TIMESTAMP,
  username_public BIT NOT NULL,
  fullname_public BIT NOT NULL,
  email_public BIT NOT NULL,
  phone_public BIT NOT NULL,
  time_public BIT NOT NULL,
  message TEXT NOT NULL,
  message_encrypted BIT NOT NULL,
  file_path VARCHAR(64),
  is_deleted BIT NOT NULL,
  FOREIGN KEY(owner) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE authorization(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  imgtag VARCHAR(64) NOT NULL,
  username VARCHAR(20) NOT NULL,
  message TEXT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted BIT NOT NULL,
  FOREIGN KEY(username) REFERENCES users(username) ON DELETE CASCADE,
  FOREIGN KEY(imgtag) REFERENCES images(tag) ON DELETE CASCADE
);
