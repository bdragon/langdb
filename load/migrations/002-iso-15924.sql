CREATE TABLE iso_15924 (
  alpha    text    NOT NULL PRIMARY KEY,
  num      text    NOT NULL UNIQUE,
  name     text    NOT NULL,
  pva      text        NULL,
  added_on date    NOT NULL,
  reserved boolean NOT NULL DEFAULT false
);
