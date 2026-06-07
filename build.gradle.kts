import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    id("java")
    id("org.jetbrains.changelog") version "2.5.0"
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "io.github.nahuel92"

val sinceVersion = "261"
val pluginVersion = providers.gradleProperty("pluginVersion").get()
val pitVersion = "1.25.3"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

changelog {
    version = pluginVersion
    unreleasedTerm = "[Unreleased]"
    outputFile = file("release-note.txt")
    repositoryUrl = "https://github.com/Nahuel92/pit4u"
}

intellijPlatform {
    pluginConfiguration {
        version = pluginVersion
        id = "io.github.nahuel92.pit4u"
        name = "PIT4U"
        description = "Plugin that allows you to run PIT mutation tests directly from your IDE"
        ideaVersion {
            sinceBuild.set(sinceVersion)
            untilBuild.set(provider { null })
        }
        changeNotes.set(provider {
            changelog.renderItem(
                changelog.getLatest(),
                Changelog.OutputType.HTML
            )
        })
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
    buildSearchableOptions.set(false)
}

dependencies {
    implementation("org.junit.platform:junit-platform-launcher:6.1.0")
    implementation("org.pitest:pitest:$pitVersion")
    implementation("org.pitest:pitest-junit5-plugin:1.2.3")
    implementation("org.pitest:pitest-command-line:$pitVersion")
    implementation("org.pitest:pitest-entry:$pitVersion")
    intellijPlatform {
        intellijIdea("2026.1")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.idea.maven")
        bundledPlugin("com.intellij.gradle")

        pluginVerifier()
        zipSigner()

        testFramework(TestFrameworkType.Platform)
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    patchPluginXml {
        changeNotes = provider {
            changelog.renderItem(
                changelog
                    .getUnreleased()
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML
            )
        }
    }
}