## Example

### Parameters

<table class="table">
  <thead>
    <tr>
      <th style="width:20%">Name</th>
      <th style="width:80%">Value</th>
    </tr>
  </thead>
  <tbody>
  <tr>
    <td><code>n</code></td>
    <td>3.0</td>
  </tr>
  <tr>
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"words"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"output"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>label</th>
      <th>words</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>0</td>
      <td>[Hi,I,heard,about,Spark]</td>
    </tr>
    <tr>
      <td>1</td>
      <td>[I,wish,Java,could,use,case,classes]</td>
    </tr>
    <tr>
      <td>2</td>
      <td>[Logistic,regression,models,are,neat]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>label</th>
      <th>words</th>
      <th>output</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>0</td>
      <td>[Hi,I,heard,about,Spark]</td>
      <td>[Hi I heard,I heard about,heard about Spark]</td>
    </tr>
    <tr>
      <td>1</td>
      <td>[I,wish,Java,could,use,case,classes]</td>
      <td>[I wish Java,wish Java could,Java could use,could use case,use case classes]</td>
    </tr>
    <tr>
      <td>2</td>
      <td>[Logistic,regression,models,are,neat]</td>
      <td>[Logistic regression models,regression models are,models are neat]</td>
    </tr>
  </tbody>
</table>
      
