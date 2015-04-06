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
  del = require('del');


var paths = {
  'bootstrap': 'node_modules/bootstrap-sass/assets',
  'fontawesome': 'node_modules/font-awesome'
};

// Compile Sass, Autoprefix and minify
gulp.task('sass', function() {
  return sass('src/scss/main.scss', {
    style: 'expanded',
    precision: '8',
    loadPath: [
      paths.bootstrap + '/stylesheets',
      paths.fontawesome + '/scss'
    ]
  })
    .pipe(autoprefixer('last 2 version', 'safari 5', 'ie 8', 'ie 9', 'opera 12.1', 'ios 6', 'android 4'))
    .pipe(gulp.dest('lib/css'))
    .pipe(rename({
      suffix: '.min'
    }))
    .pipe(minifycss())
    .pipe(gulp.dest('lib/css'))
    .pipe(notify({
      message: 'Styles task complete'
    }));
});
