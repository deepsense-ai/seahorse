create table "schedulingmanager"."workflow_schedule" (
    "id" UUID NOT NULL PRIMARY KEY,
    "cron" VARCHAR NOT NULL,
    "workflow_id" UUID NOT NULL,
    "email_for_reports" VARCHAR NOT NULL,
    "preset_id" BIGINT NOT NULL
)
