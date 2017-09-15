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
    <td>"scaled"</td>
  </tr>
  <tr>
    <td><code>with mean</code></td>
    <td>false</td>
  </tr>
  <tr>
    <td><code>with std</code></td>
    <td>true</td>
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
      <td>[-2.0,2.3,0.0]</td>
    </tr>
    <tr>
      <td>[0.0,-5.1,1.0]</td>
    </tr>
    <tr>
      <td>[1.7,-0.6,3.3]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>scaled</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[-2.0,2.3,0.0]</td>
      <td>[-1.0798984943120777,0.6168340914150375,0.0]</td>
    </tr>
    <tr>
      <td>[0.0,-5.1,1.0]</td>
      <td>[0.0,-1.3677625505289963,0.5909681092664519]</td>
    </tr>
    <tr>
      <td>[1.7,-0.6,3.3]</td>
      <td>[0.9179137201652661,-0.16091324123870546,1.9501947605792913]</td>
    </tr>
  </tbody>
</table>
      
