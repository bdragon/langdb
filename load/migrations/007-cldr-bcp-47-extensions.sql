CREATE TABLE subtag_ext_key_value_type (
  value_type text NOT NULL PRIMARY KEY
);

INSERT INTO subtag_ext_key_value_type
  (value_type)
VALUES
  ('single'),
  ('multiple'),
  ('incremental'),
  ('any');

-- ---------------------------------------------------------------------------

CREATE TABLE subtag_ext_key (
  ext_id      text    NOT NULL REFERENCES subtag_ext (id),
  id          text    NOT NULL,
  description text        NULL,
  deprecated  boolean NOT NULL DEFAULT false,
  preferred   text        NULL,
  alias       text        NULL,
  value_type  text        NULL REFERENCES subtag_ext_key_value_type (value_type),
  since       text        NULL,
  PRIMARY KEY (ext_id, id)
);

-- ---------------------------------------------------------------------------

CREATE TABLE subtag_ext_key_type (
  ext_id      text    NOT NULL,
  key_id      text    NOT NULL,
  id          text    NOT NULL,
  description text        NULL,
  deprecated  boolean NOT NULL DEFAULT false,
  preferred   text        NULL,
  alias       text        NULL,
  since       text        NULL,
  PRIMARY KEY (ext_id, key_id, id),
  FOREIGN KEY (ext_id, key_id) REFERENCES subtag_ext_key (ext_id, id)
);

-- ---------------------------------------------------------------------------

CREATE TABLE subtag_ext_attr (
  id          text    NOT NULL PRIMARY KEY,
  description text    NOT NULL,
  deprecated  boolean NOT NULL DEFAULT false,
  preferred   text        NULL,
  since       text        NULL
);
