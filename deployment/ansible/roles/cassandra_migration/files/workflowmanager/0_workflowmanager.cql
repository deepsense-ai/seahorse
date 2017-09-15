create keyspace if not exists workflowmanager
  with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

use workflowmanager;

create table if not exists workflows(
  id uuid,
  workflow text,
  results text,
  deleted boolean,
  primary key (id));

create index if not exists deleted_workflows ON workflows(deleted);

create table if not exists workflowresults (
   id uuid,
   results text,
   primary key (id));
