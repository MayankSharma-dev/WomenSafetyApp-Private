pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //Tom Tom map
        maven {
            url = uri("https://repositories.tomtom.com/artifactory/maven")
        }
    }
}

rootProject.name = "Women Safety App"
include(":app")
 