/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package org.apache.spark.launcher

class PublicSparkSubmitOptionParser extends SparkSubmitOptionParser {

  def parseArgs(args: java.util.List[String]): Unit = super.parse(args)

}
