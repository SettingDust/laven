plugins {
    kotlin("jvm") version "1.4-M3"
    `maven-publish`
}
val major = 1
val minor = 0
val patch = 1

group = "me.settingdust"
version = "$major.$minor.$patch-b${getBuildNumber()}${if (getStable().isNotBlank()) "-${getStable()}" else ""}"

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
    testImplementation(kotlin("test-junit5").toString()) {
        exclude("org.junit.platform")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

defaultTasks("test")

tasks.test {
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
