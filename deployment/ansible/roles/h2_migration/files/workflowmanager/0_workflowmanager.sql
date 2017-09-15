create table if not exists WORKFLOWS (
  id UUID,
  workflow VARCHAR2,
  deleted BOOLEAN,
  created BIGINT,
  updated BIGINT,
  primary key (id));

create table if not exists NOTEBOOKS (
   workflow_id UUID,
   node_id UUID,
   notebook VARCHAR2,
   primary key (workflow_id, node_id));

create table if not exists WORKFLOW_STATES (
  workflow_id UUID,
  node_id UUID,
  update_time BIGINT,
  state VARCHAR2,
  reports VARCHAR2,
  primary key(workflow_id, node_id)
);
