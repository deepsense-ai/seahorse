/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var gulp = require('gulp'),
    gutil = require('gulp-util'),
    runSequence = require('run-sequence'),
    browserSync = require('browser-sync'),
    browserify = require('browserify'),
    browserifyShim = require('browserify-shim'),
    browserifyAnnotate = require('browserify-ngannotate'),
    buffer = require('vinyl-buffer'),
    to5Browserify = require('6to5ify'),
    uglify = require('gulp-uglify'),
    source = require('vinyl-source-stream'),
    del = require('del'),
    concat = require('gulp-concat'),
    size = require('gulp-size'),
    less = require('gulp-less'),
    minifyCSS = require('gulp-minify-css'),
    jshint = require('gulp-jshint');
require('jshint-stylish');

var config = require('./package.json'),
    client = config.files.client,
    build = config.files.build,
    libs = config.files.libs,
    devMode = !!gutil.env.dev;


gulp.task('clean', function () {
  del([build.path]);
});

gulp.task('browser-sync', function () {
  return browserSync({
    server: {
      baseDir: build.path
    },
    open: !devMode
  });
});


gulp.task('html', function () {
  return gulp.src([client.path + client.html])
    .pipe(gulp.dest(build.path));
});

gulp.task('less', function () {
  return gulp.src(client.path + client.less)
    .pipe(less())
    .pipe(!devMode ? minifyCSS() : gutil.noop())
    .pipe(size({
      title: 'less',
      showFiles: true
    }))
    .pipe(gulp.dest(build.path + build.css));
});

gulp.task('libs:css', function () {
  return gulp.src(libs.css)
    .pipe(concat(build.bundle.css))
    .pipe(size({
      title: 'libs:css',
      showFiles: true
    }))
    .pipe(gulp.dest(build.path + build.css));
});

gulp.task('libs:js', function () {
  return gulp.src(libs.js)
    .pipe(concat(build.bundle.js))
    .pipe(size({
      title: 'libs:js',
      showFiles: true
    }))
    .pipe(gulp.dest(build.path + build.js));
});

gulp.task('jshint', function () {
  return gulp.src([client.path + client.js, './gulpfile.js'])
    .pipe(jshint())
    .pipe(jshint.reporter('jshint-stylish'))
    .pipe(jshint.reporter('fail'));
});

gulp.task('browserify', function () {
  return browserify({
      entries: [client.path + client.app],
      debug: devMode
    })
    .transform(to5Browserify)
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
  runSequence('clean', ['html', 'less', 'libs:css', 'libs:js', 'jshint', 'browserify'], callback);
});

gulp.task('start', function (callback) {
  runSequence('build', 'browser-sync', callback);
  if (devMode) {
    gulp.watch(client.path + client.html, ['html', browserSync.reload]);
    gulp.watch(client.path + client.less, ['less', browserSync.reload]);
    gulp.watch(client.path + client.js, ['jshint', 'browserify', browserSync.reload]);
  }
});


gulp.task('default', function () {
  gutil.log(gutil.colors.red('Please select task to run.'));
  gutil.log('');
  gutil.log(gutil.colors.blue('start'), gutil.colors.gray('        builds and starts application'));
  gutil.log(gutil.colors.blue('start --dev'), gutil.colors.gray('  builds and watches for source changes'));
  gutil.log('');
});
