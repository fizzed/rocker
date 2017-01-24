# A gradle plugin for Rocker.

This gradle plugin creates a task `rockerCompile` which should be run before your projects
main build script.

## Example gradle script using this plugin:

```
apply plugin: 'java'
apply plugin: 'rocker'
apply plugin: 'idea'
apply plugin: 'application'

mainClassName = 'com.example.MainClass' // change to your main class
sourceCompatibility = 1.8

sourceSets {
    main {
        java {
            srcDir('src')
            srcDir(project.buildDir.toString() + '/generated/source/apt/main')
            srcDir(project.buildDir.toString() + "/classes/main")
        }
    }
}

rocker {
    // skips building templates all together
    skip false
    // Directory that has your Template.rocker.html files
    templateDirectory = file('src/main/java/')
    // Directory where java files are created
    outputDirectory = file(project.buildDir.toString() + "/generated/source/apt/main")
    // Directory where the java classes are generated and stores 
    // rocker-compiler.conf (used by RockerRuntime.getInstance().setReloading(true))
    classDirectory = file(project.buildDir.toString() + "/classes/main")

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
        classpath group: 'com.fizzed.rocker',
                name: 'rockergradleplugin',
                version: '1.0-SNAPSHOT'

    }
}

dependencies {
    compile group: 'com.fizzed',
            name: 'rocker-compiler',
            version: '0.14.0'

    compile group: 'com.fizzed',
            name: 'rocker-runtime',
            version: '0.14.0'

    compile group: 'org.slf4j',
            name: 'slf4j-simple',
            version: '1.6.1'

    testCompile group: 'junit', name: 'junit', version: '4.11'
}
```

## Building the standalone plugin
This build has been tested in Intellij Community addition.

By running `./gradlew uploadArchives` you will export the plugin to `../../repo`. The absolute path in the project that uses the plugin must be the same.
