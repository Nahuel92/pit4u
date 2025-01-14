import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "io.github.nahuel92"

// Plugin version
val pluginVersion = "0.2.0"
val javaVersion = "21"
// IntelliJ version
val sinceVersion = "243"
val untilVersion = "243.*"
val intellijIdeaCommunityVersion = "2024.3.1.1"
// PIT version
val pitVersion = "1.17.4"
val pitestJunit5PluginVersion = "1.2.1"

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
        version = pluginVersion
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
    implementation("org.pitest:pitest:$pitVersion")
    implementation("org.pitest:pitest-junit5-plugin:$pitestJunit5PluginVersion")
    implementation("org.pitest:pitest-command-line:$pitVersion")
    implementation("org.pitest:pitest-entry:$pitVersion")
    intellijPlatform {
        intellijIdeaCommunity(intellijIdeaCommunityVersion)
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
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    patchPluginXml {
        version = "$pluginVersion"
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