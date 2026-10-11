plugins {
    id("mcav.http")
}

dependencies {
    api(platform(libs.jackson.bom))
    api(libs.bundles.spring.boot.web) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    api(libs.tomcat.embed.core) {
        exclude(group = "org.apache.tomcat", module = "tomcat-annotations-api")
    }
    api(libs.tomcat.embed.el)
    api(libs.tomcat.embed.websocket) {
        exclude(group = "org.apache.tomcat", module = "tomcat-annotations-api")
    }
    api(libs.jackson.core)
    api(libs.jackson.databind)
    compileOnlyApi(project(":mcav-common"))
    testImplementation(project(":mcav-common"))
    testImplementation(libs.slf4j.simple)
}
