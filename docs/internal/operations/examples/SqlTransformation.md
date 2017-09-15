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
    <td><code>dataframe id</code></td>
    <td>"inputDF"</td>
  </tr>
  <tr>
    <td><code>expression</code></td>
    <td>"select avg(temp) as avg_temp, max(windspeed) as max_windspeed from inputDF"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>datetime</th>
      <th>windspeed</th>
      <th>hum</th>
      <th>temp</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>2011-01-03 21:00:00.0</td>
      <td>0.1045</td>
      <td>0.47</td>
      <td>0.2</td>
    </tr>
    <tr>
      <td>2011-01-03 22:00:00.0</td>
      <td>0.1343</td>
      <td>0.64</td>
      <td>0.18</td>
    </tr>
    <tr>
      <td>2011-01-03 23:00:00.0</td>
      <td>0.1343</td>
      <td>0.69</td>
      <td>0.14</td>
    </tr>
    <tr>
      <td>2011-02-11 07:00:00.0</td>
      <td>0.0</td>
      <td>0.68</td>
      <td>0.1</td>
    </tr>
    <tr>
      <td>2011-02-13 18:00:00.0</td>
      <td>0.3284</td>
      <td>0.28</td>
      <td>0.42</td>
    </tr>
    <tr>
      <td>2011-02-18 12:00:00.0</td>
      <td>0.1642</td>
      <td>0.72</td>
      <td>0.44</td>
    </tr>
    <tr>
      <td>2011-02-19 03:00:00.0</td>
      <td>0.3881</td>
      <td>0.13</td>
      <td>0.44</td>
    </tr>
    <tr>
      <td>2011-02-19 04:00:00.0</td>
      <td>0.2985</td>
      <td>0.14</td>
      <td>0.42</td>
    </tr>
    <tr>
      <td>2013-01-01 00:00:00.0</td>
      <td>0.1343</td>
      <td>0.65</td>
      <td>0.26</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>avg_temp</th>
      <th>max_windspeed</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>0.2888888888888889</td>
      <td>0.3881</td>
    </tr>
  </tbody>
</table>
      
