Rocker Templates by Fizzed
==========================

#### 0.10.4 - 2016-12-17

 - Templates now use the classloader of model object (@alkemist)

#### 0.10.3 - 2015-10-16

 - Fix incorrect truncating of escaped unicode symbols; disable escaping of
   unicode in debug comments (jshinobi)
 - Fix Java 7 compiler issues
 - Maven project builds in either Java 7 or 8
 - Project uses TravisCI

#### 0.10.2 - 2015-10-01

 - Added support for @break and @continue in loops
 - Fixed antlr grammar to allow value names to start with reserved names (e.g.
   a variable named 'format' used to conflict with 'for' reserved name)

#### 0.10.1 - 2015-09-28

 - Added support for calling templates dynamically with list of required arguments
 - Fixed output issue of large primitive long values
 - Added check of valid file extension on dynamic template paths
 - Improved error codes during dynamic template load
 - Numerous doc enhancements

#### 0.10.0 - 2015-09-24

 - Hot reloading of templates at runtime with two different methods supported.
 - Alternative method of creating templates with dynamic loading of templates
   by path and dynamic properties.
 - Major refactoring of working internals to support hot reloading
 - Feature rich template compiler that parses templates, generates Java source,
   and compiles Java source to byte code
 - New "rocker.optimize" property for maven plugin which disables all "reloading"
   code. Useful for production compiles where you'd like to tweak an extra ~3%
   performance out of your templates.
 - New "extendsModelClass" configuration property in maven plugin and in templates
   to change the superclass a template model inherits from.

#### 0.9.0 - 2015-03-18

 - Initial release
