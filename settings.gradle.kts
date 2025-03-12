
pluginManagement {
    repositories {
        mavenLocal()
        maven {
            // RetroFuturaGradle
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroup("com.gtnewhorizons")
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("com.gtnewhorizons.gtnhsettingsconvention") version ("1.0.37")
}
