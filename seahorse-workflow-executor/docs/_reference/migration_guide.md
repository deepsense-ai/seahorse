---
layout: global
displayTitle: Migration Guide
menuTab: reference
title: Migration Guide
description: Seahorse Migration Guide
---

**Table of Contents**

* Table of Contents
{:toc}

## Migrating data from Seahorse 1.3 to Seahorse 1.4
The data that can be migrated from Seahorse 1.3 to Seahorse 1.4 includes:

1. the database containing the workflow, cluster and notebook information
2. the library containing your data files

### Finding your old data
* If using `docker-compose.yml`:
    - Database is located in `h2-data` directory located next to your old `docker-compose.yml`.
    - To find Library data, locate your old library docker volume: `docker volume inspect DIRECTORYNAME_library` (usually `/var/lib/docker/volumes/DIRECTORYNAME_library/_data/`),
    where DIRECTORYNAME should be replaced by the name of the immediate directory containing docker-compose, e.g. if your `docker-compose.yml` is placed in `/exa/mple/path/` , DIRECTORYNAME would be "path".

* If using `Vagrantfile`:
    - Database is located in the VM's filesystem, in `/resources/h2-data`.
    - Library data is located in the Vagrant VM's filesystem, in `/var/lib/docker/volumes/resources_library/_data`.

    Note that you need to ssh into your Vagrant to access that data.


### Inserting the data into the new Seahorse instance
Create new `h2-data` and `library` directories next to your new `docker-compose.yml` or `Vagrantfile`.
Move the retrieved workflow database data into the `h2-data` directory and retrieved library files into the `library` directory.

If some of these directories already exist, you can replace them with the retrieved ones. Note that in this case all the work done in Seahorse 1.4 up to this point will be lost.

Seahorse will automatically convert 1.3-compatible data into the new format upon launch.
