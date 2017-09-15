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
    <td><code>columns</code></td>
    <td>Selected columns: by name: ["baths", "price"].</td>
  </tr>
  <tr>
    <td><code>strategy</code></td>
    <td>remove row</td>
  </tr>
  <tr>
    <td><code>missing value indicator</code></td>
    <td>No</td>
  </tr>
  <tr>
    <td><code>user-defined missing values</code></td>
    <td>User-defined missing values: ["-1.0"]</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>city</th>
      <th>beds</th>
      <th>baths</th>
      <th>sq_ft</th>
      <th>price</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td></td>
      <td>2.0</td>
      <td>1.0</td>
      <td>820.0</td>
      <td>449178.0</td>
    </tr>
    <tr>
      <td>CityC</td>
      <td>null</td>
      <td>1.0</td>
      <td>656.0</td>
      <td>267975.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>2.0</td>
      <td>null</td>
      <td>636.0</td>
      <td>348946.0</td>
    </tr>
    <tr>
      <td>CityA</td>
      <td>2.0</td>
      <td>1.0</td>
      <td>736.0</td>
      <td>NaN</td>
    </tr>
    <tr>
      <td>CityC</td>
      <td>2.0</td>
      <td>-1.0</td>
      <td>564.0</td>
      <td>264867.0</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>city</th>
      <th>beds</th>
      <th>baths</th>
      <th>sq_ft</th>
      <th>price</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td></td>
      <td>2.0</td>
      <td>1.0</td>
      <td>820.0</td>
      <td>449178.0</td>
    </tr>
    <tr>
      <td>CityC</td>
      <td>null</td>
      <td>1.0</td>
      <td>656.0</td>
      <td>267975.0</td>
    </tr>
  </tbody>
</table>
      
