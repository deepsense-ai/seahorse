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

package ai.deepsense.deeplang.doperations.layout

import ai.deepsense.deeplang.DPortPosition.DPortPosition
import ai.deepsense.deeplang._

sealed trait SmallBlockLayout {
  protected val symmetricPortLayout =
    Vector(DPortPosition.Left, DPortPosition.Right)
}

trait SmallBlockLayout2To0 extends SmallBlockLayout {
  self: DOperation2To0[_, _] =>
  override def inPortsLayout: Vector[DPortPosition] = symmetricPortLayout
}

trait SmallBlockLayout2To1 extends SmallBlockLayout {
  self: DOperation2To1[_, _, _] =>
  override def inPortsLayout: Vector[DPortPosition] = symmetricPortLayout
}

trait SmallBlockLayout2To2 extends SmallBlockLayout {
  self: DOperation2To2[_, _, _, _] =>
  override def inPortsLayout: Vector[DPortPosition] = symmetricPortLayout
  override def outPortsLayout: Vector[DPortPosition] = symmetricPortLayout
}

trait SmallBlockLayout2To3 extends SmallBlockLayout {
  self: DOperation2To3[_, _, _, _, _] =>
  override def inPortsLayout: Vector[DPortPosition] = symmetricPortLayout
}

trait SmallBlockLayout0To2 extends SmallBlockLayout {
  self: DOperation0To2[_, _] =>
  override def outPortsLayout: Vector[DPortPosition] = symmetricPortLayout
}

trait SmallBlockLayout1To2 extends SmallBlockLayout {
  self: DOperation1To2[_, _, _] =>
  override def outPortsLayout: Vector[DPortPosition] = symmetricPortLayout
}

trait SmallBlockLayout3To2 extends SmallBlockLayout {
  self: DOperation3To2[_, _, _, _, _] =>
  override def outPortsLayout: Vector[DPortPosition] = symmetricPortLayout
}
