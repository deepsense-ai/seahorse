/**
 * Copyright 2018 Astraea, Inc.
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
package ai.deepsense.deeplang.catalogs.doperations

/**
  * Carrier of known operation categories.
  */
class DCategoryCatalog {
  var categories: Seq[DOperationCategory] = Seq.empty
  def registerDCategory(category: DOperationCategory): Unit =
    categories = categories :+ category

  override def toString: String = {
    val buf = new StringBuilder
    buf.append(getClass.getSimpleName)
    buf.append(" {\n")
    categories.foreach { c =>
      buf.append("    ")
      buf.append(c.pathFromRoot.map(_.name).mkString("->"))
      buf.append('\n')
    }
    buf.append("}")
    buf.toString
  }
}

object DCategoryCatalog {
  def apply(): DCategoryCatalog = new DCategoryCatalog()
}
