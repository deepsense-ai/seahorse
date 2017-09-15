package io.deepsense.deeplang.doperables.dataframe

object DataFrameUtils {

  /**
   * Method useful for generating new name for a column. When we want to add a new column
   * to a dataframe we need to generate a new name for it assuring that this name is not already
   * used in the dataframe. Common use case is when we generate a new column from an existing one
   * by adding some sort of extension. When new name generated using base column name and extension
   * is already used we want to add level number. Ex. timestamp_hour_1
   * @param existingColumnsNames set of column names in the dataframe.
   * @param originalColumnName name of the column which is a base for a new column.
   * @param newColumnNameExtensions possible extension for a base column used for
   *                                generating a new column name
   * @param newColumnNameGenerator function that takes base column name, extension and level and
   *                               return new name for a column
   * @return first free level that can be used to generate a new name for a column
   */
  def getFirstFreeNamesLevel(
      existingColumnsNames: Set[String],
      originalColumnName: String,
      newColumnNameExtensions: Set[String],
      newColumnNameGenerator: ((String, String, Int) => String)): Int = {
    def getFirstFreeNamesLevel(level: Int): Int = {
      val levelTaken: Boolean = newColumnNameExtensions.exists(
        ext => existingColumnsNames.contains(newColumnNameGenerator(originalColumnName, ext, level))
      )
      if (levelTaken) {
        getFirstFreeNamesLevel(level + 1)
      } else {
        level
      }
    }

    getFirstFreeNamesLevel(0)
  }

}
