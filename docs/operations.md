---
layout: documentation
displayTitle: Operations
docTab: operations
title: Operations
includeDeeplangMenu: true

description: Deepsense documentation homepage
---

Operations are base building blocks for Deeplang workflow.
Detailed description of operations can be found [here](deeplang.html#operations).

### Operations Catalog
List of all currently supported operations:

* Input/Output
  * [Read DataFrame](operations/read_dataframe.html)
  * [Write DataFrame](operations/write_dataframe.html)
  * [Notebook](operations/notebook.html)
* Transformations
  * [Custom Python Operation](operations/custom_python_operation.html)
  * [Create Custom Transformer](operations/create_custom_transformer.html)
  * [Create Mathematical Transformation](operations/create_mathematical_transformation.html)
  * [Apply Transformation](operations/apply_transformation.html)
  * [Transform](operations/transform.html)
  <!-- Spark ported operations -->
  * [Assemble Vector](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.VectorAssembler)
  * [Binarize](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.Binarizer)
  * [Convert to n-grams](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.NGram)
  * [DCT](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.DCT)
  * [HashingTF](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.HashingTF)
  * [Normalize](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.Normalizer)
  * [One Hot Encode](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.OneHotEncoder)
  * [Polynomial Expand](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.PolynomialExpansion)
  * [Remove Stop Words](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.StopWordsRemover)
  * [Tokenize](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.Tokenizer)
  * [Tokenize with Regex](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.RegexTokenizer)
* Data Manipulation
  * [Join](operations/join.html)
  * [Decompose Datetime](operations/decompose_datetime.html)
  * [Convert Type](operations/convert_type.html)
  * [Filter Columns](operations/filter_columns.html)
  * [Split](operations/split.html)
  * [Union](operations/union.html)
  * [SQL Expression](operations/sql_expression.html)
  * [Missing Values Handler](operations/missing_values_handler.html)
* Machine Learning
  * [PCA](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.PCA)
  * [Standard Scaler](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.StandardScaler)
  * [Vector Indexer](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.VectorIndex)
  * Recommendation
    * [ALS](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.recommendation.ALS)
  * Regression
    * [Logistic Regression](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.classification.LogisticRegression)
  * Classification
  * Clustering
  * Evaluation
    * [Binary Classification Evaluator](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.evaluation.BinaryClassificationEvaluator)
    * [Multiclass Classification Evaluator](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator)
    * [Regression Evaluator](http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.evaluation.RegressionEvaluator)
