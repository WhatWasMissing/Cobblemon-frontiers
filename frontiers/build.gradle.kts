plugins {
    id("java")
    id("java-library")
    id("dev.architectury.loom") version "1.13.467" apply false
    id("architectury-plugin") version "3.4.162" apply false
    id("com.gradleup.shadow") version "9.3.2" apply false
}

allprojects {
    apply(plugin = "java")

    version = project.properties["mod_version"]!!
    group = project.properties["maven_group"]!!

    repositories {
        mavenCentral()
        maven("https://maven.impactdev.net/repository/development/")
        maven("https://maven.neoforged.net/releases")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.processResources {
        filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
            expand(project.properties)
        }
    }
}
