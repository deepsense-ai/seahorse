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
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"sentence"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"tokenized"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>label</th>
      <th>sentence</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>0</td>
      <td>Hi I heard about Spark</td>
    </tr>
    <tr>
      <td>1</td>
      <td>I wish Java could use case classes</td>
    </tr>
    <tr>
      <td>2</td>
      <td>Logistic,regression,models,are,neat</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>label</th>
      <th>sentence</th>
      <th>tokenized</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>0</td>
      <td>Hi I heard about Spark</td>
      <td>[hi,i,heard,about,spark]</td>
    </tr>
    <tr>
      <td>1</td>
      <td>I wish Java could use case classes</td>
      <td>[i,wish,java,could,use,case,classes]</td>
    </tr>
    <tr>
      <td>2</td>
      <td>Logistic,regression,models,are,neat</td>
      <td>[logistic,regression,models,are,neat]</td>
    </tr>
  </tbody>
</table>
      
