var del = require('del');
var gulp = require('gulp');
var runSequence = require('run-sequence');
var plugins = require('gulp-load-plugins')({lazy: false});
var templateCache = require('gulp-angular-templatecache');
var config = require('./config.json');

gulp.task('clean', function () {
  return del([config.dist, config.temp], {force: true});
});

gulp.task('clean-temp', function () {
  del([config.temp], {force: true});
});

gulp.task('html', function () {
  return gulp.src(config.src + '**/*.html')
    .pipe(templateCache({
      module: config.names.partialsModule,
      standalone: true
    }))
    .pipe(gulp.dest(config.temp));
});

gulp.task('jshint', function () {
  return gulp.src(config.src + '**/*.js').
  pipe(plugins.jshint()).
  pipe(plugins.jshint.reporter('jshint-stylish')).
  pipe(plugins.jshint.reporter('fail'));
});

gulp.task('js', function () {
  return gulp.src(config.src + '**/*.js')
    .pipe(plugins.babel())
    .pipe(plugins.jshint())
    .pipe(plugins.concat(config.names.js))
    .pipe(plugins.ngAnnotate({
      add: true,
      // jshint -W106
      single_quotes: true
    }))
    .pipe(gulp.dest(config.temp));
});

gulp.task('less', function () {
  return gulp.src(config.src + '**/*.less')
    .pipe(plugins.less())
    .pipe(plugins.minifyCss())
    .pipe(gulp.dest(config.temp));
});

gulp.task('concat:js', function () {
  return gulp.src(config.temp + '**/*.js')
    .pipe(plugins.concat(config.names.js))
    .pipe(plugins.uglify())
    .pipe(gulp.dest(config.dist));
});

gulp.task('concat:css', function () {
  return gulp.src(config.temp + '**/*.css')
    .pipe(plugins.concat(config.names.css))
    .pipe(plugins.autoprefixer({
      browsers: ['last 2 versions'],
      cascade: false
    }))
    .pipe(gulp.dest(config.dist));
});

gulp.task('serve', function () {
  gulp.watch(config.src + '**/*', ['build']);
  gulp.watch(config.test + '**/*', ['build']);
});

gulp.task('build', function () {
  return runSequence('clean', 'jshint', ['js', 'less', 'html'], ['concat:js', 'concat:css'], 'clean-temp');
});

gulp.task('start', function () {
  return runSequence('build', 'serve');
});

gulp.task('default', ['build']);
