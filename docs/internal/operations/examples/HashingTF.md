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
    <td><code>num features</code></td>
    <td>5.0</td>
  </tr>
  <tr>
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"signal"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"hash"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>signal</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[a,a,b,b,c,d]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>signal</th>
      <th>hash</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[a,a,b,b,c,d]</td>
      <td>(5,[0,2,3,4],[1.0,2.0,2.0,1.0])</td>
    </tr>
  </tbody>
</table>
      
