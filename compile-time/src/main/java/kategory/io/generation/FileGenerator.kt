package kategory.io.generation

import com.squareup.kotlinpoet.*
import javax.lang.model.element.Name

/**
 * Generate some Kotlin sample files using KotlinPoet.
 */
class FileGenerator {

    fun createKotlinFile(elementPackage: Name): KotlinFile {
        return KotlinFile.builder(elementPackage.toString(), "TargetClass")
                .addType(TypeSpec.classBuilder("TargetClass")
                        .primaryConstructor(FunSpec.constructorBuilder()
                                .addParameter("name", String::class)
                                .build())
                        .addProperty(PropertySpec.builder("name", String::class)
                                .initializer("name")
                                .build())
                        .addFun(FunSpec.builder("generatedFunction").build())
                        .build())
                .build()
    }
}
