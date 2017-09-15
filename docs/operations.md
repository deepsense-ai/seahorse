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
  * [Notebook](operations/notebook.html)
  * [Read DataFrame](operations/read_dataframe.html)
  * [Read Transformer](operations/read_transformer.html)
  * [Write DataFrame](operations/write_dataframe.html)
  * [Write Transformer](operations/write_transformer.html)
* Action
  * [Evaluate](operations/evaluate.html)
  * [Fit](operations/fit.html)
  * [Fit + Transform](operations/fit_plus_transform.html)
  * [Transform](operations/transform.html)
* Set Operation
  * [Join](operations/join.html)
  * [Split](operations/split.html)
  * [Union](operations/union.html)
* Filtering
  * [Filter Columns](operations/filter_columns.html)
  * [Filter Rows](operations/filter_rows.html)
  * [Handle Missing Values](operations/handle_missing_values.html)
  * [Projection](operations/projection.html)
* Transformation
  * Custom
    * [Create Custom Transformer](operations/create_custom_transformer.html)
    * SQL
      * [SQL Column Transformation](operations/sql_column_transformation.html)
      * [SQL Transformation](operations/sql_transformation.html)
    * Python
      * [Python Column Transformation](operations/python_column_transformation.html)
      * [Python Transformation](operations/python_transformation.html)
  * Feature Conversion
    * [Assemble Vector](operations/assemble_vector.html)
    * [Binarize](operations/binarize.html)
    * [Convert Type](operations/convert_type.html)
    * [DCT](operations/dct.html)
    * [Decompose Datetime](operations/decompose_datetime.html)
    * [Normalize](operations/normalize.html)
    * [One Hot Encoder](operations/one_hot_encoder.html)
    * [Polynomial Expansion](operations/polynomial_expansion.html)
    * [Quantile Discretizer](operations/quantile_discretizer.html)
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
  * Hyper Optimization
    * [Grid Search](operations/grid_search.html)
  * Regression
    * [AFT Survival Regression](operations/aft_survival_regression.html)
    * [Decision Tree Regression](operations/decision_tree_regression.html)
    * [GBT Regression](operations/gbt_regression.html)
    * [Isotonic Regression](operations/isotonic_regression.html)
    * [Linear Regression](operations/linear_regression.html)
    * [Random Forest Regression](operations/random_forest_regression.html)
  * Classification
    * [Decision Tree Classifier](operations/decision_tree_classifier.html)
    * [GBT Classifier](operations/gbt_classifier.html)
    * [Logistic Regression](operations/logistic_regression.html)
    * [Multilayer Perceptron Classifier](operations/multilayer_perceptron_classifier.html)
    * [Naive Bayes](operations/naive_bayes.html)
    * [Random Forest Classifier](operations/random_forest_classifier.html)
  * Clustering
    * [K-Means](operations/k-means.html)
    * [LDA](operations/lda.html)
  * Feature Selection
    * [Chi-Squared Selector](operations/chi-squared_selector.html)
  * Recommendation
    * [ALS](operations/als.html)
  * Dimensionality Reduction
    * [PCA](operations/pca.html)
  * Model Evaluation
    * [Binary Classification Evaluator](operations/binary_classification_evaluator.html)
    * [Multiclass Classification Evaluator](operations/multiclass_classification_evaluator.html)
    * [Regression Evaluator](operations/regression_evaluator.html)
