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

    val priorityGen = SortPriority.coreInSequence
    registrar.registerOperation(IO, () => new PythonNotebook(), priorityGen.next())
    registrar.registerOperation(IO, () => new RNotebook(), priorityGen.next())
    registrar.registerOperation(IO, () => new ReadDatasource(), priorityGen.next())
    registrar.registerOperation(IO, () => new ReadTransformer(), priorityGen.next())
    registrar.registerOperation(IO, () => new WriteDatasource(), priorityGen.next())
    registrar.registerOperation(IO, () => new WriteTransformer(), priorityGen.next())

    // Operations API uses catalog to fetch data even for operation/{id} calls
    // To make Source and Sink accessible with this call they are added here
    // As a workaround frontend filters out those from operations pallete
    // FIXME Make operation/{id} calls not rely on catalog and introduce some kind of
    // additional 'all operations' map to be used in operation/{id} calls
    registrar.registerOperation(IO, () => Source(), priorityGen.next())
    registrar.registerOperation(IO, () => Sink(), priorityGen.next())

    registrar.registerOperation(Action, () => new Evaluate(), priorityGen.next())
    registrar.registerOperation(Action, () => new Fit(), priorityGen.next())
    registrar.registerOperation(Action, () => new FitPlusTransform(), priorityGen.next())
    registrar.registerOperation(Action, () => new Transform(), priorityGen.next())
    registrar.registerOperation(SetOperation, () => new Join(), priorityGen.next())
    registrar.registerOperation(SetOperation, () => new Split(), priorityGen.next())
    registrar.registerOperation(SetOperation, () => new Union(), priorityGen.next())
    registrar.registerOperation(SetOperation, () => new SqlCombine(), priorityGen.next())
    registrar.registerOperation(SetOperation, () => new SortTransformation(), priorityGen.next())
    registrar.registerOperation(Filtering, () => new FilterColumns(), priorityGen.next())
    registrar.registerOperation(Filtering, () => new FilterRows(), priorityGen.next())
    registrar.registerOperation(Filtering, () => new HandleMissingValues(), priorityGen.next())
    registrar.registerOperation(Filtering, () => new Projection(), priorityGen.next())

    registrar.registerOperation(Transformation.Custom, () => new CreateCustomTransformer(), priorityGen.next())
    registrar.registerOperation(Transformation.Custom, () => new SqlColumnTransformation(), priorityGen.next())
    registrar.registerOperation(Transformation.Custom, () => new SqlTransformation(), priorityGen.next())
    registrar.registerOperation(Transformation.Custom, () => new PythonColumnTransformation(), priorityGen.next())
    registrar.registerOperation(Transformation.Custom, () => new PythonTransformation(), priorityGen.next())
    registrar.registerOperation(Transformation.Custom, () => new RColumnTransformation(), priorityGen.next())
    registrar.registerOperation(Transformation.Custom, () => new RTransformation(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new AssembleVector(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new Binarize(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new ComposeDatetime(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new ConvertType(), priorityGen.next())

    registrar.registerOperation(Transformation.FeatureConversion, () => new DCT(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new DecomposeDatetime(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new GetFromVector(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new Normalize(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new OneHotEncode(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new PolynomialExpand(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new QuantileDiscretizer(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new StringIndexer(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureConversion, () => new VectorIndexer(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureScaling, () => new MinMaxScaler(), priorityGen.next())
    registrar.registerOperation(Transformation.FeatureScaling, () => new StandardScaler(), priorityGen.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new ConvertToNGrams(), priorityGen.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new CountVectorizer(), priorityGen.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new HashingTF(), priorityGen.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new IDF(), priorityGen.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new RemoveStopWords(), priorityGen.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new Tokenize(), priorityGen.next())
    registrar.registerOperation(Transformation.TextProcessing, () => new TokenizeWithRegex(), priorityGen.next())

    registrar.registerOperation(Transformation.TextProcessing, () => new Word2Vec(), priorityGen.next())
    registrar.registerOperation(ML.HyperOptimization, () => new GridSearch(), priorityGen.next())
    registrar.registerOperation(ML.Regression, () => new CreateAFTSurvivalRegression(), priorityGen.next())
    registrar.registerOperation(ML.Regression, () => new CreateDecisionTreeRegression(), priorityGen.next())
    registrar.registerOperation(ML.Regression, () => new CreateGBTRegression(), priorityGen.next())
    registrar.registerOperation(ML.Regression, () => new CreateIsotonicRegression(), priorityGen.next())
    registrar.registerOperation(ML.Regression, () => new CreateLinearRegression(), priorityGen.next())
    registrar.registerOperation(ML.Regression, () => new CreateRandomForestRegression(), priorityGen.next())
    registrar.registerOperation(ML.Classification, () => new CreateDecisionTreeClassifier(), priorityGen.next())
    registrar.registerOperation(ML.Classification, () => new CreateGBTClassifier(), priorityGen.next())
    registrar.registerOperation(ML.Classification, () => new CreateLogisticRegression(), priorityGen.next())
    registrar.registerOperation(ML.Classification, () => new CreateMultilayerPerceptronClassifier(), priorityGen.next())
    registrar.registerOperation(ML.Classification, () => new CreateNaiveBayes(), priorityGen.next())
    registrar.registerOperation(ML.Classification, () => new CreateRandomForestClassifier(), priorityGen.next())
    registrar.registerOperation(ML.Clustering, () => new CreateKMeans(), priorityGen.next())
    registrar.registerOperation(ML.Clustering, () => new CreateLDA(), priorityGen.next())

    registrar.registerOperation(ML.FeatureSelection, () => new ChiSqSelector(), priorityGen.next())
    registrar.registerOperation(ML.Recommendation, () => new CreateALS(), priorityGen.next())
    registrar.registerOperation(ML.DimensionalityReduction, () => new PCA(), priorityGen.next())
    registrar.registerOperation(ML.ModelEvaluation, () => new CreatePythonEvaluator(), priorityGen.next())
    registrar.registerOperation(ML.ModelEvaluation, () => new CreateREvaluator(), priorityGen.next())
    // operations generated from Spark evaluator
    registrar.registerOperation(ML.ModelEvaluation, () => new CreateBinaryClassificationEvaluator(), priorityGen.next())
    registrar.registerOperation(ML.ModelEvaluation, () => new CreateMulticlassClassificationEvaluator(),
      priorityGen.next())
    registrar.registerOperation(ML.ModelEvaluation, () => new CreateRegressionEvaluator(), priorityGen.next())
  }
}
