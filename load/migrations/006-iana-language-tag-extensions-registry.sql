CREATE TABLE subtag_ext (
  id            text NOT NULL PRIMARY KEY,
  description   text NOT NULL,
  comments      text     NULL,
  added         date NOT NULL,
  rfc           text NOT NULL,
  authority     text NOT NULL,
  contact_email text NOT NULL,
  mailing_list  text NOT NULL,
  url           text NOT NULL
);
