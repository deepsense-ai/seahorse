---
layout: global
menuTab: downloads
description: Seahorse downloads page
title: Try Seahorse
---

One of the main goals for Seahorse is to make it accessible to new users.
We provide several ways to quickly try Seahorse out.
Depending on your preferences or rules in your organization, you may choose:

<section class="deployment-section">
  <div class="row white">
    <div class="block">
      <div class="col-xs-12 col-sm-6 col-md-4">
        <ul class="deployment">
          <li class="header">
            <div class="wrapper">
              <div>
                <img src="img/logo.png" height="25px" alt="" />
              </div>
              <div style="height:2em">
                <big>Seahorse Desktop</big>
              </div>
            </div>
          </li>
          <li>Download a virtual image</li>
          <li>Contains all the components</li>
          <li>Includes single-node Apache Spark distribution</li>
          <li><a href="desktop_overview.html">Read more...</a></li>
          <li>
            <a target="_blank" href="//deepsense.io/get-seahorse" class="btn btn-primary active">Download</a>
          </li>
        </ul>
      </div>
      <div class="col-xs-12 col-sm-6 col-md-4">
        <ul class="deployment">
          <li class="header">
            <div class="wrapper">
              <div>
                <img src="img/tap.png" alt="TAP" height="25px" />
              </div>
              <div>
                <big>Seahorse on Trusted Analytics Platform</big>
              </div>
            </div>
          </li>
          <li>Build TAP platform customized to your organization's needs</li>
          <li>Use Seahorse on Yarn Clusters of any size</li>
          <li>Use OAuth to manage access to Seahorse</li>
          <li><a href="tap_overview.html">Read more...</a></li>
          <li>
            <a target="_blank" href="http://trustedanalytics.org/" class="btn btn-primary active">Visit TAP </a>
          </li>
        </ul>
      </div>
      <div class="col-xs-12 col-sm-6 col-md-4">
        <ul class="deployment">
          <li class="header">
            <div class="wrapper">
              <div>
                <img src="img/ibm_workbench.png" height="25px" alt="" />
              </div>
              <div>
                <big>Seahorse on IBM Data Scientist Workbench</big>
              </div>
            </div>
          </li>
          <li>Create an account on Data Scientist Workbench</li>
          <li>Start using Seahorse on provided virtual machine</li>
          <li>No download required</li>
          <li><a href="dswb_overview.html">Read more...</a></li>
          <li>
            <a target="_blank" href="https://datascientistworkbench.com/" class="btn btn-primary active">Create account</a>
          </li>
        </ul>
      </div>
    </div>
  </div>
</section>

Scaling up your Seahorse deployment in a production setting is covered in the
[Enterprise](enterprise.html) section.

Regardless of which Seahorse deployment version you choose for building a Spark application,
after it's completed, it can be [deployed on a production cluster](#spark-application-deployment).

### Spark Application Deployment

Seahorse makes it possible to build Spark applications interactively,
working directly with data in a running session. Your final workflow can be then exported
as a standalone Spark application that you can submit to any Spark cluster using
[Seahorse Batch Workflow Executor](internal/batch_workflow_executor_overview.html).
A list of precompiled binaries is available below.

<p style="text-align: center; font-style: italic">Version Matrix for Seahorse Batch Workflow Executor</p>


| **Seahorse Batch Workflow Executor Version** | **Apache Spark Version** | **Scala Version** | **Link** |
| 1.2.0 | 1.6 | 2.10 | <a target="_blank" href="https://s3.amazonaws.com/workflowexecutor/releases/1.2.0/workflowexecutor_2.10-1.2.0.jar">download</a> |
| 1.2.0 | 1.6 | 2.11 | <a target="_blank" href="https://s3.amazonaws.com/workflowexecutor/releases/1.2.0/workflowexecutor_2.11-1.2.0.jar">download</a> |
| 1.1.0 | 1.6 | 2.10 | <a target="_blank" href="https://s3.amazonaws.com/workflowexecutor/releases/1.1.0/workflowexecutor_2.10-1.1.0.jar">download</a> |
| 1.1.0 | 1.6 | 2.11 | <a target="_blank" href="https://s3.amazonaws.com/workflowexecutor/releases/1.1.0/workflowexecutor_2.11-1.1.0.jar">download</a> |

### Batch Workflow Executor Source Code

If you are interested in compiling Seahorse Batch Workflow Executor from source or working with the newest
bleeding-edge code, you can check out the master branch from our Git repository:

```
git clone {{ site.WORKFLOW_EXECUTOR_GITHUB_URL }}
```
