/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.commons.rest

import com.google.inject.Inject
import com.google.inject.name.Named
import spray.routing.Route

import ai.deepsense.commons.auth.usercontext.TokenTranslator
import ai.deepsense.commons.buildinfo.BuildInfo

class VersionApi @Inject() (
    @Named("componentName") val componentName: String,
    val tokenTranslator: TokenTranslator)
  extends RestApi with RestComponent {

  override def route: Route = {
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        path("version") {
          get {
            complete(s"name: $componentName, version: ${BuildInfo.version}, " +
              s"scalaVersion: ${BuildInfo.scalaVersion}, sbtVersion: ${BuildInfo.sbtVersion}, " +
              s"gitCommitId: ${BuildInfo.gitCommitId}")
          }
        }
      }
    }
  }
}
