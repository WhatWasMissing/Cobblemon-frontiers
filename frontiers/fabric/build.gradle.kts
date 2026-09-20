plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
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

val shadowCommon = configurations.create("shadowCommon")

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    // Pass a local Cobblemon Fabric jar when the Maven artifact is unavailable
    // or too large for an offline workspace: -Pcobblemon_fabric_local_jar=...
    val localCobblemonFabricJar = providers.gradleProperty("cobblemon_fabric_local_jar").orNull
    if (localCobblemonFabricJar != null) {
        add("modImplementation", files(localCobblemonFabricJar))
    } else {
        modImplementation("com.cobblemon:fabric:${property("cobblemon_version")}") {
            isTransitive = false
        }
    }

    implementation(project(":common", configuration = "namedElements"))
    "developmentFabric"(project(":common", configuration = "namedElements"))
    shadowCommon(project(":common", configuration = "transformProductionFabric"))
}

tasks {
    jar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-fabric")
        archiveClassifier.set("dev-slim")
    }

    shadowJar {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-fabric")
        archiveClassifier.set("dev-shadow")
        configurations = listOf(shadowCommon)
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("${rootProject.property("archives_base_name")}-fabric")
        archiveVersion.set(rootProject.version.toString())
    }
}
