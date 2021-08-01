rootProject.name = "epicmedialib"

include("api")
include("v1_17_R1")
include("v1_16_R3")
include("main")
include("lib")
include("deluxemediaplugin")

findProject("api")?.name = "epicmedialib-api"
findProject("main")?.name = "epicmedialib"
findProject("lib")?.name = "epicmedialib-lib"
findProject("deluxemediaplugin")?.name = "deluxemediaplugin"

