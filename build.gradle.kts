import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

group = "io.github.nahuel92"
val pluginVersion = providers.gradleProperty("pluginVersion").get()

plugins {
    kotlin("jvm") version "2.4.10"
    id("org.jetbrains.changelog") version "2.5.0"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}


changelog {
    version = pluginVersion
    repositoryUrl = "https://github.com/Nahuel92/pit4u"
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

intellijPlatform {
    val sinceVersion = "261"
    pluginConfiguration {
        id = "io.github.nahuel92.pit4u"
        name = "PIT4U"
        version = pluginVersion
        description = "Plugin that allows you to run PIT mutation tests directly from your IDE"
        ideaVersion {
            sinceBuild = sinceVersion
        }
        val changelog = project.changelog
        changeNotes = provider {
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML
                )
            }
        }
    }
    pluginVerification {
        ides {
            select {
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = sinceVersion
            }
            recommended()
        }
    }
    buildSearchableOptions = false
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

val pitVersion = "1.30.0"
dependencies {
    implementation("tools.jackson.dataformat:jackson-dataformat-xml:3.2.2")
    implementation("org.pitest:pitest:$pitVersion")
    implementation("org.pitest:pitest-junit5-plugin:1.2.3")
    implementation("org.pitest:pitest-command-line:$pitVersion")
    implementation("org.pitest:pitest-entry:$pitVersion")
    intellijPlatform {
        intellijIdea("2026.1")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.idea.maven")
        bundledPlugin("com.intellij.gradle")
        bundledPlugin("com.intellij.java")
        pluginVerifier()
        zipSigner()

        testFramework(
            TestFrameworkType.Starter,
            "",
            configurationName = "integrationTestImplementation"
        )
    }
    testRuntimeOnly(kotlin("stdlib"))
    testRuntimeOnly(kotlin("reflect"))
    val junitBom = platform("org.junit:junit-bom:5.10.3")
    integrationTestImplementation(junitBom)
    integrationTestImplementation("org.junit.jupiter:junit-jupiter")
    integrationTestImplementation("org.kodein.di:kodein-di-jvm:7.20.2")
    integrationTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.1")
    integrationTestRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(25)
}

val integrationTest by intellijPlatformTesting.testIdeUi.registering {
    task {
        val integrationTestSourceSet = sourceSets.getByName("integrationTest")
        testClassesDirs = integrationTestSourceSet.output.classesDirs
        classpath = integrationTestSourceSet.runtimeClasspath

        systemProperty(
            "path.to.build.plugin",
            tasks.prepareSandbox.get().pluginDirectory.get().asFile
        )
        useJUnitPlatform()
        dependsOn(tasks.prepareSandbox)

        // Make test execution visible in console
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}