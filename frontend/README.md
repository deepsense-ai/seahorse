deepsense-frontend
==================

# Setup

Prerequisites:
- Node
- NPM

In order to setup frontend you need to download all needed npm dependencies

1. Inside deepsense-frontend run command below:
  `npm install`

2. To run frontend on debug server run:
  `npm start`(which is also an equivalent to`npm run serve`)

3. To build frontend for production run:
  `npm run dist`
4. Built app can be found in `dist` catalogue

# UT

There is no dedicated folder for unit tests. Unit tests are located inside feature folders next to tested components.
Unit tests have .spec.js suffix in their name.
To run tests & linting of the code run command (inside deepsense-frontend)
  `npm test`

# List all licences

```
npm install -g license-checker
license-checker --production --csv
```
