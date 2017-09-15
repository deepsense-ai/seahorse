/**
 * Copyright (c) 2015, CodiLime Inc.
 */
/*global console*/
'use strict';

var gulp = require('gulp'),
    gutil = require('gulp-util'),
    runSequence = require('run-sequence'),
    browserSync = require('browser-sync'),
    browserify = require('browserify'),
    browserifyShim = require('browserify-shim'),
    browserifyAnnotate = require('browserify-ngannotate'),
    to5Browserify = require('6to5ify'),
    source = require('vinyl-source-stream'),
    del = require('del'),
    less = require('gulp-less'),
    jshint = require('gulp-jshint');
require('jshint-stylish');

var config = require('./package.json'),
    client = config.files.client,
    build = config.files.build;


gulp.task('jshint', function () {
  return gulp.src([client.path + client.js, './gulpfile.js'])
    .pipe(jshint())
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(jshint.reporter('fail'));
});


gulp.task('clean', function () {
  return del([build.path]);
});


gulp.task('html', function () {
  return gulp.src([client.path + client.html])
    .pipe(gulp.dest(build.path));
});

gulp.task('less', function () {
  return gulp.src(client.path + client.less)
    .pipe(less())
    .pipe(gulp.dest(build.path));
});

gulp.task('bootstrap-css', function () {
  return gulp.src(config.browser.bootstrap)
    .pipe(gulp.dest(build.path + build.css));
});


gulp.task('browserify', function () {
  return browserify({
      entries: [config.main],
      debug: true
    })
    .transform(to5Browserify)
    .transform(browserifyAnnotate, {
        add: true,
        // jshint -W106
        single_quotes: true
    })
    .transform(browserifyShim)
    .bundle()
    .pipe(source(build.js))
    .pipe(gulp.dest(build.path));
});


gulp.task('browser-sync', function () {
  return browserSync({
    server: {
      baseDir: build.path
    }
  });
});

gulp.task('build', function (callback) {
  runSequence('clean', 'html', 'bootstrap-css', 'less', 'jshint', 'browserify', callback);
});

gulp.task('start', function (callback) {
  runSequence('build', 'browser-sync', callback);
});


gulp.task('dev', ['start'], function () {
  gulp.watch(client.path + client.less, ['less', browserSync.reload]);
  gulp.watch(client.path + client.html, ['html', browserSync.reload]);
  gulp.watch(client.path + client.js, ['jshint', 'browserify', browserSync.reload]);
});


gulp.task('default', function () {
  gutil.log(gutil.colors.red('Please select task to run.'));
  console.log('\nstart - builds and starts application');
  console.log('dev   - starts application & watches for sources change\n');
});
