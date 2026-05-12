rootProject.name = "ArcartXSuite"

include("axs-api")
include("axs-core")

// ─── 可插拔模块 ───
include("modules:announcer")
include("modules:entitytracker")
include("modules:chat")
include("modules:conversation")
include("modules:eventpacket")
include("modules:combateffect")
include("modules:loginview")
include("modules:mail")
include("modules:map")
include("modules:onlinerewards")
include("modules:pickup")
include("modules:prop")
include("modules:questgps")
include("modules:rgb")
include("modules:tab")
include("modules:title")
include("modules:warehouse")
