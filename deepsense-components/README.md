deepsense-components
==================

DeepSense.io LAB application components

This directory contains `AngularJS` based self contained components that can be re-used in DeepSense apps.
Every component is an unpublished NPM package.

#How-to

 * Install [Node.js](http://nodejs.org/).
 * Install [Gulp.js](https://www.npmjs.com/package/gulp).
 * Run `./clean_all.sh && ./install_all.sh && build_all.sh`.
 * Go to particular component directory and follow their README.md

#Scripts description

 * *clean_all.sh* removes all previously installed dependencies and distributable versions
 * *install_all.sh* installs dependencies for each component - can take a while for the first time
 * *build_all.sh* builds each component using its Gulpfile

