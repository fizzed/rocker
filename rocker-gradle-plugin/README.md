# A gradle plugin for Rocker.

This gradle plugin creates a task `rockerCompile` which is run before compileJava.
The plugin is loosely modelled after the Antlr plugin.

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
    // The settings are shown with their defaults:

    // Skips building templates all together
    skip false
    // Base directory for generated java sources, actual target is sub directory 
    // with source set name. The value is passed through project.file(). 
    outputBaseDirectory = project.buildDir.toString() + "/generated-src/rocker"
    // Base directory where the java classes are generated and stores 
    // rocker-compiler.conf (used by RockerRuntime.getInstance().setReloading(true)),
    // actual target is sub directory with source set name. The value is passed 
    // through project.file().
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

repositories {
    mavenCentral()
}

buildscript {
    repositories {
        maven {
            url uri('../repo') // same absolute path as exported with uploadArchives
        }
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
This build has been tested in Intellij Community addition.

By running `./gradlew uploadArchives` you will export the plugin to `../../repo`. The absolute path in the project that uses the plugin must be the same.
