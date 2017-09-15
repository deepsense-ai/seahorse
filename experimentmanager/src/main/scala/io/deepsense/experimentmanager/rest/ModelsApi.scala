package io.deepsense.experimentmanager.rest

import java.net.URI

import scala.util.{Failure, Success}

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
import io.deepsense.deeplang.DSHdfsClient
import io.deepsense.deploymodelservice.DeployModelJsonProtocol._
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

  def createExecutionContext(): io.deepsense.deeplang.ExecutionContext = {
    val executionContext = new io.deepsense.deeplang.ExecutionContext
    val sparkConf = new SparkConf()
    sparkConf.setAppName("Spark DeepSense Akka PoC")
    sparkConf.set("spark.executor.memory", "512m")
    val sparkContext = new SparkContext(sparkConf)
    executionContext.sqlContext = new SQLContext(sparkContext)
    new DSHdfsClient(new DFSClient(new URI("/"), new Configuration()))
    executionContext
  }


  override def route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        pathPrefix(pathPrefixMatcher) {
          path(JavaUUID / "deploy") { id =>
            post {
              withUserContext { context =>
                onComplete(context) {
                  case Success(uc: UserContext) => {
                    import scala.concurrent.duration._
                    implicit val timeout = 5.seconds
                    onComplete(deployModel.deploy(id, uc, createExecutionContext())) {
                      case Success(r) => complete(r)
                      case Failure(e) => failWith(e)
                    }
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
