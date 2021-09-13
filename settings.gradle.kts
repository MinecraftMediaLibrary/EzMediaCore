rootProject.name = "ezmediacore"

include("api")
include("v1_17_R1")
include("v1_16_R3")
include("main")
include("lib")
include("deluxemediaplugin")

findProject("api")?.name = "ezmediacore-api"
findProject("v1_17_R1")?.name = "v1_17_R1"
findProject("v1_16_R3")?.name = "v1_16_R3"
findProject("main")?.name = "ezmediacore"
findProject("lib")?.name = "ezmediacore-lib"
findProject("deluxemediaplugin")?.name = "deluxemediaplugin"

