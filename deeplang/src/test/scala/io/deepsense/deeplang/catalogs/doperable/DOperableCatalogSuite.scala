/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperable

import scala.reflect.runtime.{universe => ru}

import org.scalatest.{FunSuite, Matchers}

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.catalogs.doperable.exceptions._

object H {
  trait T1 extends DOperable
  trait T2 extends T1
  trait T3 extends T1
  trait T extends DOperable
  abstract class A extends T3
  class B extends A with T {
    override def equals(any: Any) = any.isInstanceOf[B]
  }
  class C extends A with T2 {
    override def equals(any: Any) = any.isInstanceOf[C]
  }
}

object Parametrized {
  trait T[T] extends DOperable
  abstract class A[T] extends DOperable
  class B extends A[Int]
}

object Constructors {
  class NotParameterLess(val i: Int) extends DOperable
  class AuxiliaryParameterless(val i: Int) extends DOperable {
    def this() = this(1)
  }
  class WithDefault(val i: Int = 1) extends DOperable
}

object TraitInheritance {
  class C1 extends DOperable
  trait T1 extends C1
  trait T2 extends T1
  class C2 extends T2

  trait S1 extends DOperable
  trait S2 extends DOperable
  class A1 extends DOperable
  trait S3 extends A1 with S1 with S2
}

class DOperableCatalogSuite extends FunSuite with Matchers {

  def testGettingSubclasses[T <: DOperable : ru.TypeTag](
      h: DOperableCatalog, expected: DOperable*): Unit = {
    h.concreteSubclassesInstances[T] should contain theSameElementsAs expected
  }

  test("Getting concrete subclasses instances") {

    val h = new DOperableCatalog
    h.registerDOperable[H.B]()
    h.registerDOperable[H.C]()

    val b = new H.B
    val c = new H.C

    def check[T <: DOperable : ru.TypeTag](expected: DOperable*) = {
      testGettingSubclasses[T](h, expected:_*)
    }

    check[H.T with H.T1](b)
    check[H.T2 with H.T3](c)
    check[H.B](b)
    check[H.C](c)
    check[H.A with H.T2](c)
    check[H.A with H.T1](b, c)
    check[H.A](b, c)
    check[H.T3](b, c)
    check[H.T with H.T2]()
  }

  test("Getting concrete subclasses instances using ru.TypeTag") {
    val h = new DOperableCatalog
    h.registerDOperable[H.B]()
    val t = ru.typeTag[H.T]
    h.concreteSubclassesInstances(t) should contain theSameElementsAs List(new H.B)
  }

  test("Listing DTraits and DClasses") {
    val h = new DOperableCatalog
    h.registerDOperable[H.B]()
    h.registerDOperable[H.C]()

    val traits = (TraitDescriptor("DOperable", Nil)::
      TraitDescriptor("T2", List("T1"))::
      TraitDescriptor("T", List("DOperable"))::
      TraitDescriptor("T1", List("DOperable"))::
      TraitDescriptor("T3", List("T1"))::
      Nil).map(t => t.name -> t).toMap

    val classes = (ClassDescriptor("A", None, List("T3"))::
      ClassDescriptor("B", Some("A"), List("T"))::
      ClassDescriptor("C", Some("A"), List("T2"))::
      Nil).map(c => c.name -> c).toMap

    val descriptor = h.descriptor
    descriptor.traits should contain theSameElementsAs traits
    descriptor.classes should contain theSameElementsAs classes
  }

  test("Registering class extending parametrized class should produce exception") {
    intercept[ParametrizedTypeException] {
      import io.deepsense.deeplang.catalogs.doperable.Parametrized._
      val p = new DOperableCatalog
      p.registerDOperable[B]()
    }
  }

  test("Registering parametrized class should produce exception") {
    intercept[ParametrizedTypeException] {
      import io.deepsense.deeplang.catalogs.doperable.Parametrized._
      val p = new DOperableCatalog
      p.registerDOperable[A[Int]]()
    }
  }

  test("Registering parametrized trait should produce exception") {
    intercept[ParametrizedTypeException] {
      import io.deepsense.deeplang.catalogs.doperable.Parametrized._
      val p = new DOperableCatalog
      p.registerDOperable[T[Int]]()
    }
  }

  test("Registering concrete class with no parameter-less constructor should produce exception") {
    intercept[NoParameterlessConstructorInClassException] {
      import io.deepsense.deeplang.catalogs.doperable.Constructors._
      val h = new DOperableCatalog
      h.registerDOperable[NotParameterLess]()
    }
  }

  test("Registering class with constructor with default parameters should produce exception") {
    intercept[NoParameterlessConstructorInClassException] {
      import io.deepsense.deeplang.catalogs.doperable.Constructors._
      val h = new DOperableCatalog
      h.registerDOperable[WithDefault]()
    }
  }

  test("Registering class with auxiliary parameterless constructor should succeed") {
    import io.deepsense.deeplang.catalogs.doperable.Constructors._
    val h = new DOperableCatalog
    h.registerDOperable[AuxiliaryParameterless]()
  }

  test("Registering hierarchy with trait extending class should produce exception") {
    intercept[TraitInheritingFromClassException] {
      import io.deepsense.deeplang.catalogs.doperable.TraitInheritance._
      val h = new DOperableCatalog
      h.registerDOperable[C2]()
    }
  }

  test("Registering trait extending class should produce exception") {
    intercept[TraitInheritingFromClassException] {
      import io.deepsense.deeplang.catalogs.doperable.TraitInheritance._
      val h = new DOperableCatalog
      h.registerDOperable[S3]()
    }
  }
}
