rootProject.name = "ezmediacore"

include(
    ":ezmediacore:api",
    ":ezmediacore:v1_18_R2",
    ":ezmediacore:v1_19_R1",
    ":ezmediacore:v1_19_R2",
    ":ezmediacore:main",
    ":ezmediacore:lib",
    "deluxemediaplugin",
    "deluxewedisplays")

mapOf(
    "api" to "ezmediacore-api",
    "v1_18_R2" to "v1_18_R2",
    "v1_19_R1" to "v1_19_R1",
    "v1_19_R2" to "v1_19_R2",
    "main" to "ezmediacore",
    "lib" to "ezmediacore-lib",
    "deluxemediaplugin" to "deluxemediaplugin",
    "deluxewedisplays" to "deluxewedisplays"
).forEach {
    findProject(it.key)?.name = it.value
}
include("deluxewebdisplays")
