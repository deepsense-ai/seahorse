package io.deepsense.experimentmanager.rest

import java.net.URI

import scala.util.{Try, Failure, Success}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.DFSClient
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import spray.routing.PathMatchers
import scala.concurrent.ExecutionContext

import io.deepsense.commons.auth.AuthorizatorProvider
import io.deepsense.commons.auth.usercontext.{TokenTranslator, UserContext}
import io.deepsense.commons.rest.{RestApi, RestComponent}
import io.deepsense.deeplang
import io.deepsense.deeplang.{DSHdfsClient, ExecutionContext => DExecutionContext}
import io.deepsense.deploymodelservice.DeployModelJsonProtocol._
import io.deepsense.entitystorage.EntityStorageClientFactoryImpl
import io.deepsense.experimentmanager.rest.actions.DeployModel


class ModelsApi @Inject()(
    val tokenTranslator: TokenTranslator,
    authorizatorProvider: AuthorizatorProvider,
    deployModel: DeployModel,
    @Named("models.api.prefix") apiPrefix: String)
   (implicit ec: ExecutionContext)
  extends RestApi
  with RestComponent {

  require(StringUtils.isNotBlank(apiPrefix))
  private val pathPrefixMatcher = PathMatchers.separateOnSlashes(apiPrefix)

  val deeplangContext: deeplang.ExecutionContext = {
    val ctx = new DExecutionContext
    ctx.hdfsClient = new DSHdfsClient(
      new DFSClient(new URI("hdfs://ds-dev-env-master:8020"), new Configuration()))
    val esFactory = EntityStorageClientFactoryImpl()
    ctx.entityStorageClient =
      Try(esFactory.create("root-actor-system", "127.0.0.1", 2552, "EntitiesApiActor", 10))
        .getOrElse(null)
    ctx
  }

  override def route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        pathPrefix(pathPrefixMatcher) {
          path(JavaUUID / "deploy") { id =>
            post {
              withUserContext { context =>
                onComplete(context) {
                  case Success(uc: UserContext) =>
                    import scala.concurrent.duration._
                    implicit val timeout = 15.seconds
                    onComplete(deployModel.deploy(id, uc, deeplangContext)) {
                      case Success(r) => complete(r)
                      case Failure(e) => failWith(e)
                    }
                  case Failure(e) => failWith(e)
                }
              }
            }
          }
        }
      }
    }
  }
}
