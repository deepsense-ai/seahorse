/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.conversion

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.auth.AuthorizatorProvider
import io.deepsense.commons.auth.usercontext.UserContext
import io.deepsense.deeplang.doperations.{FileToDataFrame, ReadFile, SaveDataFrame}
import io.deepsense.entitystorage.EntityStorageClientFactory
import io.deepsense.experimentmanager.ExperimentManager
import io.deepsense.experimentmanager.exceptions.FileNotFoundException
import io.deepsense.graph.{Edge, Graph, Node}
import io.deepsense.models.entities.Entity
import io.deepsense.models.experiments.{Experiment, InputExperiment}

class FileConverter @Inject() (
    authorizatorProvider: AuthorizatorProvider,
    esClientFactory: EntityStorageClientFactory,
    @Named("roles.experiments.create") roleCreate: String,
    @Named("entityStorage.actorSystemName") esActorSystemName: String,
    @Named("entityStorage.hostname") esHostname: String,
    @Named("entityStorage.port") esPort: Int,
    @Named("entityStorage.actorName") esActorName: String,
    @Named("entityStorage.timeoutSeconds") esTimeoutSeconds: Int)
    (implicit ec: ExecutionContext) {

  val entityStorageClient =
    esClientFactory.create(esActorSystemName, esHostname, esPort, esActorName, esTimeoutSeconds)

  def convert(entityId: Entity.Id, userContextFuture: Future[UserContext],
      em: ExperimentManager) : Future[Experiment] = {
    authorizatorProvider.forContext(userContextFuture).withRole(roleCreate) { userContext =>
      import scala.concurrent.duration._
      implicit val timeout = esTimeoutSeconds.seconds

      entityStorageClient.getEntityData(userContext.tenantId, entityId) flatMap {
        case Some(entity) => em.create(InputExperiment(
          name = "Convert CSV to DataFrame",
          graph = createGraph(entity.dataReference.savedDataPath, entity.info.name))
        ) flatMap { experiment =>
          em.launch(experiment.id, Seq())
        }
        case None => Future.failed(FileNotFoundException(entityId))
      }
    }
  }

  private def createGraph(filePath: String, fileName: String): Graph = {
    val readFile = ReadFile(filePath, ReadFile.unixSeparatorValue)
    val fileToDataFrame = FileToDataFrame(FileToDataFrame.CSV, ",")
    val saveDataFrame = SaveDataFrame(fileName)

    val readFileNode = Node(Node.Id.randomId, readFile)
    val fileToDataFrameNode = Node(Node.Id.randomId, fileToDataFrame)
    val saveDataFrameNode = Node(Node.Id.randomId, saveDataFrame)

    val nodes = Set(readFileNode, fileToDataFrameNode, saveDataFrameNode)

    val edges = Set(
      Edge(readFileNode, 0, fileToDataFrameNode, 0),
      Edge(fileToDataFrameNode, 0, saveDataFrameNode, 0)
    )
    Graph(nodes, edges)
  }
}
