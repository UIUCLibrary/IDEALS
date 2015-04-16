/*!
 * gulp
 * $ npm install gulp-ruby-sass gulp-autoprefixer gulp-minify-css gulp-jshint gulp-concat gulp-uglify gulp-imagemin gulp-notify gulp-rename gulp-livereload gulp-cache del --save-dev
 */
var gulp = require('gulp'),
  sass = require('gulp-ruby-sass'),
  autoprefixer = require('gulp-autoprefixer'),
  minifycss = require('gulp-minify-css'),
  jshint = require('gulp-jshint'),
  uglify = require('gulp-uglify'),
  imagemin = require('gulp-imagemin'),
  rename = require('gulp-rename'),
  concat = require('gulp-concat'),
  notify = require('gulp-notify'),
  cache = require('gulp-cache'),
  livereload = require('gulp-livereload'),
    runSequence = require('run-sequence'),
  del = require('del');

// Paths - needed to find bootstrap adn fontawesome since they are downloaded by npm
var paths = {
  'bootstrap': 'node_modules/bootstrap-sass/assets',
  'fontawesome': 'node_modules/font-awesome'
};

// Compile Bootstrap
gulp.task('bootstrap', function() {
  return sass(paths.bootstrap + '/stylesheets/_bootstrap.scss', {
    style: 'expanded',
    precision: '8',
    loadPath: [
      paths.bootstrap + '/stylesheets'
    ]
  })
      .pipe(rename({
        basename: 'bootstrap'
      }))
      .pipe(gulp.dest('lib/css/'))
      .pipe(rename({
        suffix: '.min'
      }))
      .pipe(minifycss())
      .pipe(gulp.dest('lib/css/'))
      .pipe(notify({
        message: 'Bootstrap task complete'
      }));
});

// Compile Font Awesome
gulp.task('fontawesome', function() {
  return sass(paths.fontawesome + '/scss/font-awesome.scss', {
    style: 'expanded',
    loadPath: [
      paths.fontawesome + '/scss'
    ]
  })
      .pipe(gulp.dest('lib/css/'))
      .pipe(rename({
        suffix: '.min'
      }))
      .pipe(minifycss())
      .pipe(gulp.dest('lib/css/'))
      .pipe(notify({
        message: 'Font-Awesome task complete'
      }));
});

// Compile IDEALS styles, Autoprefix and minify
gulp.task('ideals', function() {
  return sass('src/scss/ideals.scss', {
    style: 'expanded'
  })
    .pipe(autoprefixer('last 2 version', 'safari 5', 'ie 8', 'ie 9', 'opera 12.1', 'ios 6', 'android 4'))
    .pipe(gulp.dest('lib/css'))
    .pipe(rename({
      suffix: '.min'
    }))
    .pipe(minifycss())
    .pipe(gulp.dest('lib/css'))
    .pipe(notify({
      message: 'IDEALS styles task complete'
    }));
});

// Compile print styles, Autoprefix and minify
gulp.task('print', function() {
  return sass('src/scss/print.scss', {
    style: 'expanded'
  })
      .pipe(autoprefixer('last 2 version', 'safari 5', 'ie 8', 'ie 9', 'opera 12.1', 'ios 6', 'android 4'))
      .pipe(gulp.dest('lib/css'))
      .pipe(rename({
        suffix: '.min'
      }))
      .pipe(minifycss())
      .pipe(gulp.dest('lib/css'))
      .pipe(notify({
        message: 'Print styles task complete'
      }));
});

gulp.task('styles', function() {
  runSequence('bootstrap', 'fontawesome', 'ideals', 'print');
})

// Clean
gulp.task('clean', function(cb) {
    //del(['dist/assets/css', 'dist/assets/js', 'dist/assets/img'], cb)
});

// Default task
gulp.task('default', ['clean'], function() {
    gulp.start('styles');
});

// Watch
gulp.task('watch', function() {

    // Watch .scss files
    gulp.watch('src/scss/*.scss', ['ideals']);
    gulp.watch(paths.bootstrap + '/stylesheets/' + '/**/*.scss', ['bootstrap']);
    gulp.watch(paths.fontawesome + '/scss/' + '/**/*.scss', ['fontawesome']);

    //// Watch .js files
    //gulp.watch('src/scripts/**/*.js', ['scripts']);
    //
    //// Watch image files
    //gulp.watch('src/images/**/*', ['images']);

});