'use strict';

module.exports = function (grunt) {

  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-autoprefixer');
  grunt.loadNpmTasks('grunt-html2js');
  grunt.loadNpmTasks('grunt-ng-annotate');
  grunt.loadNpmTasks('grunt-karma');
  grunt.loadNpmTasks('grunt-jasmine-node');
  grunt.loadNpmTasks('grunt-processhtml');
  grunt.loadNpmTasks('grunt-regex-check');
  grunt.loadNpmTasks('grunt-exec');
  grunt.loadNpmTasks('grunt-nodemon');
  grunt.loadNpmTasks('grunt-concurrent');
  grunt.loadNpmTasks('grunt-remove');
  grunt.loadNpmTasks('grunt-available-tasks');

  grunt.initConfig({

                     settings: {
                       targetDir: 'build/grunt',
                       workingDir: 'build/grunt-temp',
                       serverPort: 2888
                     },

                     copy: {
                       'scripts': {
                         cwd: 'src/app',
                         src: [
                           '**/*.js',
                           '!**/*-test.js',
                           '!lib/**/*',
                           'modules/errorhandler/errorhandler-fallback.js'
                         ],
                         dest: '<%=settings.workingDir%>/scripts',
                         expand: true
                       },
                       stylesheets: {
                         cwd: 'src/app',
                         src: [
                           '**/*.less',
                           '**/*.{png,jpg,jpeg,gif,svg}',
                           '!lib/**/*'
                         ],
                         dest: '<%=settings.workingDir%>/stylesheets',
                         expand: true
                       },
                       assets: {
                         cwd: 'src/app',
                         src: [
                           '*.html',
                           '**/*.{png,jpg,jpeg,gif,ico,svg}',
                           'modules/errorhandler/errorhandler-fallback.js',
                           'lib/production*/**/*',
                           '!**/*.less'
                         ],
                         dest: '<%=settings.workingDir%>/assets',
                         expand: true
                       },
                       'processed-assets': {
                         cwd: '<%=settings.workingDir%>/assets',
                         src: '**/*',
                         dest: '<%=settings.targetDir%>',
                         expand: true
                       },
                       'processed-scripts': {
                         cwd: '<%=settings.workingDir%>/scripts',
                         src: [
                           'app.js',
                           'app.map'
                         ],
                         dest: '<%=settings.targetDir%>',
                         expand: true
                       },
                       'processed-stylesheets': {
                         cwd: '<%=settings.workingDir%>/stylesheets',
                         src: [
                           '**/*.css',
                           '**/*.{png,jpg,jpeg,gif}'
                         ],
                         dest: '<%=settings.targetDir%>',
                         expand: true
                       }
                     },

                     remove: {
                       options: {
                         trace: true
                       },
                       build: {
                         dirList: [
                           '<%=settings.targetDir%>',
                           '<%=settings.workingDir%>'
                         ]
                       },
                       scripts: {
                         dirList: [
                           '<%=settings.workingDir%>/scripts'
                         ]
                       },
                       stylesheets: {
                         dirList: [
                           '<%=settings.workingDir%>/stylesheets'
                         ]
                       }
                     },

                     less: {
                       options: {
                         compress: true,
                         cleancss: true,
                         report: 'min',
                         ieCompat: true
                       },
                       stylesheets: {
                         files: {
                           '<%=settings.workingDir%>/stylesheets/main.css': '<%=settings.workingDir%>/stylesheets/main.less'
                         }
                       }
                     },

                     autoprefixer: {
                       options: {
                         browsers: ['last 3 version', 'ie 8', 'ie 9', 'ie 10', 'ie 11']
                       },
                       build: {
                         expand: true,
                         cwd: '<%=settings.workingDir%>/stylesheets',
                         src: ['main.css'],
                         dest: '<%=settings.workingDir%>/stylesheets'
                       }
                     },

                     html2js: {
                       options: {
                         base: 'src/app',
                         quoteChar: '\'',
                         module: 'templates-main'
                       },
                       build: {
                         src: [
                           'src/app/modules/**/*.html',
                           'src/app/template/**/*.html'
                         ],
                         dest: '<%=settings.workingDir%>/scripts/templates.js'
                       }
                     },

                     concat: {
                       build: {
                         src: [
                           '<%=settings.workingDir%>/scripts/app.js',
                           '<%=settings.workingDir%>/scripts/templates.js',
                           '<%=settings.workingDir%>/scripts/**/*.js'
                         ],
                         dest: '<%=settings.workingDir%>/scripts/app.js'
                       }
                     },

                     ngAnnotate: {
                       build: {
                         src: '<%=settings.workingDir%>/scripts/app.js',
                         dest: '<%=settings.workingDir%>/scripts/app.js'
                       }
                     },

                     uglify: {
                       options: {
                         sourceMap: true,
                         sourceMapName: '<%=settings.workingDir%>/scripts/app.map',
                         sourceMapPrefix: 2,
                         report: 'min',
                         beautify: {
                           beautify: true,
                           indent_level: 1,
                           bracketize: true
                         },
                         compress: {
                           sequences: false,
                           side_effects: true,
                           global_defs: {
                             DEBUG: false
                           }
                         }
                       },
                       scripts: {
                         src: '<%=settings.workingDir%>/scripts/app.js',
                         dest: '<%=settings.workingDir%>/scripts/app.js'
                       }
                     },

                     watch: {
                       options: {
                         interval: 500,
                         debounceDelay: 500,
                         spawn: true
                       },
                       stylesheets: {
                         files: [
                           'src/app/**/*.{less,png,jpg,jpeg,gif,ico,svg}'
                         ],
                         tasks: ['stylesheets']
                       },
                       scripts: {
                         files: [
                           'src/app/**/*.{js,coffee,html}',
                           '!src/app/**/*-test.{js,coffee}',
                           '!src/app/lib/**'
                         ],
                         tasks: ['scripts', 'sanitize']
                       },
                       tests: {
                         files: [
                           'src/app/**/*-test.{js,coffee}'
                         ],
                         tasks: ['sanitize']
                       },
                       assets: {
                         files: [
                           'src/app/*.html',
                           'src/app/**/*.{png,jpg,jpeg,gif,ico,svg}',
                           'src/app/lib/production*/**'
                         ],
                         tasks: ['assets']
                       },
                       livereload: {
                         files: [
                           '<%= settings.targetDir %>/**/*.css'
                         ],
                         options: {
                           livereload: true,
                           interval: 50,
                           debounceDelay: 50
                         }
                       },
                       windows: {
                         files: [
                           'src/app/*.gibtsnicht'
                         ],
                         tasks: ['sanitize']
                       }
                     },

                     karma: {
                       'e2e': {
                         configFile: 'src/e2e/karma-e2e.conf.js',
                         singleRun: true,
                         proxies: {
                           '/': 'http://localhost:<%= settings.serverPort %>/'
                         }
                       }
                     },

                     jasmine_node: {
                       projectRoot: "e2e",
                       requirejs: false,
                       forceExit: true,
                       jUnit: true,
                       coffee: false,
                       verbose: true
                     },

                     processhtml: {
                       options: {
                         process: true,
                         data: {}
                       },
                       assets: {
                         files: {
                           '<%=settings.workingDir%>/assets/index.html': ['<%=settings.workingDir%>/assets/index.html']
                         }
                       },
                       develop: {
                         files: {
                           '<%=settings.workingDir%>/assets/index.html': ['<%=settings.workingDir%>/assets/index.html']
                         }
                       }
                     },

                     jshint: {
                       beforeconcat: {
                         options: {
                           jshintrc: '.jshintrc'
                         },
                         src: [
                           'src/app/**/*.js',
                           '!src/app/lib/**/*.js'
                         ]
                       }
                     },

                     "regex-check": {
                       files: [
                         'src/app/app-test.js',
                         'src/app/modules/**/*-test.js',
                         'src/app/modules/**/*-inttest.js',
                         'src/app/modules/**/*-test.coffee',
                         'src/e2e/tests/**/*.js'
                       ],
                       options: {
                         pattern: /(iit|ddescribe)/g
                       }
                     },

                     "dependency-analyzer": {
                       example: {
                         src: [
                           'src/app/modules/**/*.js',
                           '!src/app/modules/**/*-test.js'
                         ]
                       }
                     },

                     exec: {
                       package_dependencies_pngs: {
                         cmd: 'dot -Tpng target/packageDependencies.dot -o target/packageDependencies.png'
                       },
                       service_dependencies_pngs: {
                         cmd: 'dot -Tpng target/serviceDependencies.dot -o target/serviceDependencies.png'
                       }
                     },

                     imagemin: {
                       stylesheets: {
                         files: [
                           {
                             expand: true,
                             cwd: '<%=settings.workingDir%>/stylesheets',
                             src: [
                               '**/*.{png,jpg,jpeg,gif}'
                             ],
                             dest: '<%=settings.workingDir%>/stylesheets'
                           }
                         ]
                       }
                     },

                     availabletasks: {
                       options: {
                         filter: 'include',
                         tasks: [
                           'build',
                           'dependencies',
                           'develop',
                           'e2e-test',
                           'help',
                           'jshint',
                           'regex-check',
                           'release',
                           'clean',
                           'sanitize',
                           'server',
                           'server-e2e',
                           'server-e2e-test',
                           'test',
                           'test:source',
                           'verify'
                         ],

                         groups: {
                           '1) Primary': [
                             'develop'
                           ],
                           '2) Build': [
                             'clean',
                             'build',
                             'verify',
                             'release'
                           ],
                           '3) Test': [
                             'server-e2e-test',
                             'e2e-test',
                             'test',
                             'test:source'
                           ],
                           '4) Server': [
                             'server',
                             'server-e2e'
                           ],
                           '5) Analyze': [
                             'dependencies',
                             'dependency-analyzer',
                             'jshint',
                             'regex-check',
                             'sanitize'
                           ]
                         }
                       }
                     }
                   });

  grunt.registerTask('assets', 'Processes the static assets', function () {
    var tasks = [];
    tasks.push(
        'copy:assets',
        'processhtml:assets'
    );
    if (grunt.option('develop')) {
      tasks.push('processhtml:develop');
    }
    tasks.push('copy:processed-assets');
    grunt.task.run(tasks);
  });

  grunt.registerTask('stylesheets', 'Builds the final CSS file', function () {
    var tasks = [
      'remove:stylesheets',
      'copy:stylesheets'
    ];

    tasks.push(
        'less:stylesheets',
        'autoprefixer',
        'copy:processed-stylesheets'
    );

    grunt.task.run(tasks);
  });

  grunt.registerTask('scripts', 'Builds the final JavaScript file', function () {
    var tasks = [];
    tasks.push(
        'remove:scripts',
        'copy:scripts',
        'html2js',
        'concat'
    );
    if (!grunt.option('develop')) {
      tasks.push(
          'ngAnnotate',
          'uglify:scripts'
      );
    }
    tasks.push('copy:processed-scripts');
    grunt.task.run(tasks);
  });

  grunt.registerTask('build', 'Builds the entire application', [
    'remove:build',
    'assets',
    'stylesheets',
    'scripts'
  ]);

  grunt.registerTask('sanitize', ['regex-check', 'jshint']);

  grunt.registerTask('test', 'Runs the unit tests against the release version. Use \n' +
                             '                       "test:source" to run the tests against the development sources or\n' +
                             '                       "test:source:coverage" to also enable code coverage.', function (source, coverage) {
    var reporters = [];
    reporters.push(grunt.option('teamcity') ? 'teamcity' : 'dots');

    if (source === 'source') {
      grunt.config('karma.unit', {
        configFile: 'src/config/karma.conf.js',
        reporters: reporters,
        autoWatch: true,
        singleRun: false,
        coverageReporter: {
          type: 'html',
          dir: '../' + grunt.config('settings.workingDir') + '/coverage'
        }
      });
      if (coverage === 'coverage') {
        reporters.push('coverage');
      }
    }
    else {
      grunt.config('karma.unit', {
        configFile: 'src/config/karma.conf.release.js',
        reporters: reporters,
        autoWatch: false,
        singleRun: true
      });
    }

    grunt.task.run(['karma:unit']);
  });

  grunt.registerTask('verify', [
    'sanitize',
    'test'
//    'e2e-test'
  ]);

  grunt.registerTask('server', 'Starts the Frontend Server on port 2889', function () {
    require('./src/server/frontend-server.js');
  });

  grunt.registerTask('develop', 'You best friend during development => Alias for "build", "server", "watch" task.', function () {
    grunt.option('develop', true);
    grunt.task.run(['build', 'server', 'watch']);
  });

  grunt.registerTask('e2e-frontend', 'Start frontend for e2e-Tests without Livereload", "server", "watch" task.', function () {
    grunt.task.run(['build', 'server', 'watch:windows']);
  });

  grunt.registerTask('release', 'Alias for "build", "verify" task.', function () {
    grunt.option('teamcity', true);
    grunt.task.run(['build', 'verify']);
  });

  grunt.registerTask('default', ['release']);

  grunt.registerTask('clean', 'Deletes all files created by a previous build', ['remove']);

};