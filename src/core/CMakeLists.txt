file(GLOB CoreSource "*.h" "*.cpp")

add_library(Core
    ${CoreSource}
)

if (APPLE)
    #set(CMAKE_APPLE_SILICON_PROCESSOR "x86_64")
    set(CMAKE_APPLE_SILICON_PROCESSOR "arm64")
endif()

target_link_libraries(Core PUBLIC
    cpr::cpr
    nlohmann_json::nlohmann_json
    json-c
    archive
)
