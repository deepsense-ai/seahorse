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
    <td><code>target type</code></td>
    <td>int</td>
  </tr>
  <tr>
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"beds"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"beds_int"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>city</th>
      <th>beds</th>
      <th>price</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CityA</td>
      <td>4.0</td>
      <td>695611.0</td>
    </tr>
    <tr>
      <td>CityC</td>
      <td>2.0</td>
      <td>294691.0</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>3.0</td>
      <td>430784.0</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>2.0</td>
      <td>336677.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>3.0</td>
      <td>584639.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>4.0</td>
      <td>579560.0</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>city</th>
      <th>beds</th>
      <th>price</th>
      <th>beds_int</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CityA</td>
      <td>4.0</td>
      <td>695611.0</td>
      <td>4</td>
    </tr>
    <tr>
      <td>CityC</td>
      <td>2.0</td>
      <td>294691.0</td>
      <td>2</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>3.0</td>
      <td>430784.0</td>
      <td>3</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>2.0</td>
      <td>336677.0</td>
      <td>2</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>3.0</td>
      <td>584639.0</td>
      <td>3</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>4.0</td>
      <td>579560.0</td>
      <td>4</td>
    </tr>
  </tbody>
</table>
      
