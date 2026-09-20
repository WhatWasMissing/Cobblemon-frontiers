rootProject.name = "CobblemonSpawnAnnouncements"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

include("common", "neoforge")
if (gradle.startParameter.projectProperties["frontiers_neoforge_only"] != "true") {
    include("fabric")
}
