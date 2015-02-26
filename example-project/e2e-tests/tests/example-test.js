"use strict";
describe('example app', function () {

  browser.globalSetup();

  it('should be available', function () {
    expect(element(by.id('example')).isPresent()).toBe(true);
  });

  it('should show a welcome message', function () {
    var welcomeTextElement = element(by.binding('welcomeText'));
    expect(welcomeTextElement.getText()).toEqual("hello world");
  });
});
