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
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"city"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"city_indexed"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>city</th>
      <th>price</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CityA</td>
      <td>695611.0</td>
    </tr>
    <tr>
      <td>CityC</td>
      <td>294691.0</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>430784.0</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>336677.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>584639.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>579560.0</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>city</th>
      <th>price</th>
      <th>city_indexed</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CityA</td>
      <td>695611.0</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>CityC</td>
      <td>294691.0</td>
      <td>2.0</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>430784.0</td>
      <td>1.0</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>336677.0</td>
      <td>1.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>584639.0</td>
      <td>0.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>579560.0</td>
      <td>0.0</td>
    </tr>
  </tbody>
</table>
      
