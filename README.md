Rocker Templates by Fizzed
==========================

[![Build Status](https://travis-ci.org/fizzed/rocker.svg?branch=master)](https://travis-ci.org/fizzed/rocker)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.fizzed/rocker/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.fizzed/rocker)

[Fizzed, Inc.](http://fizzed.com) (Follow on Twitter: [@fizzed_inc](http://twitter.com/fizzed_inc))

## Sponsored by

Rocker is proudly sponsored by <a href="https://www.greenback.com">Greenback</a>.  We love the service and think you would too.

<a href="https://www.greenback.com/?utm_source=github.com&utm_medium=sponsorship&utm_campaign=fizzed-rocker" title="Greenback - Expenses made simple"><img src="https://www.greenback.com/assets/images/logo-greenback.png" height="48" width="166" alt="Greenback"></a>

<a href="https://www.greenback.com?utm_source=github.com&utm_medium=sponsorship&utm_campaign=fizzed-rocker" title="Greenback - Expenses made simple">More engineering. Less paperwork. Expenses made simple.</a>

## Overview

Rocker is a Java 8 optimized (runtime compat with 6+), near zero-copy rendering,
speedy template engine that produces statically typed, plain java object templates
that are compiled along with the rest of your project.  No more "warm-up" time
in production, slow reflection-based logic, or nasty surprises that should have
been caught during development.

Write your templates using an [intuitive, **tagless** syntax](docs/SYNTAX.md)
with standard Java expressions for logic, iteration, and values.  Use Rocker's
special `?` presence operator for null-safe evaluation. All the heavy
lifting is done by the Rocker parser during development -- which keeps the runtime
dependencies down to just a handful of classes.  Rocker will parse your templates
and generate well-documented Java source files (so you can easily inspect and
understand how it works).

## Performance

Based on the following [template benchmark](https://github.com/fizzed/template-benchmark),
Rocker is the clear winner.  ~250% faster than Freemarker while also requiring
orders-of-magnitude less memory.

![Template Comparison](docs/benchmark.png)

## Two-minute drill

Most templates are used for websites, so here is a quick sample showing how
Rocker templates work and can call each other during the rendering process.
Create a template containing a common header and footer as well as a placeholder
for body content. Create template `src/main/java/views/main.rocker.html`

```html
@args (String title, RockerBody content)

<html>
    <head>
        <title>@title</title>
    </head>
    <body>
    @content
    </body>
</html>
```

The template we actually plan on showing to a user will render its content
within the context of the common/header footer. In Java terms, it's passing 
a block of rendering code to be executed within another template. Create template
`src/main/java/views/index.rocker.html`

```html
@args (String message)

@views.main.template("Home") -> {
    <h1>Hello @message!</h1>
}
```

Hey, what about the `RockerBody content` argument?  We cover it in more
detail in the [syntax readme](docs/SYNTAX.md), but for now just understand that its
the only special type of argument and instructs Rocker that a template expects
a "body" to be passed to it.

The Rocker parser will generate a Java source file for each template. They
will be `target/generated-sources/rocker/views/main.java` and 
`target/generated-sources/rocker/views/index.java`. In your application, you
can render the index template like so.

```java
static public void main(String[] args) {

    String output = views.index.template("World")
        .render()
        .toString();

}
```

The output will equal:

```html
<html>
    <head>
        <title>Home</title>
    </head>
    <body>
        <h1>Hello World!</h1>
    </body>
</html>
```

Once you generate the Java sources and peek inside the code, it's simple
to see how this works. The views.index class creates a views.main template instance
and hands off rendering to it -- while also passing a block of itself that
it will render when views.main calls the `@content` variable.  The syntax is 
identical to how a lambda is defined in Java 8 (implemented with lambdas for Java 8
and anonymous inner classes for Java 6/7).  Rocker does a number of things behind
the scenes to make sure templates that create other templates share the same
rendering context (output buffer, application-specific context/implicit state).

## Features

 * Templates are runtime compatible with Java 6+

 * Optimizations enabled when targeting Java 8+ -- using Lambdas and type inference under-the-hood

 * [Near zero-copy rendering](#near-zero-copy-rendering)

 * [Hot reload support in two flavors](#hot-reloading)

 * [Elegant, intuitive, tagless syntax](docs/SYNTAX.md) that infers when your logic ends for control / dynamic
   content.  All dynamic / control code uses standard Java syntax.

 * A special `?` presence operator extends syntax for simplified handling of
   null values.

 * Parsed templates become normal POJOs with defined arguments -- allowing you
   to tap into your IDEs code completion, syntax highlighting, etc.

 * Support for injecting intermediate application-specific super classes during
   parsing & generating phase  -- thereby creating your own app-specific template engine
   where you can make implicit variables/methods available to all templates.

 * Since templates are just Java classes -- your logic / dynamic content can call
   out to any other Java code. Your templates can be as advanced or as simple as
   you need. No reflection used.

 * No runtime configuration/engine required -- there isn't any sort of RockerEngine
   class required to execute templates.  Each compiled template is ready-to-go
   and knows how to render itself.

 * Templates retain enough information about the original template to throw
   exceptions at runtime (during render()) that let you track down the problematic line
   in the original template source file.

## Syntax

Checkout the [SYNTAX.md](docs/SYNTAX.md) file for a comprehensive deep dive on
the rocker syntax.

## Framework integrations

Rocker has a growing list of frameworks that it has been seamlessly integrated with.
If you want to link to a new framework added, please file an issue or submit a PR:

 * Ninja Framework: https://github.com/fizzed/ninja-rocker
 * Jooby: http://jooby.org/doc/rocker
 * Spark Framework: https://github.com/perwendel/spark-template-engines

## Near zero-copy rendering

Static (plain text) for each Rocker template is (by default) stored internally as
static byte arrays already converted into your target charset (e.g. UTF-8). When
a template is rendered -- the static byte arrays are reused across all requests.
Rocker renders to an optimized output stream that stores a composite (linked list)
view of the reused byte arrays plus your dynamic content.  Since templates consist
mostly of static content rendered into the same charset over and over again,
rather than allocating new memory, copying that content, and then converting it
into your target charset for each request -- Rocker simply uses a pointer to it
over and over again. This technique produces fast and memory efficient
renders.

Let's say you have a template consisting of 9000 bytes of plain static text
and 1000 bytes of dynamic content.  Without this optimization, it 
would require ~100MB of memory to service 10000 requests (10000 bytes x 
10000 requests).  With this optimization, it would require ~10MB of memory
to service 10000 requests (1000 bytes x 10000 requests).  Besides lower memory,
you also cut out 90MB of memory copies and 90MB of UTF-8 String->byte conversions.
A pretty useful optimization.

## No reflection

Everything is compiled by your project's compiler along with your other Java
source code.  Any dynamic code in your template is ultimately converted into
standard Java and compiled.  No reflection used.

## Hot reloading

Version 0.10.0 introduced support for hot reloading templates during
development. Hot reloading allows you to modify the template source code,
save it, and have the changes active on the next request -- without
having to restart your JVM. Rocker offers two different flavors of hot
reloading for flexibility.

### Flavor 1: static interface, dynamic rendering

The major feature of Rocker templates is that your templates are compile-time
checked for usage, arguments, logic, etc. by the Java compiler.

In version 0.10.0 the underlying structure of a template was modified where a
template generates two underlying classes.  Each template generates a model class
 (its interface) and an implementation class (its renderer). Your application will
only interact directly with the model, therefore allowing Rocker to dynamically
recompile and reload the implementation class.

The major benefit of flavor one is that your application code remains the same
and is compile-time checked by the Java compiler, while the template content can
be modified and automatically reloaded at runtime.  Only in the case where you
actually change the template arguments, will you need to restart your application.

### Flavor 2: dynamic interface, dynamic rendering

If you prefer the convenience of fully dynamic templates, flavor two supports
hot reloading of both the template model class (its interface) as well as the
implementation class (its renderer).  Your application will lose some of the
compile-time checking and a small performance hit, but gain the convenience of
everything being reloadable. The way your application will use templates is
different as well.

```java
import com.fizzed.rocker.Rocker

...

// dynamic interfaces, dynamic implementation
String rendered = Rocker.template("views/index.rocker.html")
    .bind("val", "ValueA")
    .render()
    .toString();
```

The template path and arguments will be runtime-checked. Please note that each
bindable value must match the name and type declared in your template.

In case your bindable map may contain more values that than the required ones
a relaxed bind is available. The relaxed alternative will not fail rendering
if an attribute is extra to the required list. For example:

```
@args (String name)
Hello ${name}!
```

Will render in relaxed mode as:

```java
Map map = new HashMap();
map.put("name", "Joe");
map.put("age", 42);

Rocker.template("views/hello.rocker.html")
    .relaxedBind(map)
    .render();
// -> Hello Joe!
```

### Activate hot reloading

Support for hot reloading is added to your generated templates by default in
version 0.10.0.  If you'd like to disable support, set the configuration/system
property <code>rocker.optimize</code> to true during your build.  Since the code
is present in your templates by default, you merely need to turn it on at runtime.

#### Add dependency

The <code>rocker-compiler</code> dependency needs to be added to your build. 
This dependency only needs to be present during development and can be removed
in production. In Maven, this means you'll want to add the dependency in the
<code>provided</code> scope.
```xml
<dependency>
    <groupId>com.fizzed</groupId>
    <artifactId>rocker-compiler</artifactId>
    <version>1.3.0</version>
    <scope>provided</scope>
</dependency>
```

#### Enable at runtime

Activate hot reloading at runtime. You can activate hot reloading
either with a system property or programmatically.  For activating
hot reloading with a system property in maven.

```
mvn -Drocker.reloading=true ...rest of args...
```

Alternatively, you can activate hot reloading programmatically.

```java
import com.fizzed.rocker.runtime.RockerRuntime

...

RockerRuntime.getInstance().setReloading(true);
```

### Try out hot reloading

There is a simple example demonstrating hot reload in action.  This project 
uses [Blaze](https://github.com/fizzed/blaze) to help script tasks. Run the
following

    java -jar blaze.jar hot_reload

Point your browser to http://localhost:8080

Then modify & save <code>rocker-test-reload/src/test/java/views/index.rocker.html</code>
and refresh your browser.

## Getting started

Rocker consists of two components - the parser/generator and the runtime.  To
use Rocker in your project, add the runtime dependency to your application,
then enable the parser in your build tool followed by creating your first template.

### Add dependency

Rocker is published to Maven central. To add as a dependency in Maven:

```xml
<dependency>
    <groupId>com.fizzed</groupId>
    <artifactId>rocker-runtime</artifactId>
    <version>1.3.0</version>
</dependency>

<!-- for hot-reloading support only during development -->
<dependency>
    <groupId>com.fizzed</groupId>
    <artifactId>rocker-compiler</artifactId>
    <version>1.3.0</version>
    <scope>provided</scope>
</dependency>
```

To add as a dependency in Gradle:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    compile group: 'com.fizzed', name: 'rocker-runtime', version: '1.3.0'
    // add rocker-compiler dependency as needed
}
```

### Integrate parser/generator in build tool

Rocker supports Maven and Gradle out-of-the box.

#### Maven

Add the following to your pom

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.fizzed</groupId>
            <artifactId>rocker-maven-plugin</artifactId>
            <version>1.3.0</version>
            <executions>
                <execution>
                    <id>generate-rocker-templates</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

By default, Rocker will recursively process any template files ending with
`.rocker.html` in `src/main/java`.  The directory the
template is saved will become the standard Java package the
generated Java classes will be placed into.  The generated Java source files
will be saved to `target/generated-sources/rocker`.  The plugin
will take care of adding this generated directory to your sources root.

The following properties are supported:

 * `templateDirectory` is the base directory to recursively start from
    when locating and parsing template files.  The Java `package`
    a template will be generated to will use this directory as its base.  So
    if you have `${templateDirectory}/views/mytemplate.rocker.html`
    then Rocker will generate `${outputDirectory}/views/mytemplate.java`.
    Defaults to `${project.build.sourceDirectory}`.

 * `outputDirectory` is the directory the parser will generate sources
    for templates.
    Defaults to `${project.build.directory}/generated-sources/rocker`

 * `classDirectory` is the directory the hot reload feature will (re)compile
    classes to at runtime.
    Defaults to `${project.build.outputDirectory}`

 * `failOnError` determines whether any parsing/generating errors cause Maven
    to fail.
    Defaults to true.

 * `skip` determines whether execution of the plugin should be skipped.
    Defaults to false.

 * `touchFile` is the file to "touch" after successfully generating Java sources.
    Useful for triggering other workflow.  Many IDEs will not automatically reload
    generated sources for code completion unless either explicitly told to reload
    OR if the maven pom.xml file is changed. Thus, this value is by default set
    to `${basedir}/pom.xml`.  It's usually harmless to keep this enabled.

 * `skipTouch` disables touchFile.  Defaults to false.

 * `addAsSources` will add the outputDirectory to maven as sources
    to be compiled.  Defaults to true.

 * `addAsTestSources` will adds the outputDirectory to maven as test sources
    to be compiled.  Defaults to false.  If true, this is evaluated before
    addAsSources and effectively tells maven to compile your templates as test
    code.

The following properties are also supported, but it's important to understand
these are essentially passthrough overrides to the parser and they all default
to Rocker's default value.

 * `javaVersion` is the Java version you'd like your templates 
    compile & runtime compatible with.  Defaults to the Java version of the
    JVM executing maven (e.g. "1.8").

 * `optimize` determines if hot reloading support will be removed from the
    generated templates.  False by default.
 
 * `extendsClass` is the class that all template implementations should extend.
    Useful for application-specific intermediate classes that you'd like all
    templates to extend.
    Defaults to Rocker's default.

 * `extendsModelClass` is the class that all template models should extend.
    Useful for application-specific intermediate classes that you'd like all
    template models to extend.
    Defaults to Rocker's default.
    
 * `discardLogicWhitespace` determines whether whitespace in templates that is
    determined to be only a part of logic/control blocks should be discarded.
    Helps make rendered content look more professional, while still keeping 
    much of your formatting intact.
    Defaults to Rocker's default.
 
 * `targetCharset` is the target charset for template output.
    Defaults to Rocker's default.
   
 * `suffixRegex` is the regular expression to use to find templates to
    parse.
    Defaults to Rocker's default.

 * `markAsGenerated` adds a @Generated annotation to the generated classes.
    The Retention is CLASS so that the annotation can be used by tools that
    only rely on the class files and not on the source code.
    Defaults to Rocker's default.

#### Gradle

Thanks to `@victory` and `@mnlipp` for contributing the gradle plugin. `@etiennestuder`
also had an [alternative Gradle plugin](https://github.com/etiennestuder/gradle-rocker-plugin)
you may want to consider as well.  Rocker's gradle plugin is published to
gradle.org. Just add the following to your build script:

```groovy
plugins {
  id "com.fizzed.rocker" version "1.3.0"
}

sourceSets {
    main {
        rocker {
            srcDir('src/main/java')
        }
    }
}

rocker {
    // (All settings are shown with their defaults)
    // 
    // Skips building templates all together
    skip false
    // Base directory for generated java sources, actual target is sub directory 
    // with the name of the source set. The value is passed through project.file(). 
    outputBaseDirectory = "$buildDir/generated-src/rocker"
    // Base directory for the directory where the hot reload feature 
    // will (re)compile classes to at runtime (and where `rocker-compiler.conf`
    // is generated, which is used by RockerRuntime.getInstance().setReloading(true)).
    // The actual target is a sub directory with the name of the source set. 
    // The value is passed through project.file().
    classBaseDirectory = "$buildDir/classes"
    failOnError true
    skipTouch true
    // must not be empty when skipTouch is equal to false
    touchFile ""
    javaVersion '1.8'
    extendsClass null
    extendsModelClass null
    optimize null
    discardLogicWhitespace null
    targetCharset null
    suffixRegex null
    postProcessing null
    markAsGenerated null
}

```

### Create first template

The template syntax is described in detail below, but for now create a new
file in `${templateDirectory}/views/HelloWorld.rocker.html`

    @*
     Example of hello world
    *@
    @args (String message)

    Hello @message!

### Use compiled template

Time to compile your project and starting using the template.  You can call it
from java like so:

```java
static public void main(String[] args) {

    String output = views.HelloWorld
        .template("World")
        .render()
        .toString();

}
```

### Use optimized output

Rocker is heavily optimized (by default) to output templates as byte arrays.
The default `RockerOutput` a template will render to is of the type
`com.fizzed.rocker.runtime.ArrayOfByteArraysOutput`.  This is an excellent choice
for byte arrays or asynchronous IO. However, the framework has the capability
for optimized rendering to Strings (or other custom outputs).

To efficiently render to a String:

```java
import com.fizzed.rocker.runtime.StringBuilderOutput;

static public void main(String[] args) {

    StringBuilderOutput output = views.HelloWorld
        .template("World")
        .render(StringBuilderOutput.FACTORY);

    String text = output.toString();

}
```

To efficiently render to an OutputStream:

```java
import com.fizzed.rocker.runtime.OutputStreamOutput;

static public void main(String[] args) throws Exception {

    final OutputStream os = new FileOutputStream(new File("test"));

    OutputStreamOutput output = views.HelloWorld
        .template("World")
        .render((contentType, charsetName) -> new OutputStreamOutput(contentType, os, charsetName));

}
```

Please note that if there is an exception during the render the OutputStream
would have a partial template rendered (up to the point of the exception).  In
most cases it would be better to render to the default `com.fizzed.rocker.runtime.ArrayOfByteArraysOutput`
and write its buffer of byte arrays out directly to your OutputStream.

## Other demos?

There are numerous demos of Rocker in action.  From parsing templates into a
model to asynchronously sending results in an HTTP server.  This project 
uses [Blaze](https://github.com/fizzed/blaze) to help script tasks. Run the
following for a complete list:

    java -jar blaze.jar -l

## License

Copyright (C) 2015 Fizzed, Inc.

This work is licensed under the Apache License, Version 2.0. See LICENSE for details.
