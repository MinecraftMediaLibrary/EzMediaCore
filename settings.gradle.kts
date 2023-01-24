rootProject.name = "ezmediacore"

include("api", "v1_18_R2", "v1_19_R1", "v1_19_R2", "main", "lib", "deluxemediaplugin")

mapOf(
    "api" to "ezmediacore-api",
    "v1_18_R2" to "v1_18_R2",
    "v1_19_R1" to "v1_19_R1",
    "v1_19_R2" to "v1_19_R2",
    "main" to "ezmediacore",
    "lib" to "ezmediacore-lib",
    "deluxemediaplugin" to "deluxemediaplugin"
).forEach {
    findProject(it.key)?.name = it.value
}
