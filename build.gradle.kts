plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.named<JavaExec>("runClient") {
    workingDir = file("${workingDir}/client")
    doFirst {
        file("${workingDir}/client").mkdirs()
    }
}

tasks.named<JavaExec>("runServer") {
    workingDir = file("${workingDir}/server")
    doFirst {
        file("${workingDir}/server").mkdirs()
    }
}

tasks.named<JavaExec>("runClient17") {
    workingDir = file("${workingDir}/client")
    doFirst {
        file("${workingDir}/client").mkdirs()
    }
}

tasks.named<JavaExec>("runServer17") {
    workingDir = file("${workingDir}/server")
    doFirst {
        file("${workingDir}/server").mkdirs()
    }
}

tasks.named<JavaExec>("runClient21") {
    workingDir = file("${workingDir}/client")
    doFirst {
        file("${workingDir}/client").mkdirs()
    }
}

tasks.named<JavaExec>("runServer21") {
    workingDir = file("${workingDir}/server")
    doFirst {
        file("${workingDir}/server").mkdirs()
    }
}
