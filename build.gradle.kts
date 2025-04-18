import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "io.github.nahuel92"

val sinceVersion = "243"
val untilVersion = "251.*"
val pitVersion = "1.19.1"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")
        id = "io.github.nahuel92.pit4u"
        name = "PIT4U"
        version = "0.2.2"
        description = "Plugin that allows you to run PIT mutation tests directly from your IDE"
        ideaVersion {
            sinceBuild.set(sinceVersion)
            untilBuild.set(untilVersion)
        }
    }
    pluginVerification {
        ides {
            select {
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = sinceVersion
                untilBuild = untilVersion
            }
        }
    }
    buildSearchableOptions.set(false)
}

dependencies {
    implementation("org.junit.platform:junit-platform-launcher:1.12.2")
    implementation("org.pitest:pitest:$pitVersion")
    implementation("org.pitest:pitest-junit5-plugin:1.2.2")
    implementation("org.pitest:pitest-command-line:$pitVersion")
    implementation("org.pitest:pitest-entry:$pitVersion")
    intellijPlatform {
        intellijIdeaCommunity("2024.3.5")
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
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    patchPluginXml {
        sinceBuild.set(sinceVersion)
        untilBuild.set(untilVersion)
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}