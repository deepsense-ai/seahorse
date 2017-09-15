/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 08.06.15.
 */

var path = require('path');
var del = require('del');
var gulp = require('gulp');
var uglify = require('gulp-uglify');
var runSequence = require('run-sequence');
var babel = require("gulp-babel");
var concat = require('gulp-concat');
var less = require('gulp-less');
var templateCache = require('gulp-angular-templatecache');
var minifyCss = require('gulp-minify-css');
var connect = require('gulp-connect');

var config = require('./config.json');

gulp.task('clean', function () {
    return del([config.dist + '*', config.temp + '*'], {force: true});
});

gulp.task('clean-temp', function () {
    return del([config.temp], {force: true});
});

gulp.task('html', function () {
    return gulp.src(config.src + '**/*.html')
        .pipe(templateCache({
            module: 'deepsense-catalogue-panel'
        }))
        .pipe(gulp.dest(config.temp))
        .pipe(connect.reload());
});

gulp.task('es6', function () {
    return gulp.src(config.src + '**/*.js')
        .pipe(gulp.dest(config.temp))
        .pipe(connect.reload());
});

gulp.task('less', function () {
    return gulp.src(config.src + '**/*.less')
        .pipe(less({
            // paths: Array of paths to be used for @import directives
            paths: [path.join(__dirname, 'less', 'includes')]
        }))
        .pipe(minifyCss())
        .pipe(gulp.dest(config.temp))
        .pipe(connect.reload());
});

gulp.task('concat', function () {
    gulp.src(config.temp + '**/*.js')
        .pipe(concat(config.names.js))
        .pipe(babel())
        .pipe(uglify())
        .pipe(gulp.dest(config.dist));

    return gulp.src(config.temp + '**/*.css')
        .pipe(concat(config.names.css))
        .pipe(gulp.dest(config.dist))
});

gulp.task('connect', function() {
    connect.server({
        root: ['./', '../'],
        livereload: true
    });
});

gulp.task('serve', function () {
    gulp.watch(config.src + '**/*', ['build']);
    gulp.watch(config.test + '**/*', ['build']);
});

gulp.task('build', function () {
    return runSequence('clean', ['es6', 'less', 'html'], 'concat', 'clean-temp');
});

gulp.task('start', function () {
    runSequence('build', 'connect', 'serve');
});

gulp.task('default', ['build']);
