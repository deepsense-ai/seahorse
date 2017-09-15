/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 08.06.15.
 */

var path = require('path'),
  del = require('del');

var gulp = require('gulp'),
  gutil = require('gulp-util'),
  runSequence = require('run-sequence'),
  browserSync = require('browser-sync').create(),
  babel = require("gulp-babel"),
  concat = require('gulp-concat'),
  less = require('gulp-less'),
  templateCache = require('gulp-angular-templatecache');

var config = require('./config.json'),
  devMode = !!gutil.env.dev;

gulp.task('clean', function() {
  return del([config.dist + '*', config.temp + '*'], {force: true});
});

gulp.task('html', function() {
  return gulp.src(config.src + '**/*.html')
    .pipe(templateCache({
      module: 'deepsense-catalogue-panel'
    }))
    .pipe(gulp.dest(config.temp));
});

gulp.task('es6', function() {
  return gulp.src(config.src + '**/*.js')
    .pipe(gulp.dest(config.temp));
});

gulp.task('less', function () {
  return gulp.src(config.src + '**/*.less')
    .pipe(less({
      // paths: Array of paths to be used for @import directives
      paths: [ path.join(__dirname, 'less', 'includes') ]
    }))
    .pipe(gulp.dest(config.temp));
});

gulp.task('concat', function () {
  gulp.src(config.temp + '**/*.js')
    .pipe(concat(config.names.js))
    .pipe(babel())
    .pipe(gulp.dest(config.dist));

  return gulp.src(config.temp + '**/*.css')
    .pipe(concat(config.names.css))
    .pipe(gulp.dest(config.dist))
    .pipe(browserSync.stream());
});

gulp.task('serve', function() {

  browserSync.init({
      server: './'
  });

  gulp.watch(config.src + '**/*', ['build']);
  gulp.watch(config.test + '**/*', ['build']);
  gulp.watch([config.src + '**/*', config.test + '**/*']).on('change', function () {
    // a bit hacky
    setTimeout(function () {
        browserSync.reload();
    }, 450);
  });
});

gulp.task('build', function () {
  runSequence('clean', ['es6', 'less', 'html'], 'concat');

  return gulp.src('package.json')
    .pipe(gulp.dest(config.dist));
});

gulp.task('start', function () {
  if (devMode) {
    runSequence('build', 'serve');
  }
});

gulp.task('default', ['build']);