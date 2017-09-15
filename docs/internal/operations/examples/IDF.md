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
    <td>"values"</td>
  </tr>
  <tr>
    <td><code>min documents frequency</code></td>
    <td>0.0</td>
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
      <td>[0.0,1.0,0.0,2.0]</td>
    </tr>
    <tr>
      <td>[0.0,1.0,2.0,3.0]</td>
    </tr>
    <tr>
      <td>[0.0,1.0,0.0,0.0]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>values</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[0.0,1.0,0.0,2.0]</td>
      <td>[0.0,0.0,0.0,0.5753641449035617]</td>
    </tr>
    <tr>
      <td>[0.0,1.0,2.0,3.0]</td>
      <td>[0.0,0.0,1.3862943611198906,0.8630462173553426]</td>
    </tr>
    <tr>
      <td>[0.0,1.0,0.0,0.0]</td>
      <td>[0.0,0.0,0.0,0.0]</td>
    </tr>
  </tbody>
</table>
      
