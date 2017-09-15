/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.deeplang.doperables.spark.wrappers.estimators._
import io.deepsense.deeplang.doperables.spark.wrappers.evaluators._
import io.deepsense.deeplang.doperables.spark.wrappers.models._
import io.deepsense.deeplang.doperables.spark.wrappers.transformers._
import io.deepsense.deeplang.doperations._
import io.deepsense.deeplang.doperations.custom.{Sink, Source}
import io.deepsense.deeplang.doperations.spark.wrappers.estimators._
import io.deepsense.deeplang.doperations.spark.wrappers.evaluators._
import io.deepsense.deeplang.doperations.spark.wrappers.transformers._

/**
 * Object used to register all desired DOperables and DOperations.
 */
object CatalogRecorder {

  def registerDOperables(catalog: DOperableCatalog): Unit = {
    catalog.registerDOperable[DataFrame]()
    catalog.registerDOperable[Report]()
    catalog.registerDOperable[MetricValue]()
    catalog.registerDOperable[ColumnsFilterer]()
    catalog.registerDOperable[RowsFilterer]()
    catalog.registerDOperable[MissingValuesHandler]()
    catalog.registerDOperable[Projector]()
    catalog.registerDOperable[DatetimeComposer]()
    catalog.registerDOperable[DatetimeDecomposer]()
    catalog.registerDOperable[SqlTransformer]()
    catalog.registerDOperable[SqlColumnTransformer]()
    catalog.registerDOperable[PythonTransformer]()
    catalog.registerDOperable[PythonColumnTransformer]()
    catalog.registerDOperable[TypeConverter]()
    catalog.registerDOperable[CustomTransformer]()
    catalog.registerDOperable[GetFromVectorTransformer]()

    // wrapped Spark ML estimators & models
    catalog.registerDOperable[LogisticRegression]()
    catalog.registerDOperable[LogisticRegressionModel]()
    catalog.registerDOperable[NaiveBayes]()
    catalog.registerDOperable[NaiveBayesModel]()
    catalog.registerDOperable[AFTSurvivalRegression]()
    catalog.registerDOperable[AFTSurvivalRegressionModel]()
    catalog.registerDOperable[ALS]()
    catalog.registerDOperable[ALSModel]()
    catalog.registerDOperable[KMeans]()
    catalog.registerDOperable[KMeansModel]()
    catalog.registerDOperable[LDA]()
    catalog.registerDOperable[LDAModel]()
    catalog.registerDOperable[GBTRegression]()
    catalog.registerDOperable[GBTRegressionModel]()
    catalog.registerDOperable[IsotonicRegression]()
    catalog.registerDOperable[IsotonicRegressionModel]()
    catalog.registerDOperable[LinearRegression]()
    catalog.registerDOperable[LinearRegressionModel]()
    catalog.registerDOperable[RandomForestRegression]()
    catalog.registerDOperable[RandomForestRegressionModel]()
    catalog.registerDOperable[DecisionTreeRegression]()
    catalog.registerDOperable[DecisionTreeRegressionModel]()
    catalog.registerDOperable[PCAEstimator]()
    catalog.registerDOperable[PCAModel]()
    catalog.registerDOperable[StandardScalerEstimator]()
    catalog.registerDOperable[StandardScalerModel]()
    catalog.registerDOperable[MinMaxScalerEstimator]()
    catalog.registerDOperable[MinMaxScalerModel]()
    catalog.registerDOperable[VectorIndexerEstimator]()
    catalog.registerDOperable[VectorIndexerModel]()
    catalog.registerDOperable[StringIndexerEstimator]()
    catalog.registerDOperable[MultiColumnStringIndexerModel]()
    catalog.registerDOperable[SingleColumnStringIndexerModel]()
    catalog.registerDOperable[Word2VecEstimator]()
    catalog.registerDOperable[Word2VecModel]()
    catalog.registerDOperable[CountVectorizerEstimator]()
    catalog.registerDOperable[CountVectorizerModel]()
    catalog.registerDOperable[IDFEstimator]()
    catalog.registerDOperable[IDFModel]()
    catalog.registerDOperable[GBTClassifier]()
    catalog.registerDOperable[GBTClassificationModel]()
    catalog.registerDOperable[RandomForestClassifier]()
    catalog.registerDOperable[RandomForestClassificationModel]()
    catalog.registerDOperable[DecisionTreeClassifier]()
    catalog.registerDOperable[DecisionTreeClassificationModel]()
    catalog.registerDOperable[MultilayerPerceptronClassifier]()
    catalog.registerDOperable[MultilayerPerceptronClassifierModel]()
    catalog.registerDOperable[QuantileDiscretizerEstimator]()
    catalog.registerDOperable[QuantileDiscretizerModel]()
    catalog.registerDOperable[ChiSqSelectorEstimator]()
    catalog.registerDOperable[ChiSqSelectorModel]()

    // wrapped Spark transformers
    catalog.registerDOperable[Binarizer]()
    catalog.registerDOperable[DiscreteCosineTransformer]()
    catalog.registerDOperable[NGramTransformer]()
    catalog.registerDOperable[Normalizer]()
    catalog.registerDOperable[OneHotEncoder]()
    catalog.registerDOperable[PolynomialExpander]()
    catalog.registerDOperable[RegexTokenizer]()
    catalog.registerDOperable[StopWordsRemover]()
    catalog.registerDOperable[StringTokenizer]()
    catalog.registerDOperable[VectorAssembler]()
    catalog.registerDOperable[HashingTFTransformer]()
    catalog.registerDOperable[PythonEvaluator]()

    // wrapped Spark evaluators
    catalog.registerDOperable[BinaryClassificationEvaluator]()
    catalog.registerDOperable[MulticlassClassificationEvaluator]()
    catalog.registerDOperable[RegressionEvaluator]()
  }

  def registerDOperations(catalog: DOperationsCatalog): Unit = {

    catalog.registerDOperation[Notebook](
      DOperationCategories.IO)

    catalog.registerDOperation[ReadDataFrame](
      DOperationCategories.IO)

    catalog.registerDOperation[ReadTransformer](
      DOperationCategories.IO)

    catalog.registerDOperation[WriteDataFrame](
      DOperationCategories.IO)

    catalog.registerDOperation[WriteTransformer](
      DOperationCategories.IO)


    // Operations API uses catalog to fetch data even for operation/{id} calls.
    // To make Source and Sink accessible with this call they are added here.
    // As a workaround frontend filter outs those from operations pallete.
    // FIXME Make operation/{id} calls not rely on catalog and introduce some kind of
    // additional 'all operations' map to be used in operation/{id} calls.
    catalog.registerDOperation[Source](DOperationCategories.IO)
    catalog.registerDOperation[Sink](DOperationCategories.IO)

    catalog.registerDOperation[Evaluate](
      DOperationCategories.Action)

    catalog.registerDOperation[Fit](
      DOperationCategories.Action)

    catalog.registerDOperation[FitPlusTransform](
      DOperationCategories.Action)

    catalog.registerDOperation[Transform](
      DOperationCategories.Action)

    catalog.registerDOperation[Join](
      DOperationCategories.SetOperation)

    catalog.registerDOperation[Split](
      DOperationCategories.SetOperation)

    catalog.registerDOperation[Union](
      DOperationCategories.SetOperation)

    catalog.registerDOperation[FilterColumns](
      DOperationCategories.Filtering)

    catalog.registerDOperation[FilterRows](
      DOperationCategories.Filtering)

    catalog.registerDOperation[HandleMissingValues](
      DOperationCategories.Filtering)

    catalog.registerDOperation[Projection](
      DOperationCategories.Filtering)

    catalog.registerDOperation[CreateCustomTransformer](
      DOperationCategories.Transformation.Custom)

    catalog.registerDOperation[SqlColumnTransformation](
      DOperationCategories.Transformation.Custom.SQL)

    catalog.registerDOperation[SqlTransformation](
      DOperationCategories.Transformation.Custom.SQL)

    catalog.registerDOperation[PythonColumnTransformation](
      DOperationCategories.Transformation.Custom.Python)

    catalog.registerDOperation[PythonTransformation](
      DOperationCategories.Transformation.Custom.Python)

    catalog.registerDOperation[RTransformation](
      DOperationCategories.Transformation.Custom.R)

    catalog.registerDOperation[AssembleVector](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[Binarize](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[ComposeDatetime](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[ConvertType](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[DCT](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[DecomposeDatetime](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[GetFromVector](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[Normalize](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[OneHotEncode](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[PolynomialExpand](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[QuantileDiscretizer](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[StringIndexer](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[VectorIndexer](
      DOperationCategories.Transformation.FeatureConversion)

    catalog.registerDOperation[MinMaxScaler](
      DOperationCategories.Transformation.FeatureScaling)

    catalog.registerDOperation[StandardScaler](
      DOperationCategories.Transformation.FeatureScaling)

    catalog.registerDOperation[ConvertToNGrams](
      DOperationCategories.Transformation.TextProcessing)

    catalog.registerDOperation[CountVectorizer](
      DOperationCategories.Transformation.TextProcessing)

    catalog.registerDOperation[HashingTF](
      DOperationCategories.Transformation.TextProcessing)

    catalog.registerDOperation[IDF](
      DOperationCategories.Transformation.TextProcessing)

    catalog.registerDOperation[RemoveStopWords](
      DOperationCategories.Transformation.TextProcessing)

    catalog.registerDOperation[Tokenize](
      DOperationCategories.Transformation.TextProcessing)

    catalog.registerDOperation[TokenizeWithRegex](
      DOperationCategories.Transformation.TextProcessing)

    catalog.registerDOperation[Word2Vec](
      DOperationCategories.Transformation.TextProcessing)

    catalog.registerDOperation[GridSearch](
      DOperationCategories.ML.HyperOptimization)

    catalog.registerDOperation[CreateAFTSurvivalRegression](
      DOperationCategories.ML.Regression)

    catalog.registerDOperation[CreateDecisionTreeRegression](
      DOperationCategories.ML.Regression)

    catalog.registerDOperation[CreateGBTRegression](
      DOperationCategories.ML.Regression)

    catalog.registerDOperation[CreateIsotonicRegression](
      DOperationCategories.ML.Regression)

    catalog.registerDOperation[CreateLinearRegression](
      DOperationCategories.ML.Regression)

    catalog.registerDOperation[CreateRandomForestRegression](
      DOperationCategories.ML.Regression)

    catalog.registerDOperation[CreateDecisionTreeClassifier](
      DOperationCategories.ML.Classification)

    catalog.registerDOperation[CreateGBTClassifier](
      DOperationCategories.ML.Classification)

    catalog.registerDOperation[CreateLogisticRegression](
      DOperationCategories.ML.Classification)

    catalog.registerDOperation[CreateMultilayerPerceptronClassifier](
      DOperationCategories.ML.Classification)

    catalog.registerDOperation[CreateNaiveBayes](
      DOperationCategories.ML.Classification)

    catalog.registerDOperation[CreateRandomForestClassifier](
      DOperationCategories.ML.Classification)

    catalog.registerDOperation[CreateKMeans](
      DOperationCategories.ML.Clustering)

    catalog.registerDOperation[CreateLDA](
      DOperationCategories.ML.Clustering)

    catalog.registerDOperation[ChiSqSelector](
      DOperationCategories.ML.FeatureSelection)

    catalog.registerDOperation[CreateALS](
      DOperationCategories.ML.Recommendation)

    catalog.registerDOperation[PCA](
      DOperationCategories.ML.DimensionalityReduction)

    catalog.registerDOperation[CreatePythonEvaluator](
      DOperationCategories.ML.ModelEvaluation)

    // operations generated from Spark evaluators
    catalog.registerDOperation[CreateBinaryClassificationEvaluator](
      DOperationCategories.ML.ModelEvaluation)

    catalog.registerDOperation[CreateMulticlassClassificationEvaluator](
      DOperationCategories.ML.ModelEvaluation)

    catalog.registerDOperation[CreateRegressionEvaluator](
      DOperationCategories.ML.ModelEvaluation)
  }
}
