plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.parcelize)
    alias(libs.plugins.gms.services)
}

android {
    namespace = "com.example.pizzastoreadmin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pizzastoreadmin"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val webClientId = property("web_client_id")?.toString() ?: error(
            "You should set webClientId"
        )

        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
    }

    buildTypes {
        release {
            // isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes += setOf(
                    "META-INF/LICENSE",
                    "META-INF/LICENSE-FIREBASE.txt",
                    "META-INF/NOTICE"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.logging)
    implementation(libs.mvikotlin.coroutines)

    implementation(libs.androidx.appcompat)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)

    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.credentialsx)
    implementation(libs.googleid)
    implementation(libs.identity)

    implementation(libs.google.services)
    implementation(libs.play.auth)

    implementation(libs.jet.nav)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.coil.compose)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ui.test.junit4)

    implementation(libs.gson)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.log)

    implementation(libs.androidx.room.runtime)
    implementation(libs.room.core)
    ksp(libs.room.compile)

    implementation(libs.dagger.core)
    ksp(libs.dagger.compiler)
    implementation(libs.glide.compose)

    implementation(libs.icons)
}
