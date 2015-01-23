"use strict";
var defaultConfig = require('./protractor-conf-defaults');
var localConfig = defaultConfig.e2eDefaults();

localConfig.seleniumAddress = 'http://localhost:4449/wd/hub';
localConfig.capabilities = {
  'phantomjs.binary.path': 'node_modules/.bin/phantomjs',
  'browserName': 'phantomjs',
  'count': 1,
  'sharedTestFiles': false,
  'maxInstances': 1
};
localConfig.onPrepare = function () {
  // provide our own util functions via window.browser
  // see https://github.com/angular/protractor/issues/441
  require('./protractorUtils');
  require('jasmine-reporters');
  jasmine.getEnv().addReporter(new jasmine.TeamcityReporter());

  // Disable animations so e2e tests run more quickly
  browser.addMockModule('disableNgAnimate', defaultConfig.disableNgAnimate);
};
exports.config = localConfig;
