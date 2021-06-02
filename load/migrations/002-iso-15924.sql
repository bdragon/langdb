CREATE TABLE iso_15924 (
  alpha text NOT NULL PRIMARY KEY,
  num   text NOT NULL UNIQUE,
  pva   text NOT NULL,
  name  text NOT NULL
);
