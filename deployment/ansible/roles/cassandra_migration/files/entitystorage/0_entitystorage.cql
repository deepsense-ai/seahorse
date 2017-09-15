create keyspace if not exists entitystorage
  with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

use entitystorage;

create table if not exists entities (
  tenantid text,
  id uuid,
  name text,
  description text,
  dclass text, created timestamp,
  updated timestamp,
  url text,
  metadata text,
  saved boolean,
  report text,
  primary key (tenantid, id));

create index if not exists saved_entities on entities(saved);
