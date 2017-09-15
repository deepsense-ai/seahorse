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

import com.typesafe.sbt.SbtLicenseReport.autoImportImpl._
import com.typesafe.sbt.license.{DepModuleInfo, LicenseInfo}

// scalastyle:off

// TODO Duplication with code in deepsense-backend
// TODO Try to find to automatically include those in all subprojects instead of explicit adding settings
// in root build.sbt in every subproject
object LicenceReportSettings {

  lazy val settings = Seq(
    licenseConfigurations := Set("compile", "provided"),
    licenseOverrides := {
      case DepModuleInfo(org, _, _) if hasPrefix(org, Seq(
        "org.apache",
        "com.fasterxml",
        "com.google.inject",
        "org.json4s",
        "org.apache.httpcomponents",
        "com.google.guava",
        "log4j",
        "org.99soft.guice",
        "org.eclipse.jetty.orbit", // + EPL. Can be treated as APACHE?
        "commons-beanutils",
        "commons-cli",
        "commons-io",
        "commons-net",
        "io.netty",
        "io.dropwizard.metrics",
        "com.sun.xml.fastinfoset",
        "commons-codec",
        "commons-collections",
        "commons-lang",
        "commons-logging",
        "org.codehaus.jettison",
        "org.objenesis",
        "org.tachyonproject",
        "org.htrace",
        "xerces",
        "xml-apis"
      )) => apache
      case DepModuleInfo("com.amazonaws", name, _) if name.startsWith("aws-java-sdk") => apache
      case DepModuleInfo(_, "jasper-compiler", _) |
           DepModuleInfo(_, "jasper-runtime", _)
        => apache
      case DepModuleInfo("oro", "oro", _) => apache
      case DepModuleInfo(org, _, _) if hasPrefix(org, Seq(
        "org.eclipse.jetty.orbit",
        "org.mortbay.jetty"
      )) => apacheAndEpl
      case DepModuleInfo("asm", "asm", _) => bsd
      case DepModuleInfo(org, _, _) if hasPrefix(org, Seq(
        "org.fusesource.leveldbjni",
         "org.codehaus.janino"
      )) => bsd
      case DepModuleInfo("com.github.fommil.netlib", _, _) =>
        LicenseInfo(LicenseCategory.BSD, "", "https://github.com/fommil/netlib-java/blob/master/LICENSE.txt")
      case DepModuleInfo(org, _, _) if hasPrefix(org, Seq("com.thoughtworks.paranamer")) => LicenseInfo(LicenseCategory.BSD, "BSD-Style", "http://www.opensource.org/licenses/bsd-license.php")
      case DepModuleInfo(org, _, _) if hasPrefix(org, Seq(
        "javax.ws.rs",
        "org.jvnet.mimepull",
        "org.glassfish",
        "com.sun.istack",
        "com.sun.jersey",
        "javax.xml.bind",
        "javax.servlet"
      )) =>
      LicenseInfo(LicenseCategory.GPLClasspath, "CDDL + GPLv2 with classpath exception", "https://glassfish.dev.java.net/nonav/public/CDDL+GPL.html")
      case DepModuleInfo(org, _, _) if hasPrefix(org, Seq("org.slf4j")) => LicenseInfo(LicenseCategory.MIT, "MIT License", "http://www.slf4j.org/license.html")
    }
  )

  private lazy val bsd = LicenseInfo(LicenseCategory.BSD, "BSD", "")

  private lazy val apacheAndEpl = apache

  private lazy val apache = LicenseInfo(LicenseCategory.Apache,
    "The Apache Software License, Version 2.0", "http://www.apache.org/licenses/LICENSE-2.0")

  private def hasPrefix(org: String, prefixes: Seq[String]): Boolean = prefixes.exists(x => org.startsWith(x))

}

// scalastyle:on
