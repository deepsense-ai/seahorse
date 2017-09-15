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
    <td><code>drop last</code></td>
    <td>true</td>
  </tr>
  <tr>
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"labels"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"encoded"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>labels</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>a</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>a</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>b</td>
      <td>1.0</td>
    </tr>
    <tr>
      <td>c</td>
      <td>2.0</td>
    </tr>
    <tr>
      <td>a</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>b</td>
      <td>1.0</td>
    </tr>
    <tr>
      <td>a</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>a</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>c</td>
      <td>2.0</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>labels</th>
      <th>encoded</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>a</td>
      <td>0.0</td>
      <td>(2,[0],[1.0])</td>
    </tr>
    <tr>
      <td>a</td>
      <td>0.0</td>
      <td>(2,[0],[1.0])</td>
    </tr>
    <tr>
      <td>b</td>
      <td>1.0</td>
      <td>(2,[1],[1.0])</td>
    </tr>
    <tr>
      <td>c</td>
      <td>2.0</td>
      <td>(2,[],[])</td>
    </tr>
    <tr>
      <td>a</td>
      <td>0.0</td>
      <td>(2,[0],[1.0])</td>
    </tr>
    <tr>
      <td>b</td>
      <td>1.0</td>
      <td>(2,[1],[1.0])</td>
    </tr>
    <tr>
      <td>a</td>
      <td>0.0</td>
      <td>(2,[0],[1.0])</td>
    </tr>
    <tr>
      <td>a</td>
      <td>0.0</td>
      <td>(2,[0],[1.0])</td>
    </tr>
    <tr>
      <td>c</td>
      <td>2.0</td>
      <td>(2,[],[])</td>
    </tr>
  </tbody>
</table>
      
