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

package ai.deepsense.commons.utils

import java.io.{File, FileNotFoundException, IOException}

import scala.util.Success

import org.scalatest.{Matchers, WordSpec}

class DirectoryListFileFinderSpec extends WordSpec with Matchers {

  val uutName: String = DirectoryListFileFinder.getClass.getSimpleName.filterNot(_ == '$')

  trait Setup {

    val fileA = new File("a")
    val fileB = new File("b")
    val fileC = new File("c")

    val theNeedleFileName = "theNeedle"
    val hayStackFileName = "hay.exe"

    val theNeedleFile = new File(theNeedleFileName)
    val hayStackFile = new File(hayStackFileName)

    val goodDirs = Set(fileA, fileB)
    val emptyDirs = Set(fileB)
    val badDirs = Set(fileC)

    val mockFileSystem: Map[File, Seq[File]] = Map(
      fileA -> Seq(theNeedleFile, hayStackFile),
      fileB -> Seq()
    )

    def createUUT(dirList: Traversable[File]): DirectoryListFileFinder = new DirectoryListFileFinder(dirList) {

      override def listFilesInDirectory(dir: File): Option[Seq[File]] = mockFileSystem.get(dir)

      override def filePredicate(f: File, desc: Option[String]): Boolean = f.getName == theNeedleFileName
    }

  }


  s"A $uutName" should {
    "find the files that fulfill the predicate" in {
      new Setup {
        val uut : DirectoryListFileFinder = createUUT(goodDirs)

        uut.findFile() shouldBe Success(theNeedleFile)
      }
    }

    "fail" when {
      "file is not found" in {
        new Setup {
          val uut : DirectoryListFileFinder = createUUT(emptyDirs)

          uut.findFile().failed.get shouldBe a [FileNotFoundException]
        }
      }

      "cannot enter a directory" in {
        new Setup {
          val uut : DirectoryListFileFinder = createUUT(badDirs)

          uut.findFile().failed.get shouldBe an [IOException]
        }
      }

      "given an empty list of directories" in {
        new Setup {
          val uut: DirectoryListFileFinder = createUUT(Seq())

          uut.findFile().failed.get shouldBe a [FileNotFoundException]
        }
      }
    }
  }
}
