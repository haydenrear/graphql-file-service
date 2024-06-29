plugins {
    id("com.hayden.spring")
    id("com.hayden.observable-app")
    id("com.hayden.discovery-app")
    id("com.hayden.graphql")
    id("com.hayden.java-conventions")
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

tasks.withType<JavaExec> {
    dependsOn("copyAgent")
    dependsOn("dynamicTracingAgent")
    jvmArgs(
        "-javaagent:build/agent/opentelemetry-javaagent.jar",
        "-Dotel.javaagent.configuration-file=src/main/resources/otel/otel.properties",
        "-javaagent:build/dynamic_agent/tracing_agent.jar"
    )
}

