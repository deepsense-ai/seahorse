deepsense-frontend
==================

DeepSense.io LAB application

#How-to

 * Install [Node.js](http://nodejs.org/).
 * Install all dependencies for `deepsense-components` by following its README.md instruction
 * Build all components in`deepsense-components` by following its README.md instruction
 * Run `npm start`.
 * Go to [http://localhost:3000/](http://localhost:3000/).

#Developer mode

Running `npm run dev` creates a background process that rebuilds whole project whenever any of the source files has been changed, site available at [localhost:3000](http://localhost:3000/) will be automatically reloaded.
If you run watcher for particular component as well it will allow you to automatically rebuild particular component and the whole app at the same time.
This makes possible to develop independent component and check how it behaves in the main application.
