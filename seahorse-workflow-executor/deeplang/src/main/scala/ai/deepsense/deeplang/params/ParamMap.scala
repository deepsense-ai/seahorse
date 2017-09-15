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

import scala.collection.mutable

case class ParamMap private[deeplang] (private val map: mutable.Map[Param[Any], Any])
  extends Serializable {

  def put[T](param: Param[T], value: T): this.type = put(param -> value)

  def put(paramPairs: ParamPair[_]*): this.type = {
    paramPairs.foreach { p =>
      map(p.param.asInstanceOf[Param[Any]]) = p.value
    }
    this
  }

  def get[T](param: Param[T]): Option[T] = {
    map.get(param.asInstanceOf[Param[Any]]).asInstanceOf[Option[T]]
  }

  def getOrElse[T](param: Param[T], default: T): T = {
    get(param).getOrElse(default)
  }

  def apply[T](param: Param[T]): T = {
    get(param).getOrElse {
      throw new NoSuchElementException(s"Cannot find param ${param.name}.")
    }
  }

  def contains(param: Param[_]): Boolean = {
    map.contains(param.asInstanceOf[Param[Any]])
  }

  def remove[T](param: Param[T]): Option[T] = {
    map.remove(param.asInstanceOf[Param[Any]]).asInstanceOf[Option[T]]
  }

  def ++(other: ParamMap): ParamMap = {
    ParamMap(this.map ++ other.map)
  }

  def ++=(other: ParamMap): this.type = {
    this.map ++= other.map
    this
  }

  def toSeq: Seq[ParamPair[_]] = {
    map.toSeq.map { case (param, value) =>
      ParamPair(param, value)
    }
  }

  def size: Int = map.size
}

object ParamMap {

  def apply(): ParamMap = ParamMap(mutable.Map.empty[Param[Any], Any])

  def empty: ParamMap = ParamMap()

  def apply(paramPairs: ParamPair[_]*): ParamMap = {
    ParamMap().put(paramPairs: _*)
  }
}
