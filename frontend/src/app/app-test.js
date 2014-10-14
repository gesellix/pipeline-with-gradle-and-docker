'use strict';

describe("app", function () {
  var $injector;
  var $location;
  var $rootScope;
  var $locationProvider;

  beforeEach(function () {
    module("example-app");

    module(function (_$locationProvider_) {
      $locationProvider = _$locationProvider_;
    });

    inject(function (_$injector_, _$location_, _$rootScope_) {
      $injector = _$injector_;
      $location = _$location_;
      $rootScope = _$rootScope_;
    });
  });

  it("should load application", function () {
    expect(window.exampleAngularApp).to.be.equal("RUNNING");
  });

  it('should enable html5mode', function () {
    expect($locationProvider.html5Mode().enabled).to.be.true;
  });

  it('should redirect not configured paths to "/"', function () {
    inject(function ($route) {
      expect($route.routes["null"].redirectTo).to.be.equal("/");
    });
  });

  it('should redirect "/index.html" to "/"', function () {
    inject(function ($route) {
      expect($route.routes["/index.html"].redirectTo).to.be.equal("/");
    });
  });

  it('should configure "/" to use exampleController', function () {
    inject(function ($route) {
      var actualConfig = _.pick($route.routes["/"], 'reloadOnSearch', 'templateUrl', 'controller');
      expect(actualConfig).to.deep.equal({
                                           reloadOnSearch: false,
                                           templateUrl: "modules/example/example.html",
                                           controller: "exampleController"
                                         });
    });
  });
});