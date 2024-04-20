plugins {
    id("com.hayden.spring")
    id("com.hayden.observable-app")
    id("com.hayden.persistence")
    id("com.hayden.discovery-app")
    id("com.hayden.graphql")
}

group = "com.hayden"
version = "0.0.1-SNAPSHOT"
description = "file-service"


dependencies {
    implementation(project(":utilitymodule"))
    implementation(project(":shared"))
    implementation(project(":message-broker"))
    implementation(project(":graphql"))
}

tasks.generateJava {
    schemaPaths.add("${projectDir}/src/main/resources/schema")
    packageName = "com.hayden.fileservice.codegen"
    generateClient = true
    typeMapping = mutableMapOf(
        Pair("ByteArray", "com.hayden.fileservice.config.ByteArray")
    )
}

tasks.register("prepareKotlinBuildScriptModel") {}

