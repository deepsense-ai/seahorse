create table "datasourcemanager"."datasource" (
    "id" UUID NOT NULL PRIMARY KEY,
    "name" VARCHAR NOT NULL,
    "downloadUri" VARCHAR,
    "datasourceType" VARCHAR NOT NULL,
    "jdbcUrl" VARCHAR,
    "jdbcDriver" VARCHAR,
    "jdbcTable" VARCHAR,
    "filePath" VARCHAR,
    "fileScheme" VARCHAR,
    "fileFormat" VARCHAR,
    "fileCsvSeparator" VARCHAR,
    "fileCsvIncludeHeader" BOOLEAN
)
