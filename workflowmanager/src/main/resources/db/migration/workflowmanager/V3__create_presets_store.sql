CREATE TABLE IF NOT EXISTS "PRESETS" (
  "id" BIGINT NOT NULL AUTO_INCREMENT,
  "name" VARCHAR NOT NULL,
  "clusterType" VARCHAR NOT NULL,
  "uri" VARCHAR NOT NULL,
  "userIP" VARCHAR NOT NULL,
  "hadoopUser" VARCHAR,
  "isEditable" BOOLEAN DEFAULT TRUE,
  "isDefault" BOOLEAN DEFAULT FALSE,
  "driverMemory" VARCHAR,
  "executorMemory" VARCHAR,
  "totalExecutorCores" INTEGER,
  "executorCores" INTEGER,
  "numExecutors" INTEGER,
  "params" VARCHAR,

  PRIMARY KEY ("id")
);

INSERT INTO PRESETS(name, clusterType, uri, userIP, hadoopUser, isEditable, isDefault, driverMemory, executorMemory,
totalExecutorCores, executorCores, numExecutors, params)
VALUES ('default', 'local', 'local[*]', '', NULL, FALSE, TRUE, '1G', '2G', 2, 2, 2, '');


