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
        named("client") {
            runDir = "runs/client"
            client()
        }
        named("server") {
            runDir = "runs/server"
            server()
        }
    }
}

val shadowBundle = configurations.create("shadowBundle") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    minecraft("net.minecraft:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    // Pass local jars from an installed client to avoid waiting on a remote
    // artifact download: -Pneoforge_local_jar=... -Pneoforge_local_universal_jar=...
    // -Pneoforge_local_loader_jar=... -Pneoforge_local_bus_jar=...
    // -Pneoforge_local_dist_jar=...
    // -Pcobblemon_neoforge_local_jar=...
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
        // Loom keeps loader-side compile-only jars on modCompileOnly so the
        // platform classes are available to the source set without being
        // bundled into the distributable mod.
        compileOnly(files(localNeoForgeClientJar))
        modCompileOnly(files(localNeoForgeClientJar))
        // Some Architectury Loom versions do not expose modCompileOnly on the
        // NeoForge compile classpath until after source-set wiring. Keep the
        // client API available to javac as a normal external dependency; the
        // jar is never included in shadowBundle or the final mod jar.
        implementation(files(localNeoForgeClientJar))
    }
    if (localNeoForgeLoaderJar != null) {
        compileOnly(files(localNeoForgeLoaderJar))
    }
    if (localNeoForgeBusJar != null) {
        compileOnly(files(localNeoForgeBusJar))
    }
    if (localNeoForgeDistJar != null) {
        compileOnly(files(localNeoForgeDistJar))
    }
    if (localCobblemonJar != null) {
        add("modImplementation", files(localCobblemonJar))
    } else {
        modImplementation("com.cobblemon:neoforge:${property("cobblemon_version")}") {
            isTransitive = false
        }
    }

    implementation(project(":common", configuration = "namedElements"))
    "developmentNeoForge"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowBundle(project(":common", configuration = "transformProductionNeoForge"))
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-neoforge")
        archiveClassifier.set("dev-slim")
    }

    shadowJar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-neoforge")
        // The local NeoForge userdev workaround supplies Mojang-mapped classes.
        // Keep that transformed common jar as the distributable artifact; the
        // generic Loom remapJar path otherwise rewrites it to Fabric intermediary
        // names (for example net.minecraft.class_7923), which NeoForge cannot load.
        archiveClassifier.set("")
        configurations = listOf(shadowBundle)
    }

    remapJar {
        // Disabled for the local NeoForge userdev path; shadowJar is already
        // in the names used by the NeoForge 21.1.247 runtime.
        enabled = false
        archiveBaseName.set("${rootProject.property("archives_base_name")}-neoforge")
        archiveVersion.set(rootProject.version.toString())
    }
}
