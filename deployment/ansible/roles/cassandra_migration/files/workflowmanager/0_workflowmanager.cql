create keyspace if not exists workflowmanager
  with replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

use workflowmanager;

create table if not exists workflows (
  id uuid,
  workflow text,
  results text,
  results_upload_time timestamp,
  deleted boolean,
  created timestamp,
  updated timestamp,
  primary key (id));

create index if not exists deleted_workflows ON workflows(deleted);

create table if not exists workflowresults (
   id uuid,
   results text,
   primary key (id));

create table if not exists notebooks (
   workflow_id uuid,
   node_id uuid,
   notebook text,
   primary key (workflow_id, node_id));

create table if not exists workflow_state (
  workflow_id uuid,
  node_id uuid,
  update_time timestamp,
  state text,
  reports text,
  primary key(workflow_id, node_id)
);
