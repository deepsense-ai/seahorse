---
layout: global
displayTitle: Datasources
menuTab: reference
title: Datasources
description: Datasources
---
Seahorse has Datasources of type:

* **External File** - file accessibly via HTTP, HTTPS or FTP.
* **Library File** - file uploaded to Seahorse File Library
* **HDFS** - file on Hadoop Distributed File System
* **JDBC** - relational database supporting JDBC. [Requires adding JDBC driver JAR to Seahorse](#setting-up-jdbc-datasource-with-custom-jdbc-drivers)  
* **Google Spreadsheet** - [Requires setting up a Google Service Account](#setting-up-a-google-spreadsheet-datasource)

<br>
<div class="align-left">
    <div class="img-responsive image-with-caption-container" style="width: 700px">
        <img class="img-responsive bordered-image" alt="Datasources List" src="/img/datasources.png">
    </div>
</div>

## Setting up JDBC Datasource with custom JDBC Drivers

Reading data from and writing data to JDBC-compatible databases is supported.

This functionality requires placing adequate JDBC driver JAR file to Seahorse shared folder `jars`.
That file placement has to be performed before starting editing workflow that uses JDBC connection
(otherwise, it will be required to stop running session and start it again).

## Setting up a Google Spreadsheet Datasource

Google Sheets Datasource has two parameters that require more detailed description:

* **Google Service Account Credentials JSON** - Seahorse must authorize itself as a Google User to the Google Services. <br>
  This JSON has all data required by Google Services for Seahorse to authorize itself.
* **Google Spreadsheet link** - note that this Google Spreadsheet must be shared with Seahorse Google User.

<br>
<div class="align-left">
  <div class="img-responsive image-with-caption-container" style="width: 700px">
      <img class="img-responsive bordered-image" alt="Google Spreadsheet Params Forms" src="/img/google_sheet_params.png">
  </div>
</div>
<br>

In the following sections you will learn how to set up a Google Service Account for the Seahorse instance and
how to share the spreadsheets with the Seahorse instance.

### Setting up a Google Service Account

1. Set up Google Project with Google Drive API enabled
   1. Go to <a target="_blank" href="https://console.developers.google.com/apis/api/drive"> Drive API Page </a> <br>
      If you don't have a Google Project yet - create a new one.
   2. Enable Google Drive API.
2. Set up Google Service Account
   1. Go to <a target="_blank" href="https://console.developers.google.com/iam-admin/serviceaccounts"> Service Accounts  Page</a> <br>
   2. Select your Google Project
   3. Create new Google Service Account. <br>
      <span class="attention"> **IMPORTANT** </span> - Tick **’Furnish a new private key’** option and select **JSON** key type.
      Store downloaded JSON credentials file securely. You will need that JSON credentials file later. <br>

   <br>
   <div class="align-left-indented">
      <div class="img-responsive image-with-caption-container" style="width: 400px">
          <img class="img-responsive bordered-image" alt="Create Service Account Form" src="/img/create_new_service_account.png">
      </div>
   </div>
   <br>

3. Now you can include the JSON content into Google Service Account Credentials JSON field of the Datasource

### Sharing Google Spreadsheet with you Seahorse Instance

1. Obtain e-mail address of your Google Service Account from the
   <a target="_blank" href="https://console.developers.google.com/iam-admin/serviceaccounts">
   list of Service Accounts
   </a>

   <br>
   <div class="align-left-indented">
     <div class="img-responsive image-with-caption-container" style="width: 600px">
         <img class="img-responsive bordered-image" alt="Email Address for Google Service Account" src="/img/service_account_email.png">
     </div>
   </div>
   <br>

2. Share your Google Spreadsheet with your Google Service Account using e-mail address from step 1.

   <br>
   <div class="align-left-indented">
      <div class="img-responsive image-with-caption-container" style="width: 400px">
          <img class="img-responsive bordered-image" alt="Google Spreadsheet Share Form" src="/img/share-google-account.png">
      </div>
   </div>
   <br>

3. You can use Google Spreadsheet and you Google Service Account credentials to define a
Google Spreadsheet Datasource in Seahorse.

   <br>
   <div class="align-left-indented">
      <div class="img-responsive image-with-caption-container" style="width: 700px">
          <img class="img-responsive bordered-image" alt="Google Spreadsheet Param Forms" src="/img/google_sheet_params.png">
      </div>
   </div>
   <br>

Now it's ready to use in the Seahorse!
