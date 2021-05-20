rootProject.name = "minecraftmedialibrary-root"

include(":api")

include(":v1_16_R3")
include(":v1_16_R2")
include(":v1_16_R1")
include(":v1_15_R1")
include(":v1_14_R1")
include(":v1_13_R2")
//include(":v1_13_R1")
include(":v1_12_R1")
//include(":v1_11_R1")
//include(":v1_10_R1")
include(":v1_8_R3")
//include(":v1_9_R2")
//include(":v1_9_R1")
//include(":v1_8_R2")
//include(":v1_8_R1")

include(":main")
include(":lib")
include(":deluxemediaplugin")

findProject("main")?.name = "minecraftmedialibrary"
findProject("api")?.name = "minecraftmedialibrary-api"
findProject("lib")?.name = "minecraftmedialibrary-lib"