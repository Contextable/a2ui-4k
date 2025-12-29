pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "a2ui-4k"

include(":a2ui-4k")
project(":a2ui-4k").projectDir = file("library")
include(":examples:catalog")
