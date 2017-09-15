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
    <td><code>split mode</code></td>
    <td>RANDOM</td>
  </tr>
  <tr>
    <td><code>split ratio</code></td>
    <td>0.2</td>
  </tr>
  <tr>
    <td><code>seed</code></td>
    <td>0.0</td>
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


#### Output 0

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
      <td>CityB</td>
      <td>2.0</td>
      <td>336677.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>4.0</td>
      <td>579560.0</td>
    </tr>
  </tbody>
</table>


#### Output 1

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
      <td>CityA</td>
      <td>3.0</td>
      <td>584639.0</td>
    </tr>
  </tbody>
</table>

      
