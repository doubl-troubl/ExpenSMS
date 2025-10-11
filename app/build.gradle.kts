plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.ksp)
}

// Custom task for running CLI without conflicting with Android plugin
tasks.register<JavaExec>("runCli") {
    group = "application"
    description = "Run the SMS Parser CLI"

    dependsOn("compileDebugKotlin")

    // Use the compiled classes and runtime classpath
    classpath =
        files(
            "${project.buildDir}/tmp/kotlin-classes/debug",
            configurations["debugRuntimeClasspath"],
        )

    mainClass.set("com.dagimg.expensms.SmsParserCLIKt")

    // Pass command line arguments from --args
    val argsString = project.properties["args"] as? String ?: ""
    if (argsString.isNotEmpty()) {
        args(*argsString.split(" ").toTypedArray())
    }
}

android {
    namespace = "com.dagimg.expensms"
    compileSdk = 36

    // Signing configuration for release builds
    signingConfigs {
        create("release") {
            storeFile = file("../expensms.jks")
            storePassword = project.properties["STORE_PASSWORD"]?.toString()
                ?: System.getenv("STORE_PASSWORD")
                ?: "default_password"
            keyAlias = project.properties["KEY_ALIAS"]?.toString()
                ?: System.getenv("KEY_ALIAS")
                ?: "default_alias"
            keyPassword = project.properties["KEY_PASSWORD"]?.toString()
                ?: System.getenv("KEY_PASSWORD")
                ?: "default_password"
        }
    }

    defaultConfig {
        applicationId = "com.dagimg.expensms"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs["release"]
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.kotlinx.serialization.json)

    // Room dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // Compose dependencies
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
}
