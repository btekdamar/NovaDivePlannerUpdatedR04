// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt) apply false
    // Safe Args plugin'ini buraya ekleyin, eğer modül seviyesinde zaten varsa ve sorun çıkarıyorsa kaldırın.
    // Genellikle modül seviyesinde olması yeterlidir.
    // alias(libs.plugins.navigationsafeargs) apply false // Eğer version catalog'da tanımlıysa
}

// buildscript bloğunu Kotlin DSL'e uygun hale getirelim veya
// bu bağımlılığı settings.gradle.kts'e taşıyalım.
// Şimdilik Kotlin DSL'e uygun hale getirmeyi deneyelim:
buildscript {
    repositories { // repositories bloğu eksik olabilir
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.navigation.safe.args.gradle.plugin) // classpath metot çağrısı
    }
}