/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.json.envelope

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
