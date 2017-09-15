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
    <td><code>input column</code></td>
    <td>"features"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"discretized_features"</td>
  </tr>
  <tr>
    <td><code>num buckets</code></td>
    <td>3.0</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>features</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>1.0</td>
    </tr>
    <tr>
      <td>2.0</td>
    </tr>
    <tr>
      <td>3.0</td>
    </tr>
    <tr>
      <td>4.0</td>
    </tr>
    <tr>
      <td>5.0</td>
    </tr>
    <tr>
      <td>6.0</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>discretized_features</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>1.0</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>2.0</td>
      <td>1.0</td>
    </tr>
    <tr>
      <td>3.0</td>
      <td>1.0</td>
    </tr>
    <tr>
      <td>4.0</td>
      <td>2.0</td>
    </tr>
    <tr>
      <td>5.0</td>
      <td>2.0</td>
    </tr>
    <tr>
      <td>6.0</td>
      <td>3.0</td>
    </tr>
  </tbody>
</table>
      
