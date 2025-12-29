plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kover)
    alias(libs.plugins.jreleaser)
    id("maven-publish")
    id("signing")
}

group = "com.contextable"
version = rootProject.version

kotlin {
    // Configure K2 compiler options
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                    freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
                    freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
                    freeCompilerArgs.add("-opt-in=kotlinx.serialization.ExperimentalSerializationApi")
                    languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
                    apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
                }
            }
        }
    }

    // Android target
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
                }
            }
        }
        publishLibraryVariants("release")
    }

    // JVM target (Desktop)
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
                }
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Apply default hierarchy template
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)

            // Kotlinx libraries
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)

            // Image loading (Coil 3)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

            // Logging
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "com.contextable.a2ui4k"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    testOptions {
        targetSdk = 36
    }

    buildToolsVersion = "36.0.0"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

// Staging directory for JReleaser
val stagingDir = layout.buildDirectory.dir("staging")

// Publishing configuration
publishing {
    publications {
        withType<MavenPublication> {
            groupId = "com.contextable"
            // artifactId is set automatically by KMP with platform suffixes (e.g., a2ui-4k-jvm)
            // The base name comes from the project name in settings.gradle.kts
            version = project.version.toString()
            pom {
                name.set("a2ui-4k")
                description.set("A2UI rendering engine for Compose Multiplatform")
                url.set("https://github.com/contextable/a2ui-4k")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("contextablemark")
                        name.set("Mark Fogle")
                        email.set("mark@contextable.com")
                    }
                }

                scm {
                    url.set("https://github.com/contextable/a2ui-4k")
                    connection.set("scm:git:git://github.com/contextable/a2ui-4k.git")
                    developerConnection.set("scm:git:ssh://github.com:contextable/a2ui-4k.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "staging"
            url = uri(stagingDir)
        }
    }
}

// Signing configuration
signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// JReleaser configuration for Maven Central publishing
afterEvaluate {
    jreleaser {
        gitRootSearch.set(true)

        project {
            name.set("a2ui-4k")
            version.set(rootProject.version.toString())
            description.set("A2UI rendering engine for Compose Multiplatform")
            links {
                homepage.set("https://github.com/contextable/a2ui-4k")
            }
            authors.set(listOf("Mark Fogle"))
            license.set("Apache-2.0")
            inceptionYear.set("2025")
            copyright.set("Contextable LLC")

            @Suppress("DEPRECATION")
            java {
                groupId.set("com.contextable")
                multiProject.set(false)
            }
        }

        signing {
            active.set(org.jreleaser.model.Active.ALWAYS)
            armored.set(true)
        }

        deploy {
            maven {
                pomchecker {
                    version.set("1.14.0")
                    failOnWarning.set(false)
                    failOnError.set(false)
                }

                mavenCentral {
                    create("sonatype") {
                        active.set(org.jreleaser.model.Active.ALWAYS)
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepository(stagingDir.get().asFile.absolutePath)
                        namespace.set("com.contextable")
                        sign.set(true)
                        checksums.set(true)
                        applyMavenCentralRules.set(false)
                        verifyPom.set(false)
                        sourceJar.set(false)
                        javadocJar.set(false)

                        // iOS artifact overrides - disable jar validation for .klib files
                        artifactOverride {
                            groupId.set("com.contextable")
                            artifactId.set("a2ui-4k-iosx64")
                            jar.set(false)
                            sourceJar.set(false)
                            javadocJar.set(false)
                        }
                        artifactOverride {
                            groupId.set("com.contextable")
                            artifactId.set("a2ui-4k-iosarm64")
                            jar.set(false)
                            sourceJar.set(false)
                            javadocJar.set(false)
                        }
                        artifactOverride {
                            groupId.set("com.contextable")
                            artifactId.set("a2ui-4k-iossimulatorarm64")
                            jar.set(false)
                            sourceJar.set(false)
                            javadocJar.set(false)
                        }
                    }
                }
            }
        }
    }
}
