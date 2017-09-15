/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.experimentmanager.app.rest.json

import org.scalatest._
import spray.json._

import io.deepsense.deeplang.catalogs.doperable.{ClassDescriptor, HierarchyDescriptor, TraitDescriptor}
import io.deepsense.experimentmanager.app.rest.json.RestJsonProtocol._

class RestJsonProtocolSpec extends FlatSpec {

  "TraitDescriptor" should "be correctly serialized to json" in {
    val traitDescriptor: TraitDescriptor = TraitDescriptor("Sorted", List("Iterable", "Comparable"))

    val json = traitDescriptor.toJson

    assert(expectedJsTrait(traitDescriptor) == json)
  }

  it should "be correctly serialized to json when parents are empty" in {
    val traitDescriptor: TraitDescriptor = TraitDescriptor("Sorted", List())

    val json = traitDescriptor.toJson

    assert(expectedJsTrait(traitDescriptor) == json)
  }

  it should "be correctly deserialized from json" in {
    val descriptor: TraitDescriptor = TraitDescriptor("Sorted", List("Iterable", "Comparable"))
    val json = expectedJsTrait(descriptor)

    val fromJson: TraitDescriptor = json.convertTo[TraitDescriptor]

    assert(descriptor == fromJson)
  }

  it should "be correctly deserialized from json when parents are empty" in {
    val descriptor: TraitDescriptor = TraitDescriptor("Sorted", List())
    val json = expectedJsTrait(descriptor)

    val fromJson: TraitDescriptor = json.convertTo[TraitDescriptor]

    assert(descriptor == fromJson)
  }

  "ClassDescriptor" should "be correctly serialized to json when no parent" in {
    val classDescriptor: ClassDescriptor = ClassDescriptor("Model", None, List("T1", "T2"))

    val json = classDescriptor.toJson

    assert(expectedJsClass(classDescriptor) == json)
  }

  it should "be correctly serialized to json when without traits" in {
    val classDescriptor: ClassDescriptor = ClassDescriptor("Model", Some("parent"), List())

    val json = classDescriptor.toJson

    assert(expectedJsClass(classDescriptor) == json)
  }

  it should "be correctly deserialized from json when no parent" in {
    val descriptor: ClassDescriptor = ClassDescriptor("Model", None, List("T1", "T2"))
    val json = expectedJsClass(descriptor)

    val fromJson = json.convertTo[ClassDescriptor]

    assert(descriptor == fromJson)
  }

  it should "be correctly deserialized from json when without traits" in {
    val descriptor: ClassDescriptor = ClassDescriptor("Model", Some("parent"), List())
    val json = expectedJsClass(descriptor)

    val fromJson = json.convertTo[ClassDescriptor]

    assert(descriptor == fromJson)
  }

  "HierarchyDescriptor" should "be correctly serialized to json" in {
    val hierarchyDescriptor: HierarchyDescriptor = HierarchyDescriptor(
      Map(
        "Sorted" -> TraitDescriptor("Sorted", List("Iterable", "Comparable")),
        "Trainable" -> TraitDescriptor("Trainable", List())
      ),
      Map(
        "DataFrame" -> ClassDescriptor("DataFrame", Some("Data"), List("Data")),
        "Whatever" -> ClassDescriptor("Whatever", None, List()))
    )

    val json = hierarchyDescriptor.toJson

    assert(expectedJsHierarchy(hierarchyDescriptor) == json)
  }

  it should "be correctly serialized to json when empty" in {
    val hierarchyDescriptor: HierarchyDescriptor = HierarchyDescriptor(Map(), Map())

    val json = hierarchyDescriptor.toJson

    assert(expectedJsHierarchy(hierarchyDescriptor) == json)
  }

  it should "be correctly deserialized from json" in {
    val hierarchyDescriptor: HierarchyDescriptor = HierarchyDescriptor(
      Map(
        "Sorted" -> TraitDescriptor("Sorted", List("Iterable", "Comparable")),
        "Trainable" -> TraitDescriptor("Trainable", List())
      ),
      Map(
        "DataFrame" -> ClassDescriptor("DataFrame", Some("Data"), List("Data")),
        "Whatever" -> ClassDescriptor("Whatever", None, List()))
    )
    val json = hierarchyDescriptor.toJson

    val fromJson = json.convertTo[HierarchyDescriptor]

    assert(hierarchyDescriptor == fromJson)
  }

  it should "be correctly deserialized from json when empty" in {
    val hierarchyDescriptor: HierarchyDescriptor = HierarchyDescriptor(Map(), Map())
    val json = hierarchyDescriptor.toJson

    val fromJson = json.convertTo[HierarchyDescriptor]

    assert(hierarchyDescriptor == fromJson)
  }

  private[this] def expectedJsHierarchy(hierarchy: HierarchyDescriptor): JsObject = {
    JsObject(Map[String, JsValue](
      "traits" -> JsObject(hierarchy.traits.values.map(t => t.name -> expectedJsTrait(t)).toMap),
      "classes" -> JsObject(hierarchy.classes.values.map(c => c.name -> expectedJsClass(c)).toMap)
    ))
  }

  private[this] def expectedJsTrait(traitDescriptor: TraitDescriptor): JsObject = {
    JsObject(Map(
      "name" -> JsString(traitDescriptor.name),
      "parents" -> JsArray(traitDescriptor.parents.map(JsString(_)).toVector)
    ))
  }

  private[this] def expectedJsClass(classDescriptor: ClassDescriptor): JsObject = {
    JsObject(Map[String, JsValue](
      "name" -> JsString(classDescriptor.name),
      "parent" -> classDescriptor.parent.map(JsString(_)).getOrElse(JsNull),
      "traits" -> JsArray(classDescriptor.traits.map(JsString(_)).toVector)
    ))
  }
}
