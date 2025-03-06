
pluginManagement {
    repositories {
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
        mavenLocal()
        maven { url = uri("https://maven.aliyun.com/repository/public/")}
        maven { url = uri("https://maven.aliyun.com/repository/spring/")}
        maven { url = uri("https://maven.aliyun.com/repository/google/")}
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin/")}
        maven { url = uri("https://maven.aliyun.com/repository/spring-plugin/")}
        maven { url = uri("https://maven.aliyun.com/repository/grails-core/")}
        maven { url = uri("https://maven.aliyun.com/repository/apache-snapshots/")}
    }
}

plugins {
    id("com.gtnewhorizons.gtnhsettingsconvention") version ("1.0.36")
}
