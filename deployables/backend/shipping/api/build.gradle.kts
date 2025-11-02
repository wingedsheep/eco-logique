plugins {
    id("economique.kotlin-common")
}

dependencies {
    api(project(":deployables:backend:domain:products:api"))
    api(project(":deployables:backend:domain:inventory:api"))
    api(project(":common:common-money"))
}
