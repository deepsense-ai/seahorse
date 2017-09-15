/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var gulp = require('gulp'),
    gutil = require('gulp-util'),
    runSequence = require('run-sequence'),
    nodemon = require('gulp-nodemon'),
    browserSync = require('browser-sync'),
    browserify = require('browserify'),
    browserifyShim = require('browserify-shim'),
    browserifyAnnotate = require('browserify-ngannotate'),
    buffer = require('vinyl-buffer'),
    babelifygul = require('babelify'),
    uglify = require('gulp-uglify'),
    source = require('vinyl-source-stream'),
    clean = require('gulp-clean'),
    concat = require('gulp-concat'),
    size = require('gulp-size'),
    less = require('gulp-less'),
    minifyCSS = require('gulp-minify-css'),
    jshint = require('gulp-jshint'),
    exit = require('gulp-exit');
require('jshint-stylish');

var config = require('./package.json'),
    client = config.files.client,
    server = config.files.server,
    build = config.files.build,
    libs = config.files.libs,
    devMode = !!gutil.env.dev,
    CIMode = !!gutil.env.ci;
client.path = __dirname + '/' + client.path;

var BROWSER_SYNC_RELOAD_DELAY = 2000;

gulp.task('clean', function() {
  return gulp.src([build.path], {read: false})
    .pipe(clean({force: true}));
});

gulp.task('nodemon', function (callback) {
  var called = false,
      config = {
        execMap: {
          'js': 'node --harmony'
        },
        script: server.path + server.app,
        verbose: true,
        watch: [server.path]
      };


  // TODO: remove after full login implementation
  if (gutil.env.host && gutil.env.token) {
    var args = ['--host', gutil.env.host, '--token', gutil.env.token],
        apiConfig = require('./server/api/apiConfig.js'),
        resource;
    if (gutil.env.deployURL) {
      args.push('--deployURL');
      args.push(gutil.env.deployURL);
    }
    for (resource in apiConfig.resources) {
      if (gutil.env['host-' + resource]) {
        args.push('--host-' + resource);
        args.push(gutil.env['host-' + resource]);
      }
    }
    config.args = args;
  }


  return nodemon(config).on('start', function () {
      if (!called) {
        callback();
      }
      called = true;
    })
    .on('restart', function () {
      setTimeout(function () {
        browserSync.reload();
      }, BROWSER_SYNC_RELOAD_DELAY);
    });
});

gulp.task('browser-sync', ['nodemon'], function () {
  return browserSync({
    open: !devMode,
    proxy: config.env.dev.host + ':' + config.env.dev.port
  });
});


gulp.task('html', function () {
  return gulp.src([client.path + client.html])
    .pipe(gulp.dest(build.path));
});

gulp.task('images', function () {
  return gulp.src([client.path + client.images])
    .pipe(gulp.dest(build.path + build.images));
});

gulp.task('fonts', function () {
  return gulp.src([client.path + client.fonts])
    .pipe(gulp.dest(build.path + build.fonts));
});

gulp.task('less', function () {
  return gulp.src(client.path + client.less)
    .pipe(less())
    .pipe(!devMode ? minifyCSS() : gutil.noop())
    .pipe(size({
      title: 'less',
      showFiles: true
    }))
    .pipe(gulp.dest(build.path + build.css))
    .pipe(browserSync.reload({stream: true}));
});

gulp.task('libs:css', function () {
  return gulp.src(libs[devMode ? 'dev': 'prod'].css)
    .pipe(concat(build.bundle.css))
    .pipe(size({
      title: 'libs:css',
      showFiles: true
    }))
    .pipe(gulp.dest(build.path + build.css));
});

gulp.task('libs:js', function () {
  return gulp.src(libs[devMode ? 'dev': 'prod'].js)
    .pipe(concat(build.bundle.js))
    .pipe(size({
      title: 'libs:js',
      showFiles: true
    }))
    .pipe(gulp.dest(build.path + build.js));
});

gulp.task('jshint', function () {
  return gulp.src([
      client.path + client.js,
      server.path + server.js,
      config.files.tests.e2e,
      './karma.conf.js',
      './protractor.conf.js',
      './gulpfile.js'
    ])
    .pipe(jshint())
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(CIMode ? jshint.reporter('fail') : gutil.noop())
    .pipe(CIMode ? exit() : gutil.noop());
});

gulp.task('browserify', function () {
  return browserify({
      entries: [client.path + client.app],
      debug: devMode
    })
    .transform(babelifygul)
    .transform(browserifyAnnotate, {
        add: true,
        // jshint -W106
        single_quotes: true
    })
    .transform(browserifyShim)
    .bundle()
    .pipe(source(build.js + build.bundle.app))
    .pipe(buffer())
    .pipe(!devMode ? uglify() : gutil.noop())
    .pipe(size({
      title: 'browserify',
      showFiles: true
    }))
    .pipe(gulp.dest(build.path));
});


gulp.task('build', function (callback) {
  runSequence(
    'clean',
    ['fonts', 'images', 'html', 'less', 'libs:css', 'libs:js', 'jshint', 'browserify'],
    callback
  );
});

gulp.task('start', function (callback) {
  runSequence('build', 'browser-sync', callback);
  if (devMode) {
    gulp.watch(client.path + client.html, ['html', browserSync.reload]);
    gulp.watch(client.path + client.images, ['images', browserSync.reload]);
    gulp.watch(client.path + client.lessSources, ['less']);
    gulp.watch(
      [client.path + client.js, '|', '!' + __dirname + '/' + config.files.tests.client],
      ['jshint', 'browserify', browserSync.reload]
    );
  }
});

gulp.task('watch', function (callback) {
  devMode = true;
  runSequence('browser-sync', callback);
  gulp.watch(client.path + client.html, ['html', browserSync.reload]);
  gulp.watch(client.path + client.lessSources, ['less']);
  gulp.watch(
    [client.path + client.js, '|', '!' + __dirname + '/' + config.files.tests.client],
    ['jshint', 'browserify', browserSync.reload]
  );
});

gulp.task('default', function () {
  gutil.log(gutil.colors.red('Please select task to run.'));
  gutil.log('');
  gutil.log(gutil.colors.blue('start'), gutil.colors.gray('        builds and starts application'));
  gutil.log(gutil.colors.blue('start --dev'), gutil.colors.gray('  builds and watches for source changes'));
  gutil.log('');
});
