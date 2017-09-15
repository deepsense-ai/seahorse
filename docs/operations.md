---
layout: documentation
displayTitle: Operations
docTab: operations
title: Operations
includeDeeplangMenu: true
includeOperationsMenu: true
description: Deepsense documentation homepage
---

Operations are base building blocks for Deeplang workflow.
Detailed description of operations can be found [here](deeplang_overview.html#operations).

### Operations Catalog
List of all currently supported operations:

* Input/Output
  * [Read DataFrame](operations/read_dataframe.html)
  * [Write DataFrame](operations/write_dataframe.html)
  * [Notebook](operations/notebook.html)
* Action
  * [Fit](operations/fit.html)
  * [Transform](operations/transform.html)
  * [Fit + Transform](operations/fit_plus_transform.html)
  * [Evaluate](operations/evaluate.html)
* Set Operation
  * [Join](operations/join.html)
  * [Split](operations/split.html)
  * [Union](operations/union.html)
* Filtering
  * [Filter Columns](operations/filter_columns.html)
  * [Filter Rows](operations/filter_rows.html)
  * [Handle Missing Values](operations/handle_missing_values.html)
* Transformation
  * Custom
    * [Create Custom Transformer](operations/create_custom_transformer.html)
    * SQL
      * [SQL Transformation](operations/sql_transformation.html)
      * [SQL Column Transformation](operations/sql_column_transformation.html)
    * Python
      * [Python Transformation](operations/python_transformation.html)
      * [Python Column Transformation](operations/python_column_transformation.html)
  * Feature Conversion
    * [Assemble Vector](operations/assemble_vector.html)
    * [Binarize](operations/binarize.html)
    * [Convert Type](operations/convert_type.html)
    * [DCT](operations/dct.html)
    * [Decompose Datetime](operations/decompose_datetime.html)
    * [Normalize](operations/normalize.html)
    * [One Hot Encoder](operations/one_hot_encoder.html)
    * [Polynomial Expansion](operations/polynomial_expansion.html)
    * [String Indexer](operations/string_indexer.html)
    * [Vector Indexer](operations/vector_indexer.html)
  * Feature Scaling
    * [Min-Max Scaler](operations/min-max_scaler.html)
    * [Standard Scaler](operations/standard_scaler.html)
  * Text Processing
    * [Convert to n-grams](operations/convert_to_n-grams.html)
    * [Count Vectorizer](operations/count_vectorizer.html)
    * [HashingTF](operations/hashingtf.html)
    * [IDF](operations/idf.html)
    * [Remove Stop Words](operations/remove_stop_words.html)
    * [Tokenize](operations/tokenize.html)
    * [Tokenize with Regex](operations/tokenize_with_regex.html)
    * [Word2Vec](operations/word2vec.html)
* Machine Learning
  * Classification
    * [GBT Classifier](operations/gbt_classifier.html)
    * [Logistic Regression](operations/logistic_regression.html)
    * [Multilayer Perceptron Classifier](operations/multilayer_perceptron_classifier.html)
  * Regression
    * [GBT Regressor](operations/gbt_regressor.html)
    * [Isotonic Regression](operations/isotonic_regression.html)
    * [Linear Regression](operations/linear_regression.html)
    * [Random Forest Regression](operations/random_forest_regression.html)
  * Clustering
    * [K-Means](operations/k-means.html)
  * Dimensionality Reduction
    * [PCA](operations/pca.html)
  * Recommendation
    * [ALS](operations/als.html)
  * Model Evaluation
    * [Binary Classification Evaluator](operations/binary_classification_evaluator.html)
    * [Multiclass Classification Evaluator](operations/multiclass_classification_evaluator.html)
    * [Regression Evaluator](operations/regression_evaluator.html)
  * [Grid Search](operations/grid_search.html)
