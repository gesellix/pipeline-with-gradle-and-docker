'use strict';

var path = require('path');

var express = require('express');
var app = express();
var appE2e = express();

var httpProxy = require('http-proxy');
var proxy = httpProxy.createProxyServer({});

var context = '/example';
var webappRoot = path.resolve(__dirname + '/../../build/grunt');
var fs = require('fs');

var getReq = function (path, callback) {
  app.get(path, callback);
  appE2e.get(path, callback);
};

appE2e.get('/example/app.js', function (req, res) {
  var appJs = fs.readFileSync(webappRoot + '/app.js', {encoding: 'utf-8'});
  appJs += "\nangular.module('example-app').constant('DEBOUNCE_TIME', 50);";
  res.writeHead(200, {"Content-Type": "text/javascript"});
  res.end(appJs);
});

getReq(context + '/*', function (req, res) {
  var file = webappRoot + req.path.substring(req.path.indexOf(context) + context.length);
  res.sendfile(file);
});

var listenPort = process.env.PORT || 2889;
var listenPortE2e = process.env.PORT || 3889;
app.listen(listenPort);
appE2e.listen(listenPortE2e);
console.log("Frontend running on port " + listenPort + " and E2E-modified server running on " + listenPortE2e);
