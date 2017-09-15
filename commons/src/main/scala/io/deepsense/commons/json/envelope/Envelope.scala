/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.commons.json.envelope

import spray.json.JsonFormat

/**
 *  Envelope for objects serializable to JSON
 *
 *  Such object can be later encoded into following JSON using EnvelopeJsonFormat
 *
 *   { [label]: [JSON representation of the content] }
 *
 *  where [label] is a string defined in EnvelopeJsonFormat.
 */
case class Envelope[T : JsonFormat](content: T)
