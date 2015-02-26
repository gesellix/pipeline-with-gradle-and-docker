"use strict";

var disableNgAnimate = function () {
  angular.module('disableNgAnimate', []).run(function ($animate) {
    $animate.enabled(false);
  });
};

var e2eDefaults = function () {
  var defaultConfig = {};

  var defaultBaseUri = 'http://localhost:' + (process.env.HTTP_PORT || '3010');
  var contextPath = "/example/";
  defaultConfig.baseUrl = defaultBaseUri + contextPath;

  var defaultSpecsPattern = 'tests/*.js';
  var specsPattern = (process.env.e2eTest) ? 'tests/' + process.env.e2eTest + '.js' : defaultSpecsPattern;
  defaultConfig.specs = [specsPattern];

  defaultConfig.jasmineNodeOpts = {
    showColors: true,
    defaultTimeoutInterval: 30000
  };
  defaultConfig.onPrepare = function () {
    // provide our own util functions via window.browser
    // see https://github.com/angular/protractor/issues/441
    require('./protractorUtils');

    // Disable animations so e2e tests run more quickly
    browser.addMockModule('disableNgAnimate', disableNgAnimate);
  };

  console.log('using defaults: ' + JSON.stringify(defaultConfig, undefined, 2));
  return defaultConfig;
};
exports.e2eDefaults = e2eDefaults;
exports.disableNgAnimate = disableNgAnimate;