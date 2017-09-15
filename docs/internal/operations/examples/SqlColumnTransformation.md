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
    <td><code>input column alias</code></td>
    <td>"myAlias"</td>
  </tr>
  <tr>
    <td><code>formula</code></td>
    <td>"MINIMUM(myAlias, 2.0)"</td>
  </tr>
  <tr>
    <td><code>operate on</code></td>
    <td>one column</td>
  </tr>
  <tr>
    <td><code>input column</code></td>
    <td>"Weight"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"WeightCutoff"</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>Animal</th>
      <th>Kind</th>
      <th>Weight</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Cow</td>
      <td>Mammal</td>
      <td>300.0</td>
    </tr>
    <tr>
      <td>Ostrich</td>
      <td>Bird</td>
      <td>0.5</td>
    </tr>
    <tr>
      <td>Dog</td>
      <td>Mammal</td>
      <td>5.0</td>
    </tr>
    <tr>
      <td>Sparrow</td>
      <td>Bird</td>
      <td>0.5</td>
    </tr>
    <tr>
      <td>Thing</td>
      <td>null</td>
      <td>null</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>Animal</th>
      <th>Kind</th>
      <th>Weight</th>
      <th>WeightCutoff</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Cow</td>
      <td>Mammal</td>
      <td>300.0</td>
      <td>2.0</td>
    </tr>
    <tr>
      <td>Ostrich</td>
      <td>Bird</td>
      <td>0.5</td>
      <td>0.5</td>
    </tr>
    <tr>
      <td>Dog</td>
      <td>Mammal</td>
      <td>5.0</td>
      <td>2.0</td>
    </tr>
    <tr>
      <td>Sparrow</td>
      <td>Bird</td>
      <td>0.5</td>
      <td>0.5</td>
    </tr>
    <tr>
      <td>Thing</td>
      <td>null</td>
      <td>null</td>
      <td>null</td>
    </tr>
  </tbody>
</table>
      
