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
    <td><code>num top features</code></td>
    <td>1.0</td>
  </tr>
  <tr>
    <td><code>features column</code></td>
    <td>"features"</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"selected_features"</td>
  </tr>
  <tr>
    <td><code>label column</code></td>
    <td>"label"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>label</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[0.0,0.0,18.0,1.0]</td>
      <td>1.0</td>
    </tr>
    <tr>
      <td>[0.0,1.0,12.0,0.0]</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>[1.0,0.0,15.0,0.1]</td>
      <td>0.0</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>label</th>
      <th>selected_features</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[0.0,0.0,18.0,1.0]</td>
      <td>1.0</td>
      <td>[1.0]</td>
    </tr>
    <tr>
      <td>[0.0,1.0,12.0,0.0]</td>
      <td>0.0</td>
      <td>[0.0]</td>
    </tr>
    <tr>
      <td>[1.0,0.0,15.0,0.1]</td>
      <td>0.0</td>
      <td>[0.1]</td>
    </tr>
  </tbody>
</table>
      
