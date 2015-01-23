'use strict';

var domain = require('domain');
var d = domain.create();

var frontendPort = '8080';
var backendPort = '8090';

d.on('error', function (e) {
  console.log('error: ', e.message);
});

d.run(function () {
  var express = require('express');
  var app = express();

  var httpProxy = require('http-proxy');
  var proxy = httpProxy.createProxyServer({});

  app.use(express.cookieParser());

  app.all(/(\/[^\/]+)?\/(example-backend)\/(.*)/, function (req, res) {
    proxy.web(req, res, {
      target: 'http://localhost:' + backendPort
    });
  });

  app.all(/(\/[^\/]+)?\/(example)\/(.*)/, function (req, res) {
    proxy.web(req, res, {
      target: 'http://localhost:' + frontendPort
    });
  });

  var listenPort = 3010;
  app.listen(listenPort);
  console.log("e2e test proxy server running on port " + listenPort);
  console.log("frontend is expected on port " + frontendPort);
  console.log("backend is expected on port " + backendPort);
});
