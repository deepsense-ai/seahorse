/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest

import scala.concurrent.ExecutionContext

import com.google.inject.Inject
import spray.routing.Directives

import io.deepsense.experimentmanager.app.ExperimentManager
import io.deepsense.experimentmanager.app.models.{Experiment, InputExperiment}
import io.deepsense.experimentmanager.app.rest.actions.Action
import io.deepsense.experimentmanager.rest.RestComponent

/**
 * Exposes Experiment Manager through a REST API.
 */
class RestApi @Inject() (experimentManager: ExperimentManager)(implicit ec: ExecutionContext)
    extends Directives with RestComponent {

  def route = {
    import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._

    path("") {
      get {
        complete {
          "Try /experiments & /experiments/:id  eg. /experiments/b93b65f4-0a92-4ced-b244-98b79674cde8"
        }
      }
    } ~
      path("experiments" / JavaUUID) { idParameter =>
        val experimentId = Experiment.Id(idParameter)
        get {
          complete {
            experimentManager.get(experimentId)
          }
        } ~
          put {
            entity(as[Experiment]) { experiment =>
              if (experiment.id.equals(experimentId)) {
                complete {
                  experimentManager.update(experiment)
                }
              } else {
                complete {
                  "Bad request" // TODO
                }
              }
            }
          } ~
          delete {
            complete {
              experimentManager.delete(experimentId)
              "deleted"
            }
          } ~
          post {
            entity(as[Action]) { action =>
              complete {
                action.run(experimentId, experimentManager).toString
              }
            }
          }
      } ~
      path("experiments") {
        post {
          entity(as[InputExperiment]) { experiment =>
            complete {
              experimentManager.create(experiment)
            }
          }
        } ~
          get {
            parameters('limit.?, 'page.?, 'status.?) { (limit, page, status) =>
              val limitInt = limit.map(_.toInt)
              val pageInt = page.map(_.toInt)
              val statusEnum = status.map(Experiment.Status.withName)
              complete {
                experimentManager.list(limitInt, pageInt, statusEnum).map(_.toList)
              }
            }
          }
      }

  }
}
