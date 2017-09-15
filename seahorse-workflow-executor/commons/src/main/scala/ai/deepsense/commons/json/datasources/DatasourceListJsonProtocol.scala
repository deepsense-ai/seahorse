/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.json.datasources

import java.lang.reflect.Type

import com.google.gson._
import com.google.gson.reflect.TypeToken

import ai.deepsense.api.datasourcemanager.model.Datasource
import ai.deepsense.commons.rest.client.datasources.DatasourceTypes.DatasourceList
import org.joda

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object DatasourceListJsonProtocol {
  private val t = new TypeToken[java.util.LinkedList[Datasource]](){}.getType

  private val DateTimeType = new TypeToken[joda.time.DateTime](){}.getType

  private val gson = new GsonBuilder()
    .serializeNulls()
    .registerTypeAdapter(DateTimeType, new DatetimeJsonProtocol)
    .create()


  def fromString(json: String): DatasourceList = {
    val ds: java.util.List[Datasource] = gson.fromJson(json, t)
    ds.toList
  }

  def toString(datasources: DatasourceList): String = {
    gson.toJson(datasources.asJava)
  }
}

class DatetimeJsonProtocol
  extends JsonDeserializer[joda.time.DateTime]
  with JsonSerializer[joda.time.DateTime] {


  override def deserialize(
      json: JsonElement,
      typeOfT: Type,
      context: JsonDeserializationContext): joda.time.DateTime = {
    val fmt = joda.time.format.ISODateTimeFormat.dateTimeParser()
    fmt.parseDateTime(json.getAsString)
  }

  override def serialize(
      src: joda.time.DateTime,
      typeOfSrc: Type,
      context: JsonSerializationContext): JsonElement = {
    val fmt = joda.time.format.ISODateTimeFormat.dateTime()
    new JsonPrimitive(fmt.print(src))
  }
}
