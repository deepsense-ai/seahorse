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
    <td>"vectors"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"indexed"</td>
  </tr>
  <tr>
    <td><code>max categories</code></td>
    <td>3.0</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>vectors</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[1.0,1.0,0.0,1.0]</td>
    </tr>
    <tr>
      <td>[0.0,1.0,1.0,1.0]</td>
    </tr>
    <tr>
      <td>[-1.0,1.0,2.0,0.0]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>vectors</th>
      <th>indexed</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[1.0,1.0,0.0,1.0]</td>
      <td>[2.0,0.0,0.0,1.0]</td>
    </tr>
    <tr>
      <td>[0.0,1.0,1.0,1.0]</td>
      <td>[0.0,0.0,1.0,1.0]</td>
    </tr>
    <tr>
      <td>[-1.0,1.0,2.0,0.0]</td>
      <td>[1.0,0.0,2.0,0.0]</td>
    </tr>
  </tbody>
</table>
      
