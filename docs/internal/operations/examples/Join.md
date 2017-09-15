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
    <td><code>left prefix</code></td>
    <td>"left_"</td>
  </tr>
  <tr>
    <td><code>right prefix</code></td>
    <td>"right_"</td>
  </tr>
  <tr>
    <td><code>join columns</code></td>
    <td>Join on left.city == right.city</td>
  </tr>
  </tbody>
</table>

### Input


#### Input 0

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


#### Input 1

<table class="table">
  <thead>
    <tr>
      <th>city</th>
      <th>beds</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CityA</td>
      <td>4.0</td>
    </tr>
    <tr>
      <td>CityC</td>
      <td>2.0</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>3.0</td>
    </tr>
  </tbody>
</table>


### Output

<table class="table">
  <thead>
    <tr>
      <th>left_city</th>
      <th>left_price</th>
      <th>right_beds</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>CityA</td>
      <td>695611.0</td>
      <td>4.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>584639.0</td>
      <td>4.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>579560.0</td>
      <td>4.0</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>430784.0</td>
      <td>3.0</td>
    </tr>
    <tr>
      <td>CityB</td>
      <td>336677.0</td>
      <td>3.0</td>
    </tr>
    <tr>
      <td>CityC</td>
      <td>294691.0</td>
      <td>2.0</td>
    </tr>
  </tbody>
</table>
      
