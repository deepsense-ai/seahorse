/**
 * Copyright 2018 deepsense.ai (CodiLime, Inc) & Astraea, Inc.
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

package ai.deepsense.deeplang.catalogs

import ai.deepsense.deeplang.DOperationCategories._
import ai.deepsense.deeplang.catalogs.spi.{CatalogRegistrant, CatalogRegistrar}
import ai.deepsense.deeplang.doperations._
import ai.deepsense.deeplang.doperations.custom.{Sink, Source}
import ai.deepsense.deeplang.doperations.spark.wrappers.estimators._
import ai.deepsense.deeplang.doperations.spark.wrappers.evaluators._
import ai.deepsense.deeplang.doperations.spark.wrappers.transformers._

/**
  * Class responsible for registering the default built-in set of Seahorse operations.
  */
class StandardOperations extends CatalogRegistrant {
  override def register(registrar: CatalogRegistrar): Unit = {

    val p = SortPriority.DEFAULT.inSequence(100)
    registrar.registerOperation(IO, () => new PythonNotebook(), p.next())
    registrar.registerOperation(IO, () => new RNotebook(), p.next())
    registrar.registerOperation(IO, () => new ReadDatasource(), p.next())
    registrar.registerOperation(IO, () => new ReadTransformer(), p.next())
    registrar.registerOperation(IO, () => new WriteDatasource(), p.next())
    registrar.registerOperation(IO, () => new WriteTransformer(), p.next())

    // Operations API uses catalog to fetch data even for operation/{id} calls
    // To make Source and Sink accessible with this call they are added here
    // As a workaround frontend filters out those from operations pallete
    // FIXME Make operation/{id} calls not rely on catalog and introduce some kind of
    // additional 'all operations' map to be used in operation/{id} calls
    registrar.registerOperation(IO, () => Source(), p.next())
    registrar.registerOperation(IO, () => Sink(), p.next())

    registrar.registerOperation(Action, () => new Evaluate(), p.next())
    registrar.registerOperation(Action, () => new Fit(), p.next())
    registrar.registerOperation(Action, () => new FitPlusTransform(), p.next())
    registrar.registerOperation(Action, () => new Transform(), p.next())
    registrar.registerOperation(SetOperation, () => new Join(), p.next())
    registrar.registerOperation(SetOperation, () => new Split(), p.next())
    registrar.registerOperation(SetOperation, () => new Union(), p.next())
    registrar.registerOperation(SetOperation, () => new SqlCombine(), p.next())
    registrar.registerOperation(SetOperation, () => new SortTransformation(), p.next())
    registrar.registerOperation(Filtering, () => new FilterColumns(), p.next())
    registrar.registerOperation(Filtering, () => new FilterRows(), p.next())
    registrar.registerOperation(Filtering, () => new HandleMissingValues(), p.next())
    registrar.registerOperation(Filtering, () => new Projection(), p.next())

    registrar.registerOperation(Transformation.Custom, () => new CreateCustomTransformer(), p.next())
    registrar.registerOperation(Transformation.Custom, () => new SqlColumnTransformation(), p.next())
    registrar.registerOperation(Transformation.Custom, () => new SqlTransformation(), p.next())
    registrar.registerOperation(Transformation.Custom, () => new PythonColumnTransformation(), p.next())
    registrar.registerOperation(Transformation.Custom, () => new PythonTransformation(), p.next())
    registrar.registerOperation(Transformation.Custom, () => new RColumnTransformation(), p.next())
    registrar.registerOperation(Transformation.Custom, () => new RTransformation(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new AssembleVector(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new Binarize(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new ComposeDatetime(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new ConvertType(), p.next())

    registrar.registerOperation(Transformation.FeatureConversion, () => new DCT(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new DecomposeDatetime(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new GetFromVector(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new Normalize(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new OneHotEncode(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new PolynomialExpand(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new QuantileDiscretizer(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new StringIndexer(), p.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new VectorIndexer(), p.next())
    registrar.registerOperation(Transformation.FeatureScaling, () => new MinMaxScaler(), p.next())
    registrar.registerOperation(Transformation.FeatureScaling, () => new StandardScaler(), p.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new ConvertToNGrams(), p.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new CountVectorizer(), p.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new HashingTF(), p.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new IDF(), p.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new RemoveStopWords(), p.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new Tokenize(), p.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new TokenizeWithRegex(), p.next())

    registrar.registerOperation(Transformation.TextProcessing, () => new Word2Vec(), p.next())
    registrar.registerOperation(ML.HyperOptimization, () => new GridSearch(), p.next())
    registrar.registerOperation(ML.Regression, () => new CreateAFTSurvivalRegression(), p.next())
    registrar.registerOperation(ML.Regression, () => new CreateDecisionTreeRegression(), p.next())
    registrar.registerOperation(ML.Regression, () => new CreateGBTRegression(), p.next())
    registrar.registerOperation(ML.Regression, () => new CreateIsotonicRegression(), p.next())
    registrar.registerOperation(ML.Regression, () => new CreateLinearRegression(), p.next())
    registrar.registerOperation(ML.Regression, () => new CreateRandomForestRegression(), p.next())
    registrar.registerOperation(ML.Classification, () => new CreateDecisionTreeClassifier(), p.next())
    registrar.registerOperation(ML.Classification, () => new CreateGBTClassifier(), p.next())
    registrar.registerOperation(ML.Classification, () => new CreateLogisticRegression(), p.next())
    registrar.registerOperation(ML.Classification, () => new CreateMultilayerPerceptronClassifier(), p.next())
    registrar.registerOperation(ML.Classification, () => new CreateNaiveBayes(), p.next())
    registrar.registerOperation(ML.Classification, () => new CreateRandomForestClassifier(), p.next())
    registrar.registerOperation(ML.Clustering, () => new CreateKMeans(), p.next())
    registrar.registerOperation(ML.Clustering, () => new CreateLDA(), p.next())

    registrar.registerOperation(ML.FeatureSelection, () => new ChiSqSelector(), p.next())
    registrar.registerOperation(ML.Recommendation, () => new CreateALS(), p.next())
    registrar.registerOperation(ML.DimensionalityReduction, () => new PCA(), p.next())
    registrar.registerOperation(ML.ModelEvaluation, () => new CreatePythonEvaluator(), p.next())
    registrar.registerOperation(ML.ModelEvaluation, () => new CreateREvaluator(), p.next())
    // operations generated from Spark evaluator
    registrar.registerOperation(ML.ModelEvaluation, () => new CreateBinaryClassificationEvaluator(), p.next())
    registrar.registerOperation(ML.ModelEvaluation, () => new CreateMulticlassClassificationEvaluator(), p.next())
    registrar.registerOperation(ML.ModelEvaluation, () => new CreateRegressionEvaluator(), p.next())
  }
}
