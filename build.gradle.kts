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
        version = "1.0.0"
        description = "Plugin that allows you to run PIT mutation tests directly from your IDE"
        changeNotes = "notes"
        ideaVersion {
            sinceBuild.set("232")
            untilBuild.set("242.*")
        }
    }
}

dependencies {
    implementation("org.pitest:pitest:1.17.0")
    implementation("org.pitest:pitest-junit5-plugin:1.2.1")
    implementation("org.pitest:pitest-command-line:1.17.0")
    implementation("org.pitest:pitest-entry:1.17.0")
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
        sinceBuild.set("232")
        untilBuild.set("242.*")
        pluginVersion.set("1.0-SNAPSHOT")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    generateManifest {
        version.set("1.0-SNAPSHOT")
    }
}
