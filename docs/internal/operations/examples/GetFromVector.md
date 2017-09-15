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
    <td><code>index</code></td>
    <td>1.0</td>
  </tr>
  <tr>
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
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
    <td>"second_feature"</td>
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
      <td>(3,[0,1],[-2.0,2.3])</td>
    </tr>
    <tr>
      <td>[0.01,0.2,3.0]</td>
    </tr>
    <tr>
      <td>null</td>
    </tr>
    <tr>
      <td>(3,[1,2],[0.91,3.2])</td>
    </tr>
    <tr>
      <td>(3,[0,2],[5.7,2.7])</td>
    </tr>
    <tr>
      <td>[0.0,0.0,0.0]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>second_feature</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>(3,[0,1],[-2.0,2.3])</td>
      <td>2.3</td>
    </tr>
    <tr>
      <td>[0.01,0.2,3.0]</td>
      <td>0.2</td>
    </tr>
    <tr>
      <td>null</td>
      <td>null</td>
    </tr>
    <tr>
      <td>(3,[1,2],[0.91,3.2])</td>
      <td>0.91</td>
    </tr>
    <tr>
      <td>(3,[0,2],[5.7,2.7])</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>[0.0,0.0,0.0]</td>
      <td>0.0</td>
    </tr>
  </tbody>
</table>
      
