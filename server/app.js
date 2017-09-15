/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

let express = require('express');
let app = express();
let http = require('http').Server(app);
let config = require('./../config.json');

app.use(express.static(__dirname + './../' + config.files.build.path));

http.listen(config.env.frontend.port);
