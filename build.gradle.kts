plugins {
    kotlin("jvm") version "1.4-M3"
    `maven-publish`
}

group = "me.settingdust"
version = "1.0-SNAPSHOT"

fun getBuildNumber(): String {
    if (System.getenv("BUILD_NUMBER") != null) {
        return System.getenv("BUILD_NUMBER").toString()
    }
    return ""
}

fun getStable(): String {
    if (System.getenv("GITHUB_REF") == null || System.getenv("GITHUB_REF").endsWith("-dev")) {
        return "unstable"
    }
    return ""
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/SettingDust/laven")
            credentials {
                username = project.findProperty("gpr.user") as? String ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as? String ?: System.getenv("TOKEN")
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
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", "1.4-M3"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("io.github.config4k:config4k:0.4.2")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation(kotlin("test-junit5"))
}

defaultTasks("test")

tasks.withType<Test> {
    useJUnitPlatform()
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
