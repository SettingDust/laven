import com.diffplug.gradle.spotless.SpotlessApply

plugins {
    kotlin("jvm") version "1.4.0"
    `maven-publish`
    id("com.diffplug.spotless") version "5.1.0"
}
val major = 1
val minor = 0
val patch = 1

group = "me.settingdust"
val mainVersion = arrayOf(major, minor, patch).joinToString(".")

group = "me.settingdust"
version = {
    var version = mainVersion
    val suffix = mutableListOf("")
    if (System.getenv("BUILD_NUMBER") != null) {
        suffix += System.getenv("BUILD_NUMBER").toString()
    }
    if (System.getenv("GITHUB_REF") == null || System.getenv("GITHUB_REF").endsWith("-dev")) {
        suffix += "unstable"
    }
    version += suffix.joinToString("-")
    version
}()

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/SettingDust/laven")
            credentials {
                username = project.findProperty("gpr.user") as? String ?: System.getenv("GPR_USER")
                password = project.findProperty("gpr.key") as? String ?: System.getenv("GPR_API_KEY")
            }
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")

    testApi(kotlin("test"))
    testApi(kotlin("test-common"))
    testApi(kotlin("test-annotations-common"))
    testApi(kotlin("test-junit5").toString()) {
        exclude("org.junit.platform")
    }
    testApi("org.junit.jupiter:junit-jupiter:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

defaultTasks("test")

tasks {
    build {
        dependsOn(withType<SpotlessApply>())
    }

    test {
        useJUnitPlatform()
    }
}

sourceSets.apply {
    main {
        java.srcDirs("src")
        resources.srcDirs("resources")
    }

    test {
        java.srcDirs("test")
        resources.srcDirs("testresources")
    }
}

spotless {
    val ktlintVersion = "0.37.2"
    kotlin {
        ktlint(ktlintVersion)
    }
    kotlinGradle {
        ktlint(ktlintVersion)
    }
}