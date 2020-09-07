plugins {
    kotlin("jvm") version "1.4.0"
    `maven-publish`
}
val major = 1
val minor = 0
val patch = 1

val mainVersion = arrayOf(major, minor, patch).joinToString(".")

group = "me.settingdust"
version =
    "$mainVersion${
        @OptIn(ExperimentalStdlibApi::class)
        buildList {
            add("")
            if (System.getenv("BUILD_NUMBER") != null) {
                add(System.getenv("BUILD_NUMBER").toString())
            }
            if (System.getenv("GITHUB_REF") == null || System.getenv("GITHUB_REF").endsWith("-dev")) {
                add("unstable")
            }
        }.joinToString("-")
    }"

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
    implementation(kotlin("stdlib", "1.4.0"))
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