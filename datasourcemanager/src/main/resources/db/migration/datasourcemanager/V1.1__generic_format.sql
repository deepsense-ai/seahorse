ALTER TABLE "datasourcemanager"."datasource"
  ADD "sparkGenericDataSourceFormat" VARCHAR
  DEFAULT (NULL);


CREATE TABLE "datasourcemanager"."sparkoption" (
  "id" UUID NOT NULL PRIMARY KEY,
  "key" VARCHAR NOT NULL,
  "value" VARCHAR NOT NULL,
  "datasource_id" UUID NOT NULL,
);

ALTER TABLE "datasourcemanager"."sparkoption"
ADD FOREIGN KEY ("datasource_id")
REFERENCES "datasource"("id")
ON DELETE CASCADE;