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

import scala.reflect.runtime.{universe => ru}

import org.scalatest.{FunSuite, Matchers}

import ai.deepsense.deeplang.DOperable
import ai.deepsense.deeplang.catalogs.doperable.exceptions._
import ai.deepsense.deeplang.doperables.DOperableMock

object SampleInheritance {
  trait T1 extends DOperable
  trait T2 extends T1
  trait T3 extends T1
  trait T extends DOperable
  abstract class A extends T3
  case class B() extends A with T
  case class C() extends A with T2
}

object Parametrized {
  trait T[T] extends DOperableMock
  abstract class A[T] extends DOperableMock
  class B extends A[Int]
}

object Constructors {

  class NotParameterLess(val i: Int) extends DOperableMock

  class AuxiliaryParameterless(val i: Int) extends DOperableMock {
    def this() = this(1)
  }

  class WithDefault(val i: Int = 1) extends DOperableMock
}

object TraitInheritance {
  class C1 extends DOperableMock
  trait T1 extends C1
  trait T2 extends T1
  class C2 extends T2

  trait S1 extends DOperable
  trait S2 extends DOperable

  class A1 extends DOperableMock
  trait S3 extends A1 with S1 with S2
}

object MixinInheritance {
  trait P extends DOperable

  trait TrA extends DOperable
  trait TrB extends DOperable
  class OpC extends DOperable

  class OpA extends TrA with P
  class OpB extends TrB with P
}

class DOperableCatalogSuite extends FunSuite with Matchers {

  def testGettingSubclasses[T <: DOperable : ru.TypeTag](
      h: DOperableCatalog, expected: DOperable*): Unit = {
    h.concreteSubclassesInstances[T] should contain theSameElementsAs expected
  }

  test("Getting concrete subclasses instances") {
    import SampleInheritance._

    val h = new DOperableCatalog
    h.registerDOperable[B]()
    h.registerDOperable[C]()

    val b = new B
    val c = new C

    def check[T <: DOperable : ru.TypeTag](expected: DOperable*): Unit = {
      testGettingSubclasses[T](h, expected: _*)
    }

    check[T with T1](b)
    check[T2 with T3](c)
    check[B](b)
    check[C](c)
    check[A with T2](c)
    check[A with T1](b, c)
    check[A](b, c)
    check[T3](b, c)
    check[T with T2]()
  }

  test("Getting concrete subclasses instances using ru.TypeTag") {
    import SampleInheritance._
    val h = new DOperableCatalog
    h.registerDOperable[B]()
    val t = ru.typeTag[T]
    h.concreteSubclassesInstances(t) should contain theSameElementsAs List(new B)
  }

  test("Listing DTraits and DClasses") {
    import SampleInheritance._
    val h = new DOperableCatalog
    h.registerDOperable[B]()
    h.registerDOperable[C]()

    def name[T: ru.TypeTag]: String = ru.typeOf[T].typeSymbol.fullName

    val traits = (TraitDescriptor(name[DOperable], Nil)::
      TraitDescriptor(name[T2], List(name[T1]))::
      TraitDescriptor(name[T], List(name[DOperable]))::
      TraitDescriptor(name[T1], List(name[DOperable]))::
      TraitDescriptor(name[T3], List(name[T1]))::
      Nil).map(t => t.name -> t).toMap

    val classes = (ClassDescriptor(name[A], None, List(name[T3]))::
      ClassDescriptor(name[B], Some(name[A]), List(name[T]))::
      ClassDescriptor(name[C], Some(name[A]), List(name[T2]))::
      Nil).map(c => c.name -> c).toMap

    val descriptor = h.descriptor
    descriptor.traits should contain theSameElementsAs traits
    descriptor.classes should contain theSameElementsAs classes
  }

  test("Registering class extending parametrized class") {
    import ai.deepsense.deeplang.catalogs.doperable.Parametrized._
    val p = new DOperableCatalog
    p.registerDOperable[B]()
  }

  test("Registering parametrized class") {
    import ai.deepsense.deeplang.catalogs.doperable.Parametrized._
    val p = new DOperableCatalog
    p.registerDOperable[A[Int]]()
  }

  test("Registering parametrized trait") {
    import ai.deepsense.deeplang.catalogs.doperable.Parametrized._
    val p = new DOperableCatalog
    p.registerDOperable[T[Int]]()
  }

  test("Registering concrete class with no parameter-less constructor should produce exception") {
    intercept[NoParameterlessConstructorInClassException] {
      import ai.deepsense.deeplang.catalogs.doperable.Constructors._
      val h = new DOperableCatalog
      h.registerDOperable[NotParameterLess]()
    }
  }

  test("Registering class with constructor with default parameters should produce exception") {
    intercept[NoParameterlessConstructorInClassException] {
      import ai.deepsense.deeplang.catalogs.doperable.Constructors._
      val h = new DOperableCatalog
      h.registerDOperable[WithDefault]()
    }
  }

  test("Registering class with auxiliary parameterless constructor should succeed") {
    import ai.deepsense.deeplang.catalogs.doperable.Constructors._
    val h = new DOperableCatalog
    h.registerDOperable[AuxiliaryParameterless]()
  }

  test("It is possible to register a trait that extends a class") {
    import ai.deepsense.deeplang.catalogs.doperable.MixinInheritance._
    val h = new DOperableCatalog
    h.registerDOperable[OpB]()
    h.registerDOperable[OpA]()
    val subclasses = h.concreteSubclassesInstances[P]
    assert(subclasses.size == 2)
    assert(subclasses.exists(x => x.isInstanceOf[OpA]))
    assert(subclasses.exists(x => x.isInstanceOf[OpB]))
  }
}
