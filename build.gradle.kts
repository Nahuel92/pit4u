import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "io.github.nahuel92"

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
        version = "0.1.5"
        description = "Plugin that allows you to run PIT mutation tests directly from your IDE"
        ideaVersion {
            sinceBuild.set("242")
        }
    }
}

val pitVersion = "1.17.1"

dependencies {
    implementation("org.pitest:pitest:$pitVersion")
    implementation("org.pitest:pitest-junit5-plugin:1.2.1")
    implementation("org.pitest:pitest-command-line:$pitVersion")
    implementation("org.pitest:pitest-entry:$pitVersion")
    intellijPlatform {
        intellijIdeaCommunity("2024.2.3")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.idea.maven")
        bundledPlugin("com.intellij.gradle")

        pluginVerifier()
        zipSigner()
        instrumentationTools()

        testFramework(TestFrameworkType.Platform)
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    patchPluginXml {
        sinceBuild.set("242")
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