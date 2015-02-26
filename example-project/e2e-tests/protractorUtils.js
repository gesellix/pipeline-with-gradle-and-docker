"use strict";

var fs = require('fs'),
  path = require('path');

var reportDir = path.resolve(__dirname + '/report/'),
  screenshotsDir = path.resolve(reportDir + '/screenshots/');

if (!fs.existsSync(reportDir)) {
  fs.mkdirSync(reportDir);
}

console.log('screenshotsDir = [' + screenshotsDir + ']');
if (!fs.existsSync(screenshotsDir)) {
  fs.mkdirSync(screenshotsDir);
}

var errorCallback = function (err) {
  console.log(err);
};

var getDateStr = function () {
  var d = (new Date() + '').replace(new RegExp(':', 'g'), '-').split(' ');
  // "2013-Sep-03-21:58:03"
  return [d[3], d[1], d[2], d[4]].join('-');
};

var timestampToDate = function (unix_timestamp) {
  var date = new Date(unix_timestamp);
  // hours part from the timestamp
  var hours = date.getHours();
  // minutes part from the timestamp
  var minutes = date.getMinutes();
  // seconds part from the timestamp
  var seconds = date.getSeconds();

  var timeValues = [hours, minutes, seconds];
  timeValues.forEach(function (val) {
    if (val.length < 2) {
      // padding
      val = '0' + val;
    }
  });
  // will display time in 10:30:23 format
  return hours + ':' + minutes + ':' + seconds;
};

var writeScreenshotToFile = function (pngFileName) {
  browser.takeScreenshot().then(function (png) {
    console.log('Writing file ' + pngFileName);
    fs.writeFileSync(pngFileName, png, {encoding: 'base64'}, function (err) {
      console.log(err);
    });
  }, errorCallback);
};

var writeLogentriesToConsole = function (logsEntries) {
  var len = logsEntries.length;

  for (var i = 0; i < len; ++i) {
    var logEntry = logsEntries[i];
    var msg = "Browser Console: " + timestampToDate(logEntry.timestamp) + ' ' + logEntry.type + ' ' + logEntry.message;
    console.log(msg);
  }
};

var writeLogs = function () {
  var logs = browser.driver.manage().logs(),
    logType = 'browser';

  logs.getAvailableLogTypes().then(function (logTypes) {
    if (logTypes.indexOf(logType) > -1) {
      browser.driver.manage().logs().get(logType).then(function (logsEntries) {
        writeLogentriesToConsole(logsEntries);
      }, errorCallback);
    }
  });
};

var expectNoBeforeunload = function () {
  expect(browser.driver.executeScript("var result =  (window.onbeforeunload != undefined) ? window.onbeforeunload():undefined; window.onbeforeunload =  function(){}; return result;")).toBeFalsy();
};

// taken from http://eitanp461.blogspot.de/2014/01/advanced-protractor-features.html
var dumpConsoleLog = function () {

  var passed = jasmine.getEnv().currentSpec.results().passed();

  // normalize file names
  var specName = jasmine.getEnv().currentSpec.description.replace(new RegExp(' ', 'g'), '-'),
    baseFileName = specName + '-' + getDateStr();

  if (!passed) {
    var pngFileName = path.resolve(screenshotsDir + '/' + baseFileName + '.png');
    writeScreenshotToFile(pngFileName);
  }
  writeLogs();
};

var globalSetup = function (setWindowSize) {
  beforeEach(function () {
    browser.get(browser.baseUrl);
    if (setWindowSize) {
      browser.driver.manage().window().setSize(1440, 900);
    }

    // disable transitions to avoid flaky tests
    browser.executeScript("$('<style id=\"test\" type=\"text/css\">" +
                          "* {" +
                          "-webkit-transition: none !important;" +
                          "-moz-transition: none !important;" +
                          "-o-transition: none !important;" +
                          "transition: none !important;" +
                          "}" +
                          "</style>').appendTo(document.head);");
  });

  afterEach(function () {
    browser.dumpConsoleLog();
    expectNoBeforeunload();
  });
};

browser.writeLogs = writeLogs;
browser.dumpConsoleLog = dumpConsoleLog;

browser.globalSetup = globalSetup;
