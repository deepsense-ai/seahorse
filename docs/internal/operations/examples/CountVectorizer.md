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
    <td>"lines"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"lines_out"</td>
  </tr>
  <tr>
    <td><code>max vocabulary size</code></td>
    <td>262144.0</td>
  </tr>
  <tr>
    <td><code>min different documents</code></td>
    <td>1.0</td>
  </tr>
  <tr>
    <td><code>min term frequency</code></td>
    <td>3.0</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>lines</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[a,a,a,b,b,c,c,c,d]</td>
    </tr>
    <tr>
      <td>[c,c,c,c,c,c]</td>
    </tr>
    <tr>
      <td>[a]</td>
    </tr>
    <tr>
      <td>[e,e,e,e,e]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>lines</th>
      <th>lines_out</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[a,a,a,b,b,c,c,c,d]</td>
      <td>(5,[0,2],[3.0,3.0])</td>
    </tr>
    <tr>
      <td>[c,c,c,c,c,c]</td>
      <td>(5,[0],[6.0])</td>
    </tr>
    <tr>
      <td>[a]</td>
      <td>(5,[],[])</td>
    </tr>
    <tr>
      <td>[e,e,e,e,e]</td>
      <td>(5,[1],[5.0])</td>
    </tr>
  </tbody>
</table>
      
