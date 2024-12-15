plugins {
    id("com.android.application")
}

android {
    namespace = "com.jeffrey.fypweatherapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jeffrey.fypweatherapp"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation(files("libs\\gson-2.4.jar"))
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("org.tensorflow:tensorflow-lite:2.12.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.0")
    implementation ("org.tensorflow:tensorflow-lite-select-tf-ops:2.12.0")


}