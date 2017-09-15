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

package ai.deepsense.commons.json.envelope

/**
 *  Envelope for objects serializable to JSON
 *
 *  Such object can be later encoded into following JSON using EnvelopeJson[Writer/Format]
 *
 *   { [label]: [JSON representation of the content] }
 *
 *  where [label] is a string defined in EnvelopeJson[Writer/Format].
 *  There is also EnvelopeJsonReader which allows for decoding.
 */
case class Envelope[T](content: T)
