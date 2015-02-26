"use strict";

var common_files = require('./karma.conf.common-files');

module.exports = function (config) {
  config.set({

    basePath: '../app',

    frameworks: ['chai-jquery', 'jquery-1.11.0', 'chai-sinon', 'jasmine', 'chai-as-promised'],
    files: common_files.concat([
      'app.js',
      'app-test.js',
      'modules/**/!(*-inttest).js',
      'modules/**/*.html',
      {
        pattern: '**/*.json',
        watched: true,
        included: false,
        served: true
      }
    ]),

    preprocessors: {
      '**/*.js': 'coverage',
      '**/*.html': ['ng-html2js']
    },

    reporters: ['dots'],
    port: 9876,
    logLevel: config.LOG_ERROR,

    autoWatch: true,
    browsers: ['PhantomJS'],

    captureTimeout: 60000,

    singleRun: false
  });
};
