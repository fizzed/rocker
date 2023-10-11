Rocker Templates by Fizzed
==========================

#### 1.4.0 - 2023-10-11

 - Java 6 support dropped. Java 7 is now minimum.
 - Validation of Java 7, 8, 11, 17, and 19
 - Gradle build process uses version 7.3

#### 1.3.0 - 2020-05-29

 - NOTE: Templates compiled before v1.2.5 are not runtime-compatible if compiled from v1.2.4 due to a change
   of method signature in how values are rendered.  We are bumping up the minor number and re-releasing so
   users take caution.  All templates are still source compatible and fine if generated with v1.2.5+.

#### 1.2.5 - 2020-05-26

 - Null safe operator guards against target value toString() also returning a null (@jjlauer)
 - Bump gradle publish plugin to v0.12.0

#### 1.2.4 - 2020-05-26
  
 - Version skipped due to deploy issue to Maven Central

#### 1.2.3 - 2020-01-07

 - Relaxed dynamic binding of template arguments. You can supply a map of keys and values
   and valid keys are set on the template model. (@pmlopes)
 - Additional improvements for Java version detection. (@igorbolic)

#### 1.2.2 - 2019-09-10

 - Support for marking classes with generated annotation (@serranya)

#### 1.2.1 - 2019-01-18

 - @args supports advanced generics (e.g. List<? extends Foo>) (@plblueraven)

#### 1.2.0 - 2018-11-21

 - More flexible approach to parsing Java version number to handle faster release cycle. (@drauf)

#### 1.1.0 - 2018-09-04

 - Generated templates use static methods vs. final constants for a few key internal variables. Helps
   with incremental compilation in gradle and other environments. (@breskeby)

#### 1.0.0 - 2018-08-23

 - Rocker templates have been battle tested for years -- time to officially do a v1 release!
 - Work for Java 9 and 10 compat (@drauf)

#### 0.24.0 - 2018-03-15

 - Java 9 support! (@benjamin-demarteau)
 - @for loop will now work with any Iterator object (works well with Stream.iterator() too) (@jjlauer)

#### 0.23.0 - 2017-11-20

 - @for loop will now work with any Iterable object (@bendem)

#### 0.22.0 - 2017-08-03
 
 - Issue #68: null-safe operator support for RockerContent rendering (@breskeby)

#### 0.21.0 - 2017-07-26

 - Extract runner logic from JavaGeneratorMain into Runnable interface (@breskeby)

#### 0.20.0 - 2017-05-02

 - Gradle plugin! (@victory and @mnlipp)
 - Issue #59: Add `@Generated` and `@SuppressWarnings` annotations to generated
   source files

#### 0.19.0 - 2017-04-28

 - Fix typo in log message (@mark-vieira)
 - Beta gradle plugin!

#### 0.18.0 - 2017-04-12

 - Omit MODIFIED_AT header when 'optimize' option is true (@mark-vieira). 
   Useful in cases you don't have hot reload enabled (e.g. CI environments)

#### 0.17.0 - 2017-04-11

 - Issue #56: fix nested else-if blocks failing to compile
 - Issue $57: allow rocker template filenames with a length of 1

#### 0.16.0 - 2017-03-02
  
 - Support for `else if` in if-else blocks. (@mreuvers)

#### 0.15.0 - 2017-01-13

 - `@with` blocks now support multiple variables to be set in a single statement.
   Excellent work by @mreuvers to make it functional.

#### 0.14.0 - 2016-11-28

 - Variables from enhanced @for loops are now `final`.  This allows them to be
   used in nested `@with` blocks. (@mreuvers)

#### 0.13.1 - 2016-09-26

 - Fixed off by 1 error with iterating primitive collections
 - Support for iterating String and Object primitive arrays
 - Significantly faster HTML escaping for case where String requires zero
   replacements.

#### 0.13.0 - 2016-09-19
 
 - Major built-in support for null safe handling!  This release will help you
   avoid NullPointerExceptions in numerous areas.
 - NOTE: some significant changes under-the-hood. Templates will be source
   compatible with older versions, but will likely have runtime issues (e.g.
   templates compiled with v0.12.0 will likely have runtime errors with v0.13.0)
   We recommend you recompile all your templates with v0.13.0+.
 - NOTE: default html escaping now uses an internal version rather than apache
   commons.  This internal version is slightly faster, but also only escapes
   5 entities (matches the entities Guava encodes by default).  The previous 
   version used Apache commons lang3 which included many other entities that are
   irrelevant with modern widespread usage of utf-8.
 - New `@()` eval expression will evaluate the expression and then render it
 - New `@?value` expression will only render the value if its not null
 - New `@value?:defaultValue` null ternary expression will either render the
   value if not null or will render the defaultValue.  The defaultValue can
   include strings or literals.  The ternary operation also follows short-circuit
   evaluation so defaultValue is only evaluated if value was null.
 - New `@with?` expression will either render the with block or you can optionally
   include an `else` block that will be rendered instead.
 - If guava is on the classpath, its html escaper will be used instead of 
   Rocker's internal default version.  Its about 3-4x faster.  Its such a large
   dependency, its only optional, so simply add guava to your classpath to
   activate.
 - `rocker-runtime` no longer depends on Apache commons lang3.  Its now only a
   dependency of `rocker-compiler`.
 - More efficient `@for` loops that use primitive array types.
 - Antlr dependency from v4.5 to v4.5.3

#### 0.12.3 - 2016-07-16

 - Ignore synthetic fields in plain text classloader (alkemist)

#### 0.12.2 - 2016-07-13

 - Fixed backwards compat with templates compiled w/ older versions running with this
   version of Rocker.

#### 0.12.1 - 2016-07-13

 - Load template field inner classes via the classloader that loaded the template (alkemist)

#### 0.12.0 - 2016-06-01

 - New @with feature to set a scoped variable to a value.  When in Java 8+, the
   variable type is optional since it will be inferred by the compiler.

#### 0.11.1 - 2016-02-04

 - Removed generic type parameter for DefaultRockerModel to fix an issue with
   model.render(outputFactory) returning a generic vs. concrete type.

#### 0.11.0 - 2016-02-04

 - Added support for overriding the RockerOutput a model will render to
 - Removed `callback` feature on RockerModel. Use new model.render(outputFactory, templateCustomizer)
   as its superior replacement.

#### 0.10.6 - 2016-02-03

 - Allow @args consisting of any amount of empty whitespace (@jfendler)
 - More unit tests

#### 0.10.5 - 2016-01-19

 - Fixed issue with plain text `} else {` blocks being interpreted as code rather
   than as plain text

#### 0.10.4 - 2015-12-17

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
