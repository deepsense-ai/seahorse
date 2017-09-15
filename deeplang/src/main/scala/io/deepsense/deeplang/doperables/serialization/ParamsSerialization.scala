/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperables.serialization

import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue}

import io.deepsense.deeplang.catalogs.doperable.exceptions.NoParameterlessConstructorInClassException
import io.deepsense.deeplang.params.Params
import io.deepsense.deeplang.{ExecutionContext, TypeUtils}

trait ParamsSerialization {

  self: Params =>

  def saveObjectWithParams(ctx: ExecutionContext, path: String): Unit = {
    saveMetadata(ctx, path)
    saveParams(ctx, path)
  }

  def loadObjectWithParams(ctx: ExecutionContext, path: String): this.type = {
    loadAndSetParams(ctx, path)
  }

  protected def saveMetadata(ctx: ExecutionContext, path: String) = {
    val metadataFilePath = ParamsSerialization.metadataFilePath(path)
    val metadataJson = JsObject(
      ParamsSerialization.classNameKey -> JsString(this.getClass.getName)
    )
    JsonObjectPersistence.saveJsonToFile(ctx, metadataFilePath, metadataJson)
  }

  protected def saveParams(ctx: ExecutionContext, path: String): Unit = {
    val paramsFilePath = ParamsSerialization.paramsFilePath(path)
    JsonObjectPersistence.saveJsonToFile(ctx, paramsFilePath, paramValuesToJson)
  }

  private def loadAndSetParams(ctx: ExecutionContext, path: String): this.type = {
    setParams(loadParams(ctx, path))
  }

  protected def loadParams(ctx: ExecutionContext, path: String): JsValue = {
    JsonObjectPersistence.loadJsonFromFile(ctx, ParamsSerialization.paramsFilePath(path))
  }

  private def setParams(paramsJson: JsValue): this.type = {
    this.set(paramPairsFromJson(paramsJson): _*)
  }
}

object ParamsSerialization {
  val classNameKey = "className"
  val paramsFileName = "params"
  val metadataFileName = "metadata"

  def load(ctx: ExecutionContext, path: String): Loadable = {
    import DefaultJsonProtocol._
    val metadataPath = metadataFilePath(path)
    val metadataJson: JsObject =
      JsonObjectPersistence.loadJsonFromFile(ctx, metadataPath).asJsObject
    val className = metadataJson.fields(classNameKey).convertTo[String]
    val clazz: Class[_] = Class.forName(className)
    val loadable = TypeUtils.createInstance(TypeUtils.constructorForClass(clazz)
        .getOrElse(throw new NoParameterlessConstructorInClassException(clazz.getCanonicalName))
    ).asInstanceOf[Loadable]
    loadable.load(ctx, path)
  }

  def metadataFilePath(path: String): String = {
    PathsUtils.combinePaths(path, metadataFileName)
  }

  def paramsFilePath(path: String): String = {
    PathsUtils.combinePaths(path, paramsFileName)
  }
}
