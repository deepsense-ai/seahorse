## Installing Dependencies

* sudo apt-get install ruby-full
* sudo gem install jekyll
* sudo gem install pygments.rb
* Install nodejs v0.12+ from http://nodejs.org
  (download binary package, unpack, put bin folder under the PATH environment variable)
* sudo apt-get install linkchecker


### Alternative

* Use <a target="_blank" href="https://github.com/jekyll/docker-jekyll">docker-jekyll</a>

## Building Site

* sbt generateExamples
* jekyll build
# OR
* jekyll build --watch # Refresh after file changes

## Serving

* (cd _site; python -m SimpleHTTPServer)

## Viewing

Visit http://localhost:8000 (SimpleHTTPServer server HTTP at 0.0.0.0:8000 by default)
After modifications You do not have to stop SimpleHTTPServer, just re-run `jekyll build` and refresh page in browser.

## Rules of Capitalizing

Headings of first and second level should be capitalized.

The following names are considered proper and thus capitalized in every place:

* Deeplang
* Seahorse
* Workflow Executor
