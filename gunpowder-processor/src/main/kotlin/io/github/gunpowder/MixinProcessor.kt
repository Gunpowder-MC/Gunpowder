package io.github.gunpowder

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auto.service.AutoService
import org.spongepowered.asm.mixin.Mixin
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

@AutoService(Processor::class)
@SupportedAnnotationTypes("org.spongepowered.asm.mixin.Mixin")
class MixinProcessor : AbstractProcessor() {
    private val mapper = ObjectMapper()

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_17
    }

    override fun getSupportedOptions(): MutableSet<String> {
        return mutableSetOf(
            MIXIN_NAME_OPTION,
            MIXIN_PACKAGE_OPTION,
            MIXIN_PLUGIN_OPTION
        )
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver()) {
            return true
        }

        val mixinPackage = this.processingEnv.options[MIXIN_PACKAGE_OPTION] ?: "io.github.gunpowder.mixin"
        val mixinName = this.processingEnv.options[MIXIN_NAME_OPTION] ?: "NAME_NOT_SET"
        val mixinPlugin = (this.processingEnv.options[MIXIN_PLUGIN_OPTION] ?: "false").toBooleanStrict()
        val elements = roundEnv.getElementsAnnotatedWith(Mixin::class.java)
        val resource = processingEnv.filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            "resources",
            "mixins.$mixinName.gunpowder.json",
            *elements.toTypedArray()
        )

        val attrs = mutableMapOf(
            "required" to true,
            "package" to "$mixinPackage.$mixinName",
            "compatibilityLevel" to "JAVA_8",
            "mixinPriority" to 9999,
            "mixins" to elements.map { it.simpleName.toString() },
            "injectors" to mapOf(
                "defaultRequire" to 1,
            ),
        )

        if (mixinPlugin) {
            attrs["plugin"] = "$mixinPackage.plugin.${
                mixinName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
            }ModulePlugin"
        }

        resource.openOutputStream().use { os ->
            mapper.writeValue(os, attrs)
        }
        return true
    }

    companion object {
        private const val MIXIN_PACKAGE_OPTION = "mixin.package"
        private const val MIXIN_NAME_OPTION = "mixin.name"
        private const val MIXIN_PLUGIN_OPTION = "mixin.plugin"
    }
}
