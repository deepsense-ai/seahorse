---
layout: documentation
displayTitle: Seahorse Editor
docTab: seahorse_editor_overview
title: Seahorse Editor
includeTechnicalOverviewMenu: true
description: Seahorse Desktop
---


**Table of Contents**

* Table of Contents
{:toc}

## Overview

Seahorse Editor allows you to visually create Spark Applications - workflows.
You can create your own workflows and share them with other users.

From the user perspective **Seahorse Editor** is divided into two components:
**Workflows Manager Interface** and **Interactive Designer**

## Workflows Manager Interface

**Workflow Manager Interface** allows the user to:

* List the created workflows
* Create new workflows
* Clone existing workflows (using ![clone button](./img/clone_button.png) button)
* Stop running sessions (using ![clone button](./img/kill_session.png) button)
* Delete workflows
* Import workflows stored in [JSON format](workflowfile.html)


<div class="centered-container" markdown="1">
  ![Workflow Manager Interface](./img/workflow_manager.png){: .centered-image .img-responsive}
  *Workflow Manager Interface*
</div>


## Interactive Designer

**Interactive Designer** allows the user to view, design and execute workflows
on Apache Spark cluster.


<div class="centered-container" markdown="1">
  ![Interactive Designer](./img/interactive_designer.png){: .centered-image .img-responsive}
</div>

It consists of the following panels:

### Operation Palette
Located on the left, contains the list of all the available operations.
User can drag and drop the operations onto the canvas.
The search is available on the top of the panel, it filters the
available operations.

<div class="centered-container" markdown="1">
  ![Operation Palette](./img/editor_operations_palette.png){: .centered-image .img-responsive}
  *Operation Palette*
</div>

### Workflow Canvas

This panel is located in the center. It allows to view and design your workflow.
It allows the user to connect operations output ports as
other operations input ports.

<div class="centered-container" markdown="1">
  ![Operation Palette](./img/editor_workflow_canvas.png){: .centered-image .img-responsive}
  *Workflow Canvas*
</div>

### Operation Parameters

This panel is located on the right.
It displays parameters available for the operation selected in the
**Workflow Canvas**.

<div class="centered-container" markdown="1">
  ![Operation Palette](./img/editor_parameters.png){: .centered-image .img-responsive}
  *Operation parameters*
</div>

### Execution Report

Located at the bottom of the screen. **Execution Report** is available
once the operation has been executed. It displays the information about the
operation results. The report is automatically displayed, once the user clicks
on the output port of the node representing the operation on the on **Workflow Canvas**.

<div class="centered-container" markdown="1">
  ![Operation Palette](./img/editor_report.png){: .centered-image .img-responsive}
  *Execution report*
</div>

### Workflow Operations Menu

This panel allows the users to:

* Export workflow as a JSON file.
* Start/Stop Editing. This starts/stops the interactive session on Apache Spark.
* Clear. Removes all the operations from the **Workflow Canvas**.
* Run. Executes current workflow.
Available only if the Seahorse in in the Editing Mode.
* Documentation. A link to the Seahorse documentation.

<div class="centered-container" markdown="1">
  ![Top level menu](./img/editor_top_level_menu.png){: .centered-image .img-responsive}
  *Top level menu*
</div>
