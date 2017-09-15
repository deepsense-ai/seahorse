---
layout: documentation
displayTitle: Union
title: Union
description: Union
usesMathJax: true
includeOperationsMenu: true
---

Combines input [DataFrames](../classes/dataframe.html) $$ A $$ and $$ B $$
into a DataFrame $$ D $$ such that
$$ \forall_{x \in A \cup B} \; x \in D $$ and $$ |D| = |A| + |B| $$.

This means that each row from each of the input DataFrames will end up in the resulting DataFrame
with no deduplication effort.

The schemas of $$ A $$ and $$ B $$ have to be the same.

The order of rows in $$ A $$ and $$ B $$ is not guaranteed to be preserved in the $$ D $$.

If schemas of the input DataFrames do not match a ``SchemaMismatchException`` will be thrown.

**Since**: Seahorse 0.4.0

## Input

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Data Type</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>

<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Frist DataFrame</td>
</tr>

<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Second DataFrame</td>
</tr>

</tbody>
</table>

## Output

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Data Type</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Union of the input DataFrames</td>
</tr>
</tbody>
</table>

## Parameters

Union does not take any parameters.

## Example

### Input

#### Input port 0

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

#### Input port 1

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


### Output

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
            <td>3.0</td>
            <td>430784.0</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>3.0</td>
            <td>584639.0</td>
       </tr>
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
