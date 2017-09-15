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

package io.deepsense.commons.json

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

import io.deepsense.api.datasourcemanager.model.Datasource
import io.deepsense.commons.rest.client.datasources.DatasourceTypes.DatasourceList

object DatasourceListJsonProtocol {
  private val t = new TypeToken[java.util.LinkedList[Datasource]](){}.getType()
  private val gson = new GsonBuilder().serializeNulls().create()

  def fromString(json: String): DatasourceList = {
    val ds: java.util.List[Datasource] = gson.fromJson(json, t)
    ds.toList
  }

  def toString(datasources: DatasourceList): String = {
    gson.toJson(datasources.asJava)
  }

}
