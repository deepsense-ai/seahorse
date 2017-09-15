/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.parameters

import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}

import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, FunSuite}

class ParametersSerializationSuite extends FunSuite with Matchers with MockitoSugar {

  test("ParametersSchema and its content should be serializable") {
    val param = BooleanParameter("", None, required = false)
    val schema = ParametersSchema("x" -> param)
    param.value = Some(false)
    val result = serializeAndDeserialize(schema)
    result shouldBe schema
    result.getBoolean("x") shouldBe param.value
  }

  test("Parameter and it's value should be serializable") {
    val param = BooleanParameter("", None, required = false)
    param.value = Some(true)
    testParameterSerialization(param)
  }

  test("NumericParameter, it's validator and it's value should be serializable") {
    val rangeValidator = RangeValidator(3, 4)
    val param = NumericParameter("", None, required = false, validator = rangeValidator)
    param.value = Some(1420)
    testParameterSerialization(param)
  }

  test("StringParameter, it's validator and it's value should be serializable") {
    val regexValidator = RegexValidator("xxx".r)
    val param = StringParameter("", None, required = false, validator = regexValidator)
    param.value = Some("xyz")
    val result = serializeAndDeserialize(param)

    // Here we perform checking of RegexValidator contents equality manually,
    // because unfortunately "a".r != "a".r
    val resultValidator = result.validator.asInstanceOf[RegexValidator]
    resultValidator.regex.toString() shouldBe regexValidator.regex.toString()

    // We replace validator of result with original validator,
    // so that we can perform normal equality check on rest of the fields
    val resultWithReplacedValidator = result.copy(validator = regexValidator)
    resultWithReplacedValidator shouldBe param

    result.value shouldBe param.value
  }

  test("ChoiceParameter and it's value should be serializable") {
    val param = ChoiceParameter("", None, required = false, options = Map.empty)
    param.value = Some("some selection")
    testParameterSerialization(param)
  }

  test("MultipleChoiceParameter and it's value should be serializable") {
    val param = MultipleChoiceParameter("", None, required = false, options = Map.empty)
    param.value = Some(Seq("first selection", "second selection"))
    testParameterSerialization(param)
  }

  test("ParametersSequence and it's value should be serializable") {
    val param = ParametersSequence("", required = false, predefinedSchema = ParametersSchema())
    param.value = Some(Vector(ParametersSchema(
      "x" -> BooleanParameter("", None, required = false))))
    testParameterSerialization(param)
  }

  test("SingleColumnSelectorParameter and it's value should be serializable") {
    val param = SingleColumnSelectorParameter("", required = false)
    param.value = Some(IndexSingleColumnSelection(4))
    testParameterSerialization(param)
  }

  test("ColumnSelectorParameter and it's value should be serializable") {
    val param = ColumnSelectorParameter("", required = false)
    param.value = Some(MultipleColumnSelection(Vector(NameColumnSelection(List("xyz")))))
    testParameterSerialization(param)
  }

  private[this] def serializeAndDeserialize[T](objectToSerialize: T): T = {
    val bytesOut = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(bytesOut)
    oos.writeObject(objectToSerialize)
    oos.flush()
    oos.close()

    val bufferIn = new ByteArrayInputStream(bytesOut.toByteArray)
    val streamIn = new ObjectInputStream(bufferIn)
    streamIn.readObject().asInstanceOf[T]
  }

  private[this] def testParameterSerialization(param: Parameter): Unit = {
    val result = serializeAndDeserialize(param)
    result shouldBe param
    result.value shouldBe param.value
  }
}
