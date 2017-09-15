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

package ai.deepsense.deeplang.catalogs.doperable

/**
 * Describes either class or trait.
 */
trait TypeDescriptor

/**
 * Describes class.
 * @param name display name of class
 * @param parent direct superclass of described class, if any
 * @param traits all direct supertraits of described class
 */
case class ClassDescriptor(
    name: String,
    parent: Option[String],
    traits: List[String])
  extends TypeDescriptor

/**
 * Describes trait.
 * @param name display name of trait
 * @param parents all direct supertraits of described trait
 */
case class TraitDescriptor(
    name: String,
    parents: List[String])
  extends TypeDescriptor
