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

package ai.deepsense.seahorse.datasource

import scalaz.syntax.validation._
import ai.deepsense.commons.service.api.CommonApiExceptions
import ai.deepsense.commons.service.api.CommonApiExceptions.ApiException
import scalaz.Validation

package object converters {

 def validateDefined[T](fieldName: String, field: Option[T]) = field match {
    case Some(value) => value.success
    case None => CommonApiExceptions.fieldMustBeDefined(fieldName).failure
  }

  def validationIdentity[T, A] (field: Option[T], function: T => Validation[ApiException, Option[A]])
  : Validation[ApiException, Option[A]] =
    field match {
    case Some(value) => function(value)
    case None => None.success
  }

}
