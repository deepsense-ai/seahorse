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
    <td><code>p</code></td>
    <td>2.0</td>
  </tr>
  <tr>
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"features"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"normalized"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>features</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[-2.0,2.3,0.0]</td>
    </tr>
    <tr>
      <td>[0.0,0.0,0.0]</td>
    </tr>
    <tr>
      <td>[0.6,-1.1,-3.0]</td>
    </tr>
    <tr>
      <td>[0.0,0.91,3.2]</td>
    </tr>
    <tr>
      <td>[5.7,0.72,2.7]</td>
    </tr>
    <tr>
      <td>[0.0,0.0,0.0]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>features</th>
      <th>normalized</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[-2.0,2.3,0.0]</td>
      <td>[-0.6561787149247866,0.7546055221635046,0.0]</td>
    </tr>
    <tr>
      <td>[0.0,0.0,0.0]</td>
      <td>[0.0,0.0,0.0]</td>
    </tr>
    <tr>
      <td>[0.6,-1.1,-3.0]</td>
      <td>[0.18454987557625951,-0.3383414385564758,-0.9227493778812975]</td>
    </tr>
    <tr>
      <td>[0.0,0.91,3.2]</td>
      <td>[0.0,0.2735299305180406,0.9618634919315713]</td>
    </tr>
    <tr>
      <td>[5.7,0.72,2.7]</td>
      <td>[0.8979061661970154,0.11341972625646508,0.4253239734617441]</td>
    </tr>
    <tr>
      <td>[0.0,0.0,0.0]</td>
      <td>[0.0,0.0,0.0]</td>
    </tr>
  </tbody>
</table>
      
