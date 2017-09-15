/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.models

import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}

import io.deepsense.models.entities.Entity

trait EntityMatchers {

  def tenantId(expectedValue: String) = new HavePropertyMatcher[Entity, String] {
    def apply(entity: Entity) = HavePropertyMatchResult(
        entity.tenantId == expectedValue,
        "tenantId",
        expectedValue,
        entity.tenantId)
  }

  def id(expectedValue: Entity.Id) = new HavePropertyMatcher[Entity, Entity.Id] {
    def apply(entity: Entity) = HavePropertyMatchResult(
        entity.id == expectedValue,
        "id",
        expectedValue,
        entity.id)
  }

  def dClass(expectedValue: String) = new HavePropertyMatcher[Entity, String] {
    def apply(entity: Entity) = HavePropertyMatchResult(
        entity.dClass == expectedValue,
        "dClass",
        expectedValue,
        entity.dClass)
  }

  def name(expectedValue: String) = new HavePropertyMatcher[Entity, String] {
    def apply(entity: Entity) = HavePropertyMatchResult(
        entity.name == expectedValue,
        "name",
        expectedValue,
        entity.name)
  }

  def description(expectedValue: String) = new HavePropertyMatcher[Entity, String] {
    def apply(entity: Entity) = HavePropertyMatchResult(
        entity.description == expectedValue,
        "description",
        expectedValue,
        entity.description)
  }

  def saved(expectedValue: Boolean) = new HavePropertyMatcher[Entity, Boolean] {
    def apply(entity: Entity) = HavePropertyMatchResult(
        entity.saved == expectedValue,
        "saved",
        expectedValue,
        entity.saved)
  }

}
