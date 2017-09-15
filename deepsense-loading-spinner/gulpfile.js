/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

var SRC_DIR = './src';
var DIST_DIR = './dist';
var TMP_DIR = './.tmp';
var OUTPUT_NAME = 'deepsense-loading-spinner';

var gulp = require('gulp');
var clean = require('gulp-clean');
var gulpSequence = require('gulp-sequence');
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var concatCss = require('gulp-concat-css');
var less = require('gulp-less');
var minifyCSS = require('gulp-minify-css');
var autoprefix = require('gulp-autoprefixer');
var size = require('gulp-size');
var babel = require('gulp-babel');
var plumber = require('gulp-plumber');
var ngHtml2Js = require("gulp-ng-html2js");
var minifyHtml = require("gulp-minify-html");


gulp.task('scripts', function() {
    return gulp.src(SRC_DIR + '/**/*.js').
        pipe(plumber()).
        pipe(size({
            title: 'scripts',
            showFiles: true
        })).
        pipe(babel()).
        pipe(concat(OUTPUT_NAME + '.js')).
        pipe(uglify()).
        pipe(gulp.dest(TMP_DIR));
});

gulp.task('styles', function() {
    return gulp.src(SRC_DIR + '/**/*.less').
        pipe(plumber()).
        pipe(less({
            paths: [__dirname, SRC_DIR]
        })).
        pipe(size({
            title: 'scripts',
            showFiles: true
        })).
        pipe(concatCss(OUTPUT_NAME + '.css')).
        pipe(autoprefix()).
        pipe(minifyCSS()).
        pipe(gulp.dest(DIST_DIR));
});

gulp.task('partials', function() {
    return gulp.src(SRC_DIR + '/**/*.tpl.html').
        pipe(minifyHtml({
            empty: true,
            spare: true,
            quotes: true
        })).
        pipe(ngHtml2Js({
            moduleName: 'deepsense.spinner-partials'
        })).
        pipe(concat('deepsense-partials.min.js')).
        pipe(uglify()).
        pipe(gulp.dest(TMP_DIR));
});

gulp.task('concat:js', function() {
   return gulp.src(TMP_DIR + '/**/*.js').
       pipe(concat(OUTPUT_NAME + '.js')).
       pipe(uglify()).
       pipe(gulp.dest(DIST_DIR));
});

gulp.task('clean', function() {
    return gulp.src([DIST_DIR], { read: false })
        .pipe(clean({ force: true }));
});

gulp.task('default', gulpSequence('clean', ['scripts', 'styles', 'partials'], 'concat:js'));