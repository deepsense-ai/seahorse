/**
 * Copyright 2015, deepsense.ai
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

package ai.deepsense.deeplang

import java.io.File
import java.net.URL

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext

import ai.deepsense.commons.utils.LoggerForCallerClass
import ai.deepsense.deeplang.catalogs.CatalogPair
import ai.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import ai.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import ai.deepsense.deeplang.doperables._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.deeplang.doperables.spark.wrappers.estimators._
import ai.deepsense.deeplang.doperables.spark.wrappers.evaluators._
import ai.deepsense.deeplang.doperables.spark.wrappers.models._
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers._
import ai.deepsense.deeplang.doperations._
import ai.deepsense.deeplang.doperations.custom.{Sink, Source}
import ai.deepsense.deeplang.doperations.spark.wrappers.estimators._
import ai.deepsense.deeplang.doperations.spark.wrappers.evaluators._
import ai.deepsense.deeplang.doperations.spark.wrappers.transformers._
import ai.deepsense.deeplang.refl.CatalogScanner

/**
 * Class used to register all desired DOperables and DOperations.
 */
class CatalogRecorder private (jars: Seq[URL]) {
  def withDir(jarsDir: File): CatalogRecorder = {
    val additionalJars =
      if (jarsDir.exists && jarsDir.isDirectory) {
        jarsDir.listFiles().toSeq.filter(f => f.isFile && f.getName.endsWith(".jar"))
      } else {
        Seq.empty
      }
    withJars(additionalJars)
  }

  def withJars(additionalJars: Seq[File]): CatalogRecorder = {
    new CatalogRecorder(jars ++ additionalJars.map(_.toURI.toURL))
  }

  def withSparkContext(sparkContext: SparkContext): CatalogRecorder = {
    new CatalogRecorder(jars ++ sparkContext.jars.map(new URL(_)))
  }

  private def registerDOperables(catalog: DOperableCatalog): Unit = {
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
    catalog.registerDOperable[RTransformer]()
    catalog.registerDOperable[RColumnTransformer]()
    catalog.registerDOperable[TypeConverter]()
    catalog.registerDOperable[CustomTransformer]()
    catalog.registerDOperable[GetFromVectorTransformer]()
    catalog.registerDOperable[SortTransformer]()

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
    catalog.registerDOperable[REvaluator]()

    // wrapped Spark evaluators
    catalog.registerDOperable[BinaryClassificationEvaluator]()
    catalog.registerDOperable[MulticlassClassificationEvaluator]()
    catalog.registerDOperable[RegressionEvaluator]()
  }

  private def registerDOperations(catalog: DOperationsCatalog): Unit = {

    catalog.registerDOperation(DOperationCategories.Other, () => new UnknownOperation, visible = false)
    catalog.registerDOperation(DOperationCategories.IO, () => new PythonNotebook())
    catalog.registerDOperation(DOperationCategories.IO, () => new RNotebook())
    catalog.registerDOperation(DOperationCategories.IO, () => new ReadDatasource())
    catalog.registerDOperation(DOperationCategories.IO, () => new ReadTransformer())
    catalog.registerDOperation(DOperationCategories.IO, () => new WriteDatasource())
    catalog.registerDOperation(DOperationCategories.IO, () => new WriteTransformer())

    // Operations API uses catalog to fetch data even for operation/{id} calls.
    // To make Source and Sink accessible with this call they are added here.
    // As a workaround frontend filters out those from operations pallete.
    // FIXME Make operation/{id} calls not rely on catalog and introduce some kind of
    // additional 'all operations' map to be used in operation/{id} calls.
    catalog.registerDOperation(DOperationCategories.IO, () => Source())
    catalog.registerDOperation(DOperationCategories.IO, () => Sink())

    catalog.registerDOperation(DOperationCategories.Action, () => new Evaluate())
    catalog.registerDOperation(DOperationCategories.Action, () => new Fit())
    catalog.registerDOperation(DOperationCategories.Action, () => new FitPlusTransform())
    catalog.registerDOperation(DOperationCategories.Action, () => new Transform())
    catalog.registerDOperation(DOperationCategories.SetOperation, () => new Join())
    catalog.registerDOperation(DOperationCategories.SetOperation, () => new Split())
    catalog.registerDOperation(DOperationCategories.SetOperation, () => new Union())
    catalog.registerDOperation(DOperationCategories.SetOperation, () => new SqlCombine())
    catalog.registerDOperation(DOperationCategories.SetOperation, () => new SortTransformation())
    catalog.registerDOperation(DOperationCategories.Filtering, () => new FilterColumns())
    catalog.registerDOperation(DOperationCategories.Filtering, () => new FilterRows())
    catalog.registerDOperation(DOperationCategories.Filtering, () => new HandleMissingValues())
    catalog.registerDOperation(DOperationCategories.Filtering, () => new Projection())

    catalog.registerDOperation(DOperationCategories.Transformation.Custom, () => new CreateCustomTransformer())
    catalog.registerDOperation(DOperationCategories.Transformation.Custom, () => new SqlColumnTransformation())
    catalog.registerDOperation(DOperationCategories.Transformation.Custom, () => new SqlTransformation())
    catalog.registerDOperation(DOperationCategories.Transformation.Custom, () => new PythonColumnTransformation())
    catalog.registerDOperation(DOperationCategories.Transformation.Custom, () => new PythonTransformation())
    catalog.registerDOperation(DOperationCategories.Transformation.Custom, () => new RColumnTransformation())
    catalog.registerDOperation(DOperationCategories.Transformation.Custom, () => new RTransformation())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new AssembleVector())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new Binarize())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new ComposeDatetime())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new ConvertType())

    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new DCT())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new DecomposeDatetime())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new GetFromVector())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new Normalize())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new OneHotEncode())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new PolynomialExpand())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new QuantileDiscretizer())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new StringIndexer())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureConversion, () => new VectorIndexer())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureScaling, () => new MinMaxScaler())
    catalog.registerDOperation(DOperationCategories.Transformation.FeatureScaling, () => new StandardScaler())
    catalog.registerDOperation(DOperationCategories.Transformation.TextProcessing, () => new ConvertToNGrams())
    catalog.registerDOperation(DOperationCategories.Transformation.TextProcessing, () => new CountVectorizer())
    catalog.registerDOperation(DOperationCategories.Transformation.TextProcessing, () => new HashingTF())
    catalog.registerDOperation(DOperationCategories.Transformation.TextProcessing, () => new IDF())
    catalog.registerDOperation(DOperationCategories.Transformation.TextProcessing, () => new RemoveStopWords())
    catalog.registerDOperation(DOperationCategories.Transformation.TextProcessing, () => new Tokenize())
    catalog.registerDOperation(DOperationCategories.Transformation.TextProcessing, () => new TokenizeWithRegex())

    catalog.registerDOperation(DOperationCategories.Transformation.TextProcessing, () => new Word2Vec())
    catalog.registerDOperation(DOperationCategories.ML.HyperOptimization, () => new GridSearch())
    catalog.registerDOperation(DOperationCategories.ML.Regression, () => new CreateAFTSurvivalRegression())
    catalog.registerDOperation(DOperationCategories.ML.Regression, () => new CreateDecisionTreeRegression())
    catalog.registerDOperation(DOperationCategories.ML.Regression, () => new CreateGBTRegression())
    catalog.registerDOperation(DOperationCategories.ML.Regression, () => new CreateIsotonicRegression())
    catalog.registerDOperation(DOperationCategories.ML.Regression, () => new CreateLinearRegression())
    catalog.registerDOperation(DOperationCategories.ML.Regression, () => new CreateRandomForestRegression())
    catalog.registerDOperation(DOperationCategories.ML.Classification, () => new CreateDecisionTreeClassifier())
    catalog.registerDOperation(DOperationCategories.ML.Classification, () => new CreateGBTClassifier())
    catalog.registerDOperation(DOperationCategories.ML.Classification, () => new CreateLogisticRegression())
    catalog.registerDOperation(DOperationCategories.ML.Classification, () => new CreateMultilayerPerceptronClassifier())
    catalog.registerDOperation(DOperationCategories.ML.Classification, () => new CreateNaiveBayes())
    catalog.registerDOperation(DOperationCategories.ML.Classification, () => new CreateRandomForestClassifier())
    catalog.registerDOperation(DOperationCategories.ML.Clustering, () => new CreateKMeans())
    catalog.registerDOperation(DOperationCategories.ML.Clustering, () => new CreateLDA())

    catalog.registerDOperation(DOperationCategories.ML.FeatureSelection, () => new ChiSqSelector())
    catalog.registerDOperation(DOperationCategories.ML.Recommendation, () => new CreateALS())
    catalog.registerDOperation(DOperationCategories.ML.DimensionalityReduction, () => new PCA())
    catalog.registerDOperation(DOperationCategories.ML.ModelEvaluation, () => new CreatePythonEvaluator())
    catalog.registerDOperation(DOperationCategories.ML.ModelEvaluation, () => new CreateREvaluator())
    // operations generated from Spark evaluators
    catalog.registerDOperation(DOperationCategories.ML.ModelEvaluation, () => new CreateBinaryClassificationEvaluator())
    catalog.registerDOperation(DOperationCategories.ML.ModelEvaluation,
      () => new CreateMulticlassClassificationEvaluator())
    catalog.registerDOperation(DOperationCategories.ML.ModelEvaluation, () => new CreateRegressionEvaluator())

  }

  private def createDOperableCatalog(): DOperableCatalog = {
    val catalog = new DOperableCatalog
    registerDOperables(catalog)
    catalog
  }

  private def createDOperationsCatalog(): DOperationsCatalog = {
    val catalog = DOperationsCatalog()
    registerDOperations(catalog)
    catalog
  }

  lazy val catalogs: CatalogPair = {
    val dOperableCatalog = createDOperableCatalog()
    val dOperationsCatalog = createDOperationsCatalog()

    new CatalogScanner(jars).scanAndRegister(dOperableCatalog, dOperationsCatalog)

    CatalogPair(dOperableCatalog, dOperationsCatalog)
  }

}

object CatalogRecorder {
  val logger = LoggerForCallerClass()

  def fromDir(dir: File): CatalogRecorder = {
    new CatalogRecorder(Seq.empty).withDir(dir)
  }
  def fromJars(jars: Seq[URL]): CatalogRecorder = {
    new CatalogRecorder(jars)
  }
  def fromSparkContext(sparkContext: SparkContext): CatalogRecorder = {
    new CatalogRecorder(Seq.empty).withSparkContext(sparkContext)
  }

  lazy val resourcesCatalogRecorder: CatalogRecorder = {
    fromDir(Config.jarsDir)
  }
}
