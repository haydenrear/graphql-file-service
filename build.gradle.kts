plugins {
    id("com.hayden.graphql-data-service")
    id("com.hayden.discovery-app")
}

group = "com.hayden"
version = "0.0.1-SNAPSHOT"
description = "file-service"

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
