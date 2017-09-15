// This sbt file is used to replace SDK example's dependency on deeplang to use local one instead
// of the published. It allows to test example together with corresponding workflow executor's
// version without relying on incidental published snapshot. Example uses snapshot instead of
// concrete version, since we must be able to fix example after changes in workflow executor
// (and before releasing the next version).
// Name of the file is a bit fragile - this file must be loaded after build.sbt; "a" at the
// beginning of the name seems to force it.
libraryDependencies := {
  libraryDependencies.value.filterNot { x =>
    x.name == "seahorse-executor-deeplang"
  }
}

lazy val deeplang = ProjectRef(new File("../seahorse-workflow-executor"), "deeplang") % Provided

lazy val sdkRoot = project in new File(".") dependsOn deeplang