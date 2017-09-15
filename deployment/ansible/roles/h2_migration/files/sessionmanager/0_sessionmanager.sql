create table "SESSIONS" (
  "id" UUID NOT NULL PRIMARY KEY,
  "batch_id" INTEGER,
  "version" INTEGER NOT NULL
);

create unique index "batch_id" on "SESSIONS" ("batch_id");
