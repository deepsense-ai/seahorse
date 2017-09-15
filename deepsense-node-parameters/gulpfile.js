/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 08.06.15.
 */

var del = require('del');
var gulp = require('gulp');
var uglify = require('gulp-uglify');
var runSequence = require('run-sequence');
var babelifygul = require('babelify');
var browserify = require('browserify');
var browserifyAnnotate = require('browserify-ngannotate');
var browserifyShim = require('browserify-shim');
var buffer = require('vinyl-buffer');
var source = require('vinyl-source-stream');

var config = require('./config.json');

gulp.task('clean', function () {
    return del([ config.dist + '*' ], { force: true });
});

gulp.task('js', function () {
    return browserify({
        entries: [ config.src + config.files.entryPointFile ],
        debug: true
    })
        .transform(babelifygul)
        .transform(browserifyAnnotate, {
            add: true,
            // jshint -W106
            single_quotes: true
        })
        .transform(browserifyShim)
        .bundle()
        .pipe(source(config.files.distFile))
        .pipe(buffer())
        .pipe(uglify())
        .pipe(gulp.dest(config.dist));
});

gulp.task('serve', function () {
    gulp.watch(config.src + '**/*', [ 'build' ]);
    gulp.watch(config.test + '**/*', [ 'build' ]);
});

gulp.task('build', ['clean'], function () {
    return runSequence('js');
});

gulp.task('start', function () {
    return runSequence('build', 'serve');
});

gulp.task('default', ['build']);