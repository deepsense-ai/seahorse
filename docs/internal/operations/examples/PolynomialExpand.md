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
    <td><code>degree</code></td>
    <td>2.0</td>
  </tr>
  <tr>
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"input"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"expanded"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>input</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[-2.0,2.3,0.0]</td>
    </tr>
    <tr>
      <td>[-2.0,2.3]</td>
    </tr>
    <tr>
      <td>[0.0,0.0,0.0]</td>
    </tr>
    <tr>
      <td>[0.6,-1.1,-3.0]</td>
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
      <th>input</th>
      <th>expanded</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[-2.0,2.3,0.0]</td>
      <td>[-2.0,4.0,2.3,-4.6,5.289999999999999,0.0,0.0,0.0,0.0]</td>
    </tr>
    <tr>
      <td>[-2.0,2.3]</td>
      <td>[-2.0,4.0,2.3,-4.6,5.289999999999999]</td>
    </tr>
    <tr>
      <td>[0.0,0.0,0.0]</td>
      <td>[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0]</td>
    </tr>
    <tr>
      <td>[0.6,-1.1,-3.0]</td>
      <td>[0.6,0.36,-1.1,-0.66,1.2100000000000002,-3.0,-1.7999999999999998,3.3000000000000003,9.0]</td>
    </tr>
    <tr>
      <td>[0.0,0.0,0.0]</td>
      <td>[0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0]</td>
    </tr>
  </tbody>
</table>
      
