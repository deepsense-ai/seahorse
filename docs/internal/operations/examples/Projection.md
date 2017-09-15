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
    <td><code>projection columns</code></td>
    <td>Select columns: <code>price</code>, <code>city</code>, <code>city</code> (renamed to <code>location</code>)</td>
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
      <th>price</th>
      <th>city</th>
      <th>location</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>695611.0</td>
      <td>CityA</td>
      <td>CityA</td>
    </tr>
    <tr>
      <td>294691.0</td>
      <td>CityC</td>
      <td>CityC</td>
    </tr>
    <tr>
      <td>430784.0</td>
      <td>CityB</td>
      <td>CityB</td>
    </tr>
    <tr>
      <td>336677.0</td>
      <td>CityB</td>
      <td>CityB</td>
    </tr>
    <tr>
      <td>584639.0</td>
      <td>CityA</td>
      <td>CityA</td>
    </tr>
    <tr>
      <td>579560.0</td>
      <td>CityA</td>
      <td>CityA</td>
    </tr>
  </tbody>
</table>
      
