plugins {
    id("java")
    id("com.gradleup.shadow")
}
val paperApiVersion = providers.gradleProperty("paperApiVersion").get()
dependencies {
    implementation(project(":plugin-core"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}
tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand("version" to project.version) }
}
tasks.jar { enabled = false }
tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("OrzMCPluginTemplate")
}
tasks.assemble { dependsOn(tasks.shadowJar) }
