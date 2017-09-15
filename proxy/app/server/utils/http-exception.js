/**
 * Copyright (c) 2016, CodiLime Inc.
 */
function throwError(res, status, name, message, originalError) {
  var timestamp = new Date().getTime();
  var error = {
    error: name,
    status: status,
    message: message,
    timestamp: timestamp
  };
  var args = ["Error processing request", error];
  if(originalError) {
    args.push(originalError);
  }
  console.error.apply(console, args);
  res.status(status).send(error);
}

module.exports = {
  throw: throwError
};
