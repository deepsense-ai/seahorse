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
    <td><code>input column</code></td>
    <td>"words"</td>
  </tr>
  <tr>
    <td><code>output</code></td>
    <td>append new column</td>
  </tr>
  <tr>
    <td><code>output column</code></td>
    <td>"vectors"</td>
  </tr>
  <tr>
    <td><code>max iterations</code></td>
    <td>1.0</td>
  </tr>
  <tr>
    <td><code>step size</code></td>
    <td>0.025</td>
  </tr>
  <tr>
    <td><code>seed</code></td>
    <td>0.0</td>
  </tr>
  <tr>
    <td><code>vector size</code></td>
    <td>5.0</td>
  </tr>
  <tr>
    <td><code>num partitions</code></td>
    <td>1.0</td>
  </tr>
  <tr>
    <td><code>min count</code></td>
    <td>2.0</td>
  </tr>
  </tbody>
</table>

### Input

<table class="table">
  <thead>
    <tr>
      <th>words</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[Lorem,ipsum,at,dolor]</td>
    </tr>
    <tr>
      <td>[Nullam,gravida,non,ipsum]</td>
    </tr>
    <tr>
      <td>[Etiam,at,nunc,lacinia]</td>
    </tr>
  </tbody>
</table>

### Output

<table class="table">
  <thead>
    <tr>
      <th>words</th>
      <th>vectors</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>[Lorem,ipsum,at,dolor]</td>
      <td>[-0.005179300147574395,0.006232483079656959,-3.8920482620596886E-4,0.01865686010569334,-0.023596201092004776]</td>
    </tr>
    <tr>
      <td>[Nullam,gravida,non,ipsum]</td>
      <td>[-0.006070327013731003,0.00392990792170167,0.005970077123492956,-0.0047599030658602715,-0.007559983059763908]</td>
    </tr>
    <tr>
      <td>[Etiam,at,nunc,lacinia]</td>
      <td>[8.910268661566079E-4,0.002302575157955289,-0.006359281949698925,0.023416763171553612,-0.016036218032240868]</td>
    </tr>
  </tbody>
</table>
      
