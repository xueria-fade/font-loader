dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/plugin.versions.toml"))
        }
    }
}