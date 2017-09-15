/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.params

import java.lang.reflect.Modifier

import spray.json._

import io.deepsense.commons.utils.CollectionExtensions._
import io.deepsense.deeplang.exceptions.DeepLangException
import io.deepsense.deeplang.params.exceptions.ParamValueNotProvidedException

/**
 * Everything that inherits this trait declares that it contains parameters.
 * Parameters are discovered by reflection.
 * This trait also provides method for managing values and default values of parameters.
 */
trait Params extends Serializable {

  def paramsToJson: JsValue = JsArray(params.map {
    case param =>
      val default = getDefault(param)
      param.toJson(default)
  }: _*)

  /**
   * Json describing values associated to parameters.
   * If there is no value given for parameter, default value is returned.
   * If it's also not there, parameter won't appear in json at all.
   */
  def paramValuesToJson: JsValue = {
    val fields = for (param <- params) yield {
      getOrDefaultOption(param).map {
        case paramValue => param.name -> param.anyValueToJson(paramValue)
      }
    }
    JsObject(fields.flatten.toMap)
  }

  /**
   * Sequence of paramPairs for this class parsed from Json.
   * If name of parameter is unknown, exception will be thrown.
   * JsNull is treated as empty object.
   * JsNull as value of parameter is ignored.
   */
  def paramPairsFromJson(jsValue: JsValue): Seq[ParamPair[_]] = jsValue match {
    case JsObject(map) =>
      val pairs = for ((label, value) <- map) yield {
        (paramsByName.get(label), value) match {
          case (Some(parameter), JsNull) => None
          case (Some(parameter), _) => Some(ParamPair(
            parameter.asInstanceOf[Param[Any]],
            parameter.valueFromJson(value)))
          case (None, _) => throw new DeserializationException(
            s"Cannot fill parameters schema with $jsValue: unknown parameter label $label.")
        }
      }
      pairs.flatten.toSeq
    case JsNull => Seq.empty
    case _ => throw new DeserializationException(s"Cannot fill parameters schema with $jsValue:" +
      s"object expected.")
  }

  /**
   * Sets param values based on provided json.
   * If name of parameter is unknown, exception will be thrown.
   * JsNull is treated as empty object.
   * JsNull as value of parameter is ignored.
   */
  def setParamsFromJson(jsValue: JsValue): this.type = set(paramPairsFromJson(jsValue): _*)

  val params: Array[Param[_]]

  /**
   * Allows to declare parameters order conveniently and makes sure
   * that all parameters are declared.
   * Additionally check uniqueness of names.
   */
  protected def declareParams(params: Param[_]*): Array[Param[_]] = {
    val declaredParamSet = params.toSet
    val reflectionParamSet = getParamsByReflection.toSet
    require(declaredParamSet == reflectionParamSet,
      s"[${getClass.getName}] Not all parameters {${reflectionParamSet.mkString(", ")}}" +
        s" were declared in {${declaredParamSet.mkString(", ")}}")
    require(params.map(_.name).hasUniqueValues, "Names of parameters are not unique")
    params.toArray
  }

  private def getParamsByReflection: Array[Param[_]] = {
    val methods = this.getClass.getMethods
    methods.filter { m =>
      Modifier.isPublic(m.getModifiers) &&
        classOf[Param[_]].isAssignableFrom(m.getReturnType) &&
        m.getParameterTypes.isEmpty
    }.map(m => m.invoke(this).asInstanceOf[Param[_]])
  }

  private lazy val paramsByName: Map[String, Param[_]] =
    params.map { case param => param.name -> param }.toMap

  def validateParams: Vector[DeepLangException] = {
    params.filter(isDefined).flatMap { param =>
      param.asInstanceOf[Param[Any]].validate($(param))
    }.toVector
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
  def sameAs(other: Params): Boolean =
    other.getClass == this.getClass && other.extractParamMap() == this.extractParamMap()

  protected def copyValues[T <: Params](to: T, extra: ParamMap = ParamMap.empty): T = {
    val map = extractParamMap(extra)
    params.foreach { param =>
      if (map.contains(param) && to.hasParam(param.name)) {
        to.set(param.name, map(param))
      }
    }
    to
  }

  private val paramMap: ParamMap = ParamMap.empty

  private val defaultParamMap: ParamMap = ParamMap.empty
}
