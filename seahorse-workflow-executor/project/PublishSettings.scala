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

import com.typesafe.sbt.pgp.PgpKeys
import sbt.Keys._
import sbt._

// scalastyle:off

object PublishSettings {
  def enablePublishing = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := (
    <url>https://github.com/deepsense-ai/seahorse</url>
      <licenses>
        <license>
          <name>Apache 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:deepsense.ai/seahorse.git</url>
        <connection>scm:git:git@github.com:deepsense.ai/seahorse.git</connection>
      </scm>
      <developers>
        <developer>
          <name>deepsense.ai</name>
          <email>contact@deepsense.ai</email>
          <url>https://deepsense.ai/</url>
          <organization>deepsense.ai</organization>
          <organizationUrl>https://deepsense.ai/</organizationUrl>
        </developer>
      </developers>))

  def disablePublishing = Seq(
    PgpKeys.publishSigned := ()
  )
}
