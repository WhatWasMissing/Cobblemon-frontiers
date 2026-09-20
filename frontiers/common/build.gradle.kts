plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    common("fabric", "neoforge")
}

loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("com.cobblemon:mod:${property("cobblemon_version")}") {
        isTransitive = false
    }
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")
}
