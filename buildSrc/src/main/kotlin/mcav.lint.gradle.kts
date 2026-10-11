import me.brandonli.mcav.gradle.LogMessagesTask
import me.brandonli.mcav.gradle.QualifiedNamesTask
import me.brandonli.mcav.gradle.VariableNamesTask

plugins {
    base
}

val javaSources = layout.projectDirectory.dir("src").asFileTree.matching { include("*/java/**/*.java") }

val qualifiedNames = tasks.register<QualifiedNamesTask>("qualifiedNames") {
    group = "verification"
    description = "Fails on fully qualified type names in the Java code of every source set of this module"
    sources.from(javaSources)
    report = layout.buildDirectory.file("reports/qualified-names.txt")
}

val logMessages = tasks.register<LogMessagesTask>("logMessages") {
    group = "verification"
    description = "Fails on log calls whose message is not a constant in the Java code of every source set of this module"
    sources.from(javaSources)
    report = layout.buildDirectory.file("reports/log-messages.txt")
}

val variableNames = tasks.register<VariableNamesTask>("variableNames") {
    group = "verification"
    description = "Fails on one-letter and abbreviated variable names in the Java code of every source set of this module"
    sources.from(javaSources)
    report = layout.buildDirectory.file("reports/variable-names.txt")
}

tasks.check {
    dependsOn(qualifiedNames, logMessages, variableNames)
}
