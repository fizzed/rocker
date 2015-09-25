Rocker Templates by Fizzed
==========================

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
