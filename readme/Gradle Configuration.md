#Gradle Configuration

BankTemplate customized a file  [./config.gradle](../config.gradle)  that can be referenced by other gradle. Via a, the project can unify the libraries versions of each module.



## SDK Version

in [./config.gradle](../config.gradle) , modify the SDKs version，they will be used for every module.

```groovy
ext {
    versions =[
            compileSdkVersion   : 33,
            minSdkVersion       : 24,
            targetSdkVersion    : 31,
    ]
    ...
}
```

In the modules, they are used in this way:

```groovy
...
android {
    compileSdk versions.compileSdkVersion

    defaultConfig {
        minSdk versions.minSdkVersion
        targetSdk versions.targetSdkVersion
    }
    ...
}
```



## Libraries Dependencies 

### Maven libraries

If you want to modify the maven libraries, you just only modify as follow in [./config.gradle](../config.gradle) , they will be used for every module.

```groovy
ext {
    /*androidx*/
    androidx = [
            'room': 'androidx.room:room-runtime:2.5.1',
            'room-compiler': 'androidx.room:room-compiler:2.5.1',
         	'room-testing': 'androidx.room:room-testing:2.5.1',
        	...
    ]
	...
}
```

In the modules, they are used in this way:

```groovy
...
dependencies {
   implementation androidx['room']
    annotationProcessor androidx['room-compiler']
    implementation androidx['room-testing']
}
```

### Local AAR or JAR

Because [app/build.gradle](../app/build.gradle) will import all aar and jar files in [app/libs](../app/libs), you just only  replace/add/remove the files in the libs.

```groovy
..
dependencies {
    implementation fileTree(include: ['*.jar', '*.aar'], dir: 'libs')
    ...
}
...

```

Then, you can import them in other modules:

```groovy
...
dependencies {
    compileOnly fileTree(dir: "../app/libs", include: ["*.aar"])
    compileOnly fileTree(dir: "../app/libs", include: ["*.jar"])
    or
    compileOnly files("../app/libs/xxx.aar")
}
...
```



## Customized Generated APK Name

In [app/build.gradle](../app/build.gradle), you can customized the apk name.

```groovy
...
android {
    ...
    android.applicationVariants.all { variant ->
        variant.outputs.all {
            //project name
            def project = project.getParent().getName()
            //debug/release
            def buildType = buildType.name
            //current time. YYYYMMDDhhmm
            def time = releaseTime()
            //apk name, such as NSDK-BANKTEMPLATE-R3-1-npi-an-3.6-alpha10-debug-202209060848
            def apkName = project + "-" + versionCode + "-" + versionName + "-" + buildType + "-" + time
            if (variant.productFlavors != null && variant.productFlavors.size() > 0) {
                //Add flavor name if use product flavors
                apkName = apkName + "-" + variant.productFlavors[0].name
            }
            outputFileName = apkName.toUpperCase() + ".apk"
        }
    }
}
...
def static releaseTime() {
    return new Date().format("YYYYMMddHHmm")
}
```



## Signing Configs

[newland_debug.keystore](../newland_debug.keystore) is the default signature certificate of the apk. You can replace it with yours.

It's used in [app/build.gradle](../app/build.gradle).

```groovy
...
android {
    ...
    //newland signing configs
    signingConfigs {
        mySignature {
            keyAlias 'androiddebugkey'
            keyPassword 'android'
            storeFile rootProject.file('newland_debug.keystore')
            storePassword 'android'
            v1SigningEnabled true
            v2SigningEnabled true
        }
    }
    buildTypes {
        release {
//            minifyEnabled true
//            shrinkResources true
//            //proguard file
//            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            //signature file
            signingConfig signingConfigs.mySignature
        }
        debug {
            //signature file
            signingConfig signingConfigs.mySignature
        }

    }
    ...
}
```



## Namespace

Android Studio requires replacing the element `package of AndroidManifest.xml` with `namespace` in every module. It will be used as the default prefix package name for `AndroidManifest.xml.`

Defined as follows:

```groovy
...
android {
    namespace 'acquire.sdk'
    ...
}

```

