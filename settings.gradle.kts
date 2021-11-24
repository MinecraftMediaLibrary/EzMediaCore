rootProject.name = "ezmediacore"

include("api", "v1_18_R1", "v1_17_R1", "v1_16_R3", "main", "lib", "deluxemediaplugin")

mapOf(
    "api" to "ezmediacore-api",
    "v1_18_R1" to "v1_18_R1",
    "v1_17_R1" to "v1_17_R1",
    "v1_16_R3" to "v1_16_R3",
    "main" to "ezmediacore",
    "lib" to "ezmediacore-lib",
    "deluxemediaplugin" to "deluxemediaplugin"
).forEach {
    findProject(it.key)?.name = it.value
}
include("v1_18_R1")
