# A gradle plugin for Rocker.

This gradle plugin that creates `generateRockerTemplateSource` tasks that are 
run before `compileJava`. 

## Example gradle script using this plugin:

```groovy
apply plugin: 'java'
apply plugin: 'rocker-gradle-plugin'// actually implies java plugin

sourceCompatibility = 1.8

sourceSets {
    main {
        rocker {
            // Directory that has your Template.rocker.html files
            srcDir('rocker-templates')
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
    outputBaseDirectory = project.buildDir.toString() + "/generated-src/rocker"
    // Base directory for the directory where the hot reload feature 
    // will (re)compile classes to at runtime (and where `rocker-compiler.conf`
    // is generated, which is used by RockerRuntime.getInstance().setReloading(true)).
    // The actual target is a sub directory with the name of the source set. 
    // The value is passed through project.file().
    classBaseDirectory = project.buildDir.toString() + "/classes"

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
}

// For each source set "name" a task "generate<name>RockerTemplateSource"
// is generated (with an empty "name" for the main source set). It is
// possible to override the directories derived from the base names
// by setting the tasks' properties "classDir" and "outputDir".

// For a complete build.gradle you also need:

repositories {
    mavenCentral()
}

buildscript {
    repositories {
        mavenLocal() // Provided you have published the plugin there
        jcenter() // Needed for plugin's dependencies
    }

    dependencies {
        classpath group: 'com.fizzed',
                name: 'rocker-gradle-plugin',
                version: '1.0-SNAPSHOT'

    }
}

dependencies {
    compile group: 'com.fizzed',
            name: 'rocker-compiler',
            version: '0.18.0'

    compile group: 'com.fizzed',
            name: 'rocker-runtime',
            version: '0.18.0'

    compile group: 'org.slf4j',
            name: 'slf4j-simple',
            version: '1.6.1'

    testCompile group: 'junit', name: 'junit', version: '4.11'
}
```

## Building the standalone plugin
This build has been tested in Eclipse and in Intellij Community addition.

By running `./gradlew publishToMavenLocal` you can make the plugin available
locally.
