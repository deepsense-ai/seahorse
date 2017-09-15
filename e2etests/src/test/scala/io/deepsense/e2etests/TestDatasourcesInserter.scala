package io.deepsense.e2etests

import java.util.UUID

import io.deepsense.api.datasourcemanager.model._

trait TestDatasourcesInserter {

  self: SeahorseIntegrationTestDSL =>

  def insertDatasourcesForTest(): Unit = {
    for (DatasourceForTest(id, ds) <- datasourcesNeededForTests) yield {
      insertDatasource(id, ds)
    }
  }

  private def datasourcesNeededForTests = List(
    DatasourceForTest(
      uuid = "425c1536-7039-47d7-8db4-5c4e8bb9d742",
      csvFromHttp("transactions", fromS3("transactions"))),
    DatasourceForTest(
      uuid = "3f64a431-6248-48df-826c-b662b076b135",
      csvFromHttp("small state names", fromS3("StateNamesSmall"))
    )
  )

  private def csvFromHttp(name: String, url: String): DatasourceParams = {
    (new DatasourceParams)
      .name(name)
      .datasourceType(DatasourceType.EXTERNALFILE)
      .visibility(Visibility.PUBLICVISIBILITY)
      .externalFileParams(
        (new ExternalFileParams)
          .url(url)
          .fileFormat(FileFormat.CSV)
          .csvFileFormatParams(
            (new CsvFileFormatParams)
              .convert01ToBoolean(false)
              .separatorType(CsvSeparatorType.COMMA)
              .includeHeader(true)))
  }

  private def fromS3(name: String): String = s"https://s3.amazonaws.com/workflowexecutor/examples/data/$name.csv"

  private case class DatasourceForTest(uuid: UUID, datasourceParams: DatasourceParams)

  private object DatasourceForTest {
    def apply(uuid: String, datasourceParams: DatasourceParams): DatasourceForTest = {
      new DatasourceForTest(UUID.fromString(uuid), datasourceParams)
    }
  }
}
