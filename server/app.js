/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

let express = require('express');
let app = express();
let http = require('http').Server(app);
let config = require('./../package.json');

app.use(express.static(__dirname + './../build'));

http.listen(config.env.frontend.port);
