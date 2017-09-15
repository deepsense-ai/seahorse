CREATE TABLE IF NOT EXISTS "PRESETS" (
  "id" BIGINT NOT NULL AUTO_INCREMENT,
  "name" VARCHAR NOT NULL,
  "clusterType" VARCHAR NOT NULL,
  "uri" VARCHAR NOT NULL,
  "userIP" VARCHAR NOT NULL,
  "hadoopUser" VARCHAR,
  "isEditable" BOOLEAN DEFAULT TRUE,
  "isDefault" BOOLEAN DEFAULT FALSE,
  "executorMemory" VARCHAR,
  "totalExecutorCores" INTEGER,
  "executorCores" INTEGER,
  "numExecutors" INTEGER,
  "params" VARCHAR,

  PRIMARY KEY ("id")
);

INSERT INTO PRESETS(name, clusterType, uri, userIP, hadoopUser, isEditable, isDefault, executorMemory,
totalExecutorCores, executorCores, numExecutors, params)
VALUES ('default', 'local', '', '', NULL, FALSE, TRUE, '2G', 2, 2, 2, '--num-executors 2 --verbose');


