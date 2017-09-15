/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperables.dataframe

import io.deepsense.deeplang.UnitSpec
import org.apache.spark.sql.types._

class HasSchemaValidationSpec extends UnitSpec {

  class Validator extends HasSchemaValidation

  "SchemaValidation" should "fail if column names are not unique in schema" in {
    val schema: StructType = new StructType(
      Array(new StructField("car", StringType), new StructField("car", IntegerType)))

    intercept[IllegalArgumentException] {
      new Validator().validateSchema(schema)
    }
  }

  it should "fail if there are multiple columns with id role" in {
    val schema: StructType = new StructType(Array(
      new StructField("pesel", StringType, metadata = createIdRoleMetadata),
      new StructField("id", IntegerType, metadata = createIdRoleMetadata)
    ))

    intercept[IllegalArgumentException] {
      new Validator().validateSchema(schema)
    }
  }

  it should "fail if id column has invalid type" in {
    val schema: StructType = new StructType(Array(
      new StructField("pesel", BooleanType, metadata = createIdRoleMetadata)
    ))

    intercept[IllegalArgumentException] {
      new Validator().validateSchema(schema)
    }
  }

  it should "validate correct schema" in {
    val schema: StructType = new StructType(Array(
      new StructField("pesel", IntegerType, metadata = createIdRoleMetadata),
      new StructField("name", StringType)
    ))

    new Validator().validateSchema(schema)
  }

  private[this] def createIdRoleMetadata = {
    val metadataBuilder = new MetadataBuilder
    metadataBuilder.putString("role", ColumnRole.Id.name)
    metadataBuilder.build()
  }

}
