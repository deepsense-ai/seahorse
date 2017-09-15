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

package ai.deepsense.deeplang.params

import java.lang.reflect.Modifier

import spray.json._

import ai.deepsense.commons.utils.CollectionExtensions._
import ai.deepsense.commons.utils.Logging
import ai.deepsense.deeplang.doperables.descriptions.{HasInferenceResult, ParamsInferenceResult}
import ai.deepsense.deeplang.exceptions.{DeepLangException, DeepLangMultiException}
import ai.deepsense.deeplang.params.exceptions.ParamValueNotProvidedException
import ai.deepsense.deeplang.params.multivalue.MultipleValuesParam
import ai.deepsense.deeplang.params.wrappers.spark._
import scala.reflect.runtime.{universe => ru}

import ai.deepsense.models.json.graph.GraphJsonProtocol.GraphReader

/**
 * Everything that inherits this trait declares that it contains parameters.
 * Parameters are discovered by reflection.
 * This trait also provides method for managing values and default values of parameters.
 */
trait Params extends Serializable with HasInferenceResult with DefaultJsonProtocol with Logging {

  def paramsToJson: JsValue = JsArray(params.map {
    case param =>
      val default = getDefault(param)
      param.toJson(default)
  }: _*)

  /**
   * Json describing values associated to parameters.
   */
  def paramValuesToJson: JsValue = {
    val fields = for (param <- params) yield {
      get(param).map {
        case paramValue => param.name -> param.anyValueToJson(paramValue)
      }
    }
    JsObject(fields.flatten.toMap)
  }

  /**
   * Sequence of paramPairs for this class, parsed from Json.
   * If a name of a parameter is unknown, it's ignored
   * JsNull is treated as empty object.
   * JsNull as value of parameter is ignored.
   */
  def paramPairsFromJson(jsValue: JsValue, graphReader: GraphReader): Seq[ParamPair[_]] = jsValue match {
    case JsObject(map) =>
      val pairs = for ((label, value) <- map) yield {
        (paramsByName.get(label), value) match {
          case (Some(parameter), JsNull) => None
          case (Some(parameter), _) =>
            if (isMultiValueParam(parameter, value)) {
              getMultiValueParam(value, parameter)
            } else {
              Some(ParamPair(
                parameter.asInstanceOf[Param[Any]],
                parameter.valueFromJson(value, graphReader)))
            }
          case (None, _) => {
            // Currently frontend might occasionally send invalid params
            // (like removing public param from custom transformer or DS-2671)
            // In that case we are doing nothing.
            logger.warn(s"Field $label is not defined in schema. Ignoring...")
            None
          }
        }
      }
      pairs.flatten.toSeq
    case JsNull => Seq.empty
    case _ => throw objectExpectedException(jsValue)
  }

  /**
   * Sequence of params without values for this class, parsed from Json.
   * If a name of a parameter is unknown, it's ignored
   * JsNull is treated as empty object.
   * JsNull as a value of a parameter unsets param's value.
   */
  def noValueParamsFromJson(jsValue: JsValue): Seq[Param[_]] = jsValue match {
    case JsObject(map) =>
      val pairs = for ((label, value) <- map) yield {
        (paramsByName.get(label), value) match {
          case (p @ Some(parameter), JsNull) => p
          case (Some(parameter), _) => None
          case (None, _) => {
            // Currently frontend might occasionally send invalid params
            // (like removing public param from custom transformer or DS-2671)
            // In that case we are doing nothing.
            logger.info(s"Field $label is not defined in schema. Ignoring...")
            None
          }
        }
      }
      pairs.flatten.toSeq
    case JsNull => Seq.empty
    case _ => throw objectExpectedException(jsValue)
  }

  private def getMultiValueParam(value: JsValue, parameter: Param[_]): Option[ParamPair[_]] = {
    parameter match {
      case _: IntParamWrapper[_] |
           _: LongParamWrapper[_] |
           _: FloatParamWrapper[_] |
           _: DoubleParamWrapper[_] |
           _: NumericParam =>
        createMultiValueParam[Double](value, parameter)
      case _ => None
    }
  }

  private def isMultiValueParam(parameter: Param[_], value: JsValue): Boolean = {
    parameter.isGriddable && MultipleValuesParam.isMultiValParam(value)
  }

  private def createMultiValueParam[T](
      value: JsValue,
      parameter: Param[_])(implicit format: JsonFormat[T]): Option[ParamPair[T]] = {
    val multiValParam = MultipleValuesParam.fromJson[T](value)
    Some(ParamPair(parameter.asInstanceOf[Param[T]], multiValParam.values))
  }

  /**
   * Sets param values based on provided json.
   * If a name of a parameter is unknown, it's ignored
   * JsNull is treated as empty object.
   *
   * When ignoreNulls = false, JsNull as a value of a parameter unsets param's value.
   * When ignoreNulls = true, parameters with JsNull values are ignored.
   */
  def setParamsFromJson(jsValue: JsValue, graphReader: GraphReader, ignoreNulls: Boolean = false): this.type = {
    set(paramPairsFromJson(jsValue, graphReader): _*)
    if (!ignoreNulls) {
      noValueParamsFromJson(jsValue).foreach(clear)
    }
    this
  }

  def params: Array[Param[_]]

  private lazy val paramsByName: Map[String, Param[_]] =
    params.map { case param => param.name -> param }.toMap

  protected def customValidateParams: Vector[DeepLangException] = Vector.empty

  /**
   * Validates params' values by:
   * 1. testing whether the params have values set (or default values),
   * 2. testing whether the values meet the constraints,
   * 3. testing custom validations, possibly spanning over multiple params.
   */
  def validateParams: Vector[DeepLangException] = {
    val singleParameterErrors = params.flatMap { param =>
      if (isDefined(param)) {
        val paramValue: Any = $(param)
        val anyTypeParam: Param[Any] = param.asInstanceOf[Param[Any]]
        anyTypeParam.validate(paramValue)
      } else {
        Vector(new ParamValueNotProvidedException(param.name))
      }
    }.toVector
    val customValidationErrors = customValidateParams
    singleParameterErrors ++ customValidationErrors
  }

  /**
    * Validates Params entities that contain dynamic parameters' values.
    * Validation errors are wrapped in DeepLangMultiException.
    */
  def validateDynamicParams(params: Params*): Unit = {
    val validationResult = params.flatMap(param => param.validateParams).toVector
    if (validationResult.nonEmpty) {
      throw DeepLangMultiException(validationResult)
    }
  }

  final def isSet(param: Param[_]): Boolean = {
    paramMap.contains(param)
  }

  final def isDefined(param: Param[_]): Boolean = {
    defaultParamMap.contains(param) || paramMap.contains(param)
  }

  private def hasParam(paramName: String): Boolean = {
    params.exists(_.name == paramName)
  }

  private def getParam(paramName: String): Param[Any] = {
    params.find(_.name == paramName).getOrElse {
      throw new NoSuchElementException(s"Param $paramName does not exist.")
    }.asInstanceOf[Param[Any]]
  }

  protected final def set[T](param: Param[T], value: T): this.type = {
    set(param -> value)
  }

  private final def set(param: String, value: Any): this.type = {
    set(getParam(param), value)
  }

  protected[deeplang] final def set(paramPair: ParamPair[_]): this.type = {
    paramMap.put(paramPair)
    this
  }

  protected[deeplang] final def set(paramPairs: ParamPair[_]*): this.type = {
    paramMap.put(paramPairs: _*)
    this
  }

  protected[deeplang] final def set(paramMap: ParamMap): this.type = {
    set(paramMap.toSeq: _*)
    this
  }

  protected final def clear(param: Param[_]): this.type = {
    paramMap.remove(param)
    this
  }

  final def get[T](param: Param[T]): Option[T] = paramMap.get(param)

  final def getOrDefaultOption[T](param: Param[T]): Option[T] = get(param).orElse(getDefault(param))

  final def getOrDefault[T](param: Param[T]): T = getOrDefaultOption(param).getOrElse {
    throw ParamValueNotProvidedException(param.name)
  }

  protected final def $[T](param: Param[T]): T = getOrDefault(param)

  protected def setDefault[T](param: Param[T], value: T): this.type = {
    defaultParamMap.put(param -> value)
    this
  }

  protected def setDefault(paramPairs: ParamPair[_]*): this.type = {
    paramPairs.foreach { p =>
      setDefault(p.param.asInstanceOf[Param[Any]], p.value)
    }
    this
  }

  final def getDefault[T](param: Param[T]): Option[T] = {
    defaultParamMap.get(param)
  }

  final def hasDefault[T](param: Param[T]): Boolean = {
    defaultParamMap.contains(param)
  }

  final def extractParamMap(extra: ParamMap = ParamMap.empty): ParamMap = {
    defaultParamMap ++ paramMap ++ extra
  }

  def replicate(extra: ParamMap = ParamMap.empty): this.type = {
    val that = this.getClass.getConstructor().newInstance().asInstanceOf[this.type]
    copyValues(that, extra)
  }

  /**
   * Compares 'this' and 'other' params. Objects are equal when they are of the same
   * class and their parameters have the same values set.
   * @return True, if 'this' and 'other' are the same.
   */
  def sameAs(other: Params): Boolean = {
    other.getClass == this.getClass && other.paramValuesToJson == this.paramValuesToJson
  }

  // TODO Mutability leakage - it's possible to mutate object `to` internals from outside.
  // there should be protected copyFrom from instead (not `to`).
  def copyValues[T <: Params](to: T, extra: ParamMap = ParamMap.empty): T = {
    val map = paramMap ++ extra
    params.foreach { param =>
      if (map.contains(param) && to.hasParam(param.name)) {
        to.set(param.name, map(param))
      }
    }
    to
  }

  override def inferenceResult: Option[ParamsInferenceResult] = {
    Some(ParamsInferenceResult(
      schema = paramsToJson,
      values = paramValuesToJson
    ))
  }

  private def objectExpectedException(jsValue: JsValue): DeserializationException =
    new DeserializationException(s"Cannot fill parameters schema with $jsValue object expected.")

  private[deeplang] def paramMap: ParamMap = _paramMap
  private val _paramMap: ParamMap = ParamMap.empty

  private[deeplang] def defaultParamMap: ParamMap = _defaultParamMap
  private val _defaultParamMap: ParamMap = ParamMap.empty

}
