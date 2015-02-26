"use strict";

angular.module('example-app', [
  'ngRoute',
  'ngAnimate',
  'templates-main',
  'exampleController'
])

    .constant('clientId', Date.now())

    .config(function ($routeProvider, $locationProvider) {
              $locationProvider.html5Mode().enabled = true;
              $routeProvider
                  .when('/', {
                          templateUrl: 'modules/example/example.html',
                          controller: 'exampleController',
                          reloadOnSearch: false
                        })
                  .when('/index.html', {
                          redirectTo: '/'
                        })
                  .otherwise({
                               redirectTo: '/'
                             });
            })

    .run(function () {
           window.exampleAngularApp = "RUNNING";
         });

angular.module('templates-main', []); // <-- don't change. grunt replaces this module at runtime.
