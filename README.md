# Gunpowder

Gunpowder aims to provide an all-in-one API for your server modding needs.

## Support

If you find a bug or want to suggest a feature, go to the [Youtrack Page](https://youtrack.martmists.com/issues/Gunpowder)

## License

Gunpowder for 1.18.2 and above is licensed under the BSD-3-Clause license.
Gunpowder for prior versions is licensed under the MIT license.

## Extending

If you want to use Gunpowder, use our [template project](https://github.com/Gunpowder-MC/GunpowderTemplate) or follow the steps below:

Add the following to your build.gradle.kts:
    
```kotlin
dependencies {
    modCompileOnly("io.github.gunpowder:gunpowder:${gunpowder_version}+${minecraft_version}")
    modRuntimeOnly("io.github.gunpowder:gunpowder:${gunpowder_version}+${minecraft_version}:runtime")
}

repositories {
    maven("https://maven.martmists.com/releases")
    maven("https://maven.martmists.com/snapshots")  // For development builds
}

// If you wish to autogenerate a mixins.json:
plugins {
    kotlin("kapt")
}

dependencies {
    kapt("io.github.gunpowder:gunpowder-processor:${gunpowder_version}")
}

kapt {
    arguments {
        // Package where mixins and plugins are located 
        arg("mixin.package", "io.github.gunpowder.mixin")
        
        // all mixins are in package `${mixin.package}.${mixin.name}`, so here `io.github.gunpowder.mixin.base`
        arg("mixin.name", "base")
        
        // We don't have a mixin plugin. Set to true if you do. 
        // Should be located at `${mixin.package}.plugin.${mixin.name.capitalized()}ModulePlugin`, 
        // so here io.github.gunpowder.mixin.base.BaseModulePlugin
        arg("mixin.plugin", "false")
    }
}
```

Create a class extending GunpowderModule (e.g. com.example.ExampleModule) and add it to your fabric.mod.json:

```json
{
    "entrypoints": {
        "gunpowder:module": [
          "com.example.ExampleModule"
        ]  
    }
}
```
