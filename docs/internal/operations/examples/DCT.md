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
    <td><code>inverse</code></td>
    <td>false</td>
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
    <td>"output"</td>
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
      <td>[0.0,1.0,-2.0,3.0]</td>
    </tr>
    <tr>
      <td>[-1.0,2.0,4.0,-7.0]</td>
    </tr>
    <tr>
      <td>[14.0,-2.0,-5.0,1.0]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>output</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[0.0,1.0,-2.0,3.0]</td>
      <td>[1.0,-1.1480502970952693,2.0000000000000004,-2.7716385975338604]</td>
    </tr>
    <tr>
      <td>[-1.0,2.0,4.0,-7.0]</td>
      <td>[-1.0,3.378492794482933,-7.000000000000001,2.9301512653149677]</td>
    </tr>
    <tr>
      <td>[14.0,-2.0,-5.0,1.0]</td>
      <td>[4.0,9.304453421915744,11.000000000000002,1.5579302036357163]</td>
    </tr>
  </tbody>
</table>
      
