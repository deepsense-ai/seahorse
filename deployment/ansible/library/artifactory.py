#!/usr/bin/env python

# Cloned from https://github.com/DanielRedOak/ansible-mod-artifactory

DOCUMENTATION = '''
---
module: artifactory

short_description: Download artifacts from the Artifactory
description:
     - Download artifacts from the Artifactory
version_added: "1.8.2"
options:
  path:
    description:
      - "Path to download the artifact to"
    required: yes
    default: null
  filename:
    description:
      - "If you wish to rename the downloaded artifact, this filename will be used."
    required: no
    default: null
  base_url:
    description:
      - "Base URL for your Artifactory instance"
    required: yes
    default: null
  repository:
    description:
      - "Repository to use for artifacts"
    required: yes
    default: libs-release-local
  groupid:
    description:
      - "groupid path.  Ex. com.example.product
    required: yes
    default: null
  artifactid:
    description:
      - "artifactid"
    required: yes
    default: null
  version:
    description:
      - "version of the artifact to download"
  classifier:
    description:
      - "classifier of the artifact to download"
  properties:
    description:
      - "dictionary of properties to match when selecting an artifact to download"
    required: no
  shasum:
    description:
      - "if true, existing files on the filesystem will be compared vis sha1sum to the Artifactory artifact before downloading.  If matching, the action is skipped"
    required: false
    choices: [ "yes", "no" ]
    default: "yes"
  md5sum:
    description:
      - "if true, existing files on the filesystem will be compared via md5sum to the Artifactory artifact before downloading.  If matching, the action is skipped"
    required: false
    choices: [ "yes", "no" ]
    default: "no"
  username:
    description:
      - "username used to connect to the Artifactory"
  password:
    description:
      - "password used to connect to the Artifactory"

notes: Tested with Artifactory Cloud only at this time
requirements: Ansible 1.8.2 or higher
author: Ryan O'Keeffe
'''
EXAMPLES = '''
'''

import os.path

def dlurl(module, url, destfile):
  # Grab file
  fileget_resp, fileget_info = fetch_url(module, url)
  if fileget_info['status'] == 200:
    f = open(destfile, 'w')
    try:
      shutil.copyfileobj(fileget_resp, f)
    except Exception, err:
      os.remove(destfile)
      module.fail_json(msg="Failed to create file: %s" % str(err))
    f.close()
    fileget_resp.close()
  else:
    module.fail_json(msg="File failed to download with statuscode %s" % fileget_info['status'])

def main():
    module = AnsibleModule(
        argument_spec     = dict(
            state         = dict(default="present"),
            filename      = dict(required=False),
            path          = dict(required=True),
            base_url      = dict(required=True),
            repository    = dict(default='libs-release-local'),
            groupid       = dict(required=True),
            artifactid    = dict(required=True),
            version       = dict(required=True),
            classifier    = dict(required=False),
            properties    = dict(required=False),
            sha1sum       = dict(default=True, type='bool'),
            md5sum        = dict(default=False, type='bool'),
            url_username  = dict(required=False),
            url_password  = dict(required=False)
        ),
        #supports_check_mode=True
    )

    # Assign some usable variables to make life easier
    changed = False
    path = module.params['path']
    base_url = module.params['base_url']
    repository  = module.params['repository']
    groupid = module.params['groupid']
    artifactid = module.params['artifactid']
    version = module.params['version']
    classifier = module.params['classifier']
    domd5 = module.params['md5sum']
    dosha1 = module.params['sha1sum']
    filename = module.params['filename']
    properties = module.params['properties']


    sha1 = None
    md5 = None
    durl = None
    file = None
    dlfname = None
    changed = False
    headers=None

    # Build API call to get the file info URI
    get_api_call = base_url + "/api/search/gavc?g=" + groupid + "&a=" + artifactid + "&v=" + version + "&repos=" + repository
    if classifier:
        get_api_call += "&c=" + classifier
    if properties:
      headers = { 'X-Result-Detail': 'properties'}

    # Do the call
    urlget_resp, urlget_info = fetch_url(module, get_api_call, headers=headers)
    if urlget_info['status'] == 200:
        # Get the json fun.
        rjson = json.load(urlget_resp)
        urlget_resp.close()
        #Check properties to select matches
        selected_results = []
        if properties:
          # Loop through the results
          for result in rjson['results']:
            match = 0
            for key in properties:
              if key in result['properties']:
                if properties[key] == result['properties'][key]:
                  match +=1
            if match == len(properties):
              selected_results.append(result)
          if len(selected_results) == 0:
            module.fail_json(msg="No artifact matched all properties")
        else:
          selected_results = rjson['results']

        # Make sure we have a single result for now...
        # TODO Support multi artifact download based on this search
        if len(selected_results) == 1:
            artifact_url = selected_results[0]['uri']
            artinfo_resp, artinfo_info = fetch_url(module, artifact_url)
            if artinfo_info['status'] == 200:
                # Get the json fun.
                rjson_ainfo = json.load(artinfo_resp)
                sha1 = rjson_ainfo['checksums']['sha1']
                md5 = rjson_ainfo['checksums']['md5']
                durl = rjson_ainfo['downloadUri']
                file = durl.split('/')[-1]
                artinfo_resp.close()
            else:
              module.fail_json(msg="Failed to get Artifact information. with status code %s" % (artinfo_info['status']))
        elif len(selected_results) > 1:
          module.fail_json(msg="The Artifactory search query returned more than one result, please narrow the search.  Multiple artifact download may be supported in the future", result=selected_results)
        else:
          module.fail_json(msg="The Artifactory search query returned no results.")
    else:
      module.fail_json(msg="Request to Artifactory failed with status code %s" % (urlget_info['status']))


    # Do we need to rename when downloaded?
    if filename:
      dlfname = path + filename
    else:
      dlfname = path + file

    # Do checking to see if we need to download it/compare sums
    if os.path.isfile(dlfname):
      # Checksum time
      if dosha1:
        if module.sha1(dlfname) != sha1:
          changed = True
      if domd5:
        if module.md5(dlfname) != md5:
          changed = True
    else:
      changed = True
    if changed:
      dlurl(module, durl, dlfname)
      # Make sure the sums match now
      if dosha1:
        if module.sha1(dlfname) != sha1:
          module.fail_json(msg="Downloaded file does not match expected sha1")
      if domd5:
        if module.md5(dlfname) != md5:
          module.fail_json(msg="Downloaded file does not match expected md5")
    module.exit_json(changed=changed, filename=file, md5=md5, sha1=sha1, url=durl, status=urlget_info['status'])

from ansible.module_utils.basic import *
from ansible.module_utils.urls import *
main()
