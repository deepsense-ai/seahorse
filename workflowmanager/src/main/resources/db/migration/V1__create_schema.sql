CREATE TABLE IF NOT EXISTS WORKFLOWS(
    "id" UUID,
    "workflow" VARCHAR2,
    "deleted" BOOLEAN,
    "created" BIGINT,
    "updated" BIGINT,
    "owner_id" VARCHAR2,
    "owner_name" VARCHAR2,
    PRIMARY KEY ("id"));

CREATE TABLE IF NOT EXISTS NOTEBOOKS(
    "workflow_id" UUID,
    "node_id" UUID,
    "notebook" VARCHAR2,
    PRIMARY KEY ("workflow_id", "node_id"));

CREATE TABLE IF NOT EXISTS WORKFLOW_STATES(
    "workflow_id" UUID,
    "node_id" UUID,
    "update_time" BIGINT,
    "results" VARCHAR2,
    "reports" VARCHAR2,
    PRIMARY KEY ("workflow_id", "node_id")
);
