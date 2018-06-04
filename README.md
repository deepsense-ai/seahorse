# Seahorse

Seahorse is an open-source visual framework allowing you to create Apache Spark applications
in a fast, simple and interactive way.

Seahorse is distributed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).

Read more about Seahorse on the documentation page: [seahorse.deepsense.ai](https://seahorse.deepsense.ai/).

## Building Seahorse from source

Prerequisites:
* docker 1.30
* docker-compose 1.9
* JDK 8
* sbt 0.13
* python 2.7
  * PyYAML
* npm 4.6
* jekyll 3.2
  * pygments.rb
  * jekyll-sass-converter
  * jekyll-watch
* PhantomJS

Run
```console
./build/build_all.sh
```
This will build all the needed docker images and create a `docker-compose.yml` file.
You can now run it using `docker-compose up`. Seahorse will start at [http://localhost:33321](http://localhost:33321).

A good place to start using Seahorse is the [basic examples](http://seahorse.deepsense.ai/basic_examples.html) section of the documentation.

## Development

Note that in order to contribute to Seahorse you have to sign the
[Contributor License Agreement](https://seahorse.deepsense.ai/licenses/cla).

Before submitting a PR, please run the Scala style check:

```console
sbt scalastylebackend && (cd ./seahorse-workflow-executor && sbt scalastyle)
```

### Running tests

Initialize the submodules before running the tests:
```console
git submodule init
git submodule update
```

Backend tests:
```console
./build/build_and_run_tests.sh
```

Frontend tests:
```console
./frontend/run_unit_tests.sh
```

End-to-end integration tests:
```console
./build/e2e_tests.sh -a
```

In order for Seahorse to compile and run correctly on Mac OS, you need to increase memory for Docker engine to at least 6GB.

### Bash completion for Python scripts

Some of our Python scripts used by devs support bash autocompletion using argcomplete.

```
pip install argcomplete
activate-global-python-argcomplete --user
```

See [this](http://argcomplete.readthedocs.io/en/latest/#activating-global-completion) for global completion support.

#### Mac OS
Note, that bash 4.2 is required.
[Installation instruction for Mac users](http://argcomplete.readthedocs.io/en/latest/#global-completion)

After the bash upgrade, you may have to rename `.bash_profile` to `.bashrc`. And maybe add `/usr/local/bin` to $PATH.
Also, check if you're actually running the new bash with `echo $BASH_VERSION` - your terminal might still be using the old one.

### Developing SDK operations on local repository
To compile and test SDK operations on local repository, you can use `seahorse-sdk-example` submodule
```console
git submodule init
git submodule update
./build/prepare_sdk_dependencies.sh
```
Now it will compile and test against the local Seahorse repository:
```console
cd seahorse-sdk-example
sbt test
```

## Enterprise options and support

Seahorse was originally created at [deepsense.ai](http://deepsense.ai). Technical support and customization options are available upon [contact](mailto:contact@deepsense.ai).
