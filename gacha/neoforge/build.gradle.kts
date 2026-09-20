plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    silentMojangMappingsLicense()
    runs {
        named("client") { runDir = "runs/client"; client() }
        named("server") { runDir = "runs/server"; server() }
    }
}

val shadowBundle = configurations.create("shadowBundle") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    val localNeoForgeJar = providers.gradleProperty("neoforge_local_jar").orNull
    val localNeoForgeUniversalJar = providers.gradleProperty("neoforge_local_universal_jar").orNull
    val localNeoForgeClientJar = providers.gradleProperty("neoforge_local_client_jar").orNull
    val localNeoForgeLoaderJar = providers.gradleProperty("neoforge_local_loader_jar").orNull
    val localNeoForgeBusJar = providers.gradleProperty("neoforge_local_bus_jar").orNull
    val localNeoForgeDistJar = providers.gradleProperty("neoforge_local_dist_jar").orNull
    val localCobblemonJar = providers.gradleProperty("cobblemon_neoforge_local_jar").orNull
    configurations.maybeCreate("neoForge")
    configurations.maybeCreate("forgeUniversal")
    add("neoForge", localNeoForgeJar?.let { files(it) }
            ?: "net.neoforged:neoforge:${property("neoforge_version")}")
    if (localNeoForgeUniversalJar != null) {
        add("forgeUniversal", files(localNeoForgeUniversalJar))
        compileOnly(files(localNeoForgeUniversalJar))
    }
    if (localNeoForgeClientJar != null) {
        compileOnly(files(localNeoForgeClientJar))
        modCompileOnly(files(localNeoForgeClientJar))
        // Keep Dist available to javac even when this Loom version does not
        // wire modCompileOnly into the NeoForge source set early enough.
        implementation(files(localNeoForgeClientJar))
    }
    if (localNeoForgeLoaderJar != null) compileOnly(files(localNeoForgeLoaderJar))
    if (localNeoForgeBusJar != null) compileOnly(files(localNeoForgeBusJar))
    if (localNeoForgeDistJar != null) compileOnly(files(localNeoForgeDistJar))
    if (localCobblemonJar != null) {
        add("modImplementation", files(localCobblemonJar))
    } else {
        modImplementation("com.cobblemon:neoforge:${property("cobblemon_version")}") { isTransitive = false }
    }

    implementation(project(":common", configuration = "namedElements"))
    "developmentNeoForge"(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowBundle(project(":common", configuration = "transformProductionNeoForge"))
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-neoforge")
        archiveClassifier.set("dev-slim")
    }
    shadowJar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-neoforge")
        archiveClassifier.set("")
        configurations = listOf(shadowBundle)
    }
    remapJar {
        enabled = false
        archiveBaseName.set("${rootProject.property("archives_base_name")}-neoforge")
        archiveVersion.set(rootProject.version.toString())
    }
}
