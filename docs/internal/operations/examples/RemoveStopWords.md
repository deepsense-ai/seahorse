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
    <td><code>case sensitive</code></td>
    <td>false</td>
  </tr>
  <tr>
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"raw"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"removed"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>id</th>
      <th>raw</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>0</td>
      <td>[I,saw,the,red,baloon]</td>
    </tr>
    <tr>
      <td>1</td>
      <td>[Mary,had,a,little,lamb]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>id</th>
      <th>raw</th>
      <th>removed</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>0</td>
      <td>[I,saw,the,red,baloon]</td>
      <td>[saw,red,baloon]</td>
    </tr>
    <tr>
      <td>1</td>
      <td>[Mary,had,a,little,lamb]</td>
      <td>[Mary,little,lamb]</td>
    </tr>
  </tbody>
</table>
      
