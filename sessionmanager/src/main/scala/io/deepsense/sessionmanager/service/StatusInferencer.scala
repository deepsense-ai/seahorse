/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import org.joda.time.DateTime

import io.deepsense.sessionmanager.service.EventStore.Event

trait StatusInferencer {

  /**
    * Calculates the current Status knowing that the last event
    * occurred at some point of time.
    * @param event The last event that happened.
    * @param at A point of time in which to calculate the Status.
    * @return The Status at a point of time 'at' in context of
    *         the last event.
    */
  def statusFromEvent(event: Event, at: DateTime): Status.Value
}
