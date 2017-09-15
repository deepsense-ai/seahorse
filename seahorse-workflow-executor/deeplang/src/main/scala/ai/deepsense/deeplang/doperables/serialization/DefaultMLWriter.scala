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


package ai.deepsense.deeplang.doperables.serialization

import org.apache.hadoop.fs.Path
import org.apache.spark.SparkContext
import org.apache.spark.ml.param.{ParamPair, Params}
import org.apache.spark.ml.util.MLWriter
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import ai.deepsense.deeplang.doperables.Transformer
import ai.deepsense.sparkutils.ML.MLWriterWithSparkContext

class DefaultMLWriter[T <: Params](instance: T) extends MLWriter with MLWriterWithSparkContext {

  def saveImpl(path: String): Unit = {
    val modelPath = Transformer.modelFilePath(path)
    saveMetadata(instance, path, sc)
    CustomPersistence.save(sparkContext, instance, modelPath)
  }

  /**
    * Saves metadata + Params to: path + "/metadata"
    *  - class
    *  - timestamp
    *  - sparkVersion
    *  - uid
    *  - paramMap
    *  - (optionally, extra metadata)
    *
    * @param extraMetadata  Extra metadata to be saved at same level as uid, paramMap, etc.
    * @param paramMap  If given, this is saved in the "paramMap" field.
    *                  Otherwise, all [[org.apache.spark.ml.param.Param]]s are encoded using
    *                  [[org.apache.spark.ml.param.Param.jsonEncode()]].
    */
  // Copied from org.apache.spark.ml.util.DefaultParamWriter.
  // We need to be consistent with Spark Format, but this method is private.
  private def saveMetadata(
      instance: Params,
      path: String,
      sc: SparkContext,
      extraMetadata: Option[JObject] = None,
      paramMap: Option[JValue] = None): Unit = {
    val uid = instance.uid
    val cls = instance.getClass.getName
    val params = instance.extractParamMap().toSeq.asInstanceOf[Seq[ParamPair[Any]]]
    val jsonParams = paramMap.getOrElse(render(params.map { case ParamPair(p, v) =>
      p.name -> parse(p.jsonEncode(v))
    }.toList))
    val basicMetadata = ("class" -> cls) ~
      ("timestamp" -> System.currentTimeMillis()) ~
      ("sparkVersion" -> sc.version) ~
      ("uid" -> uid) ~
      ("paramMap" -> jsonParams)
    val metadata = extraMetadata match {
      case Some(jObject) =>
        basicMetadata ~ jObject
      case None =>
        basicMetadata
    }
    val metadataPath = new Path(path, "metadata").toString
    val metadataJson = compact(render(metadata))
    sc.parallelize(Seq(metadataJson), 1).saveAsTextFile(metadataPath)
  }
}
