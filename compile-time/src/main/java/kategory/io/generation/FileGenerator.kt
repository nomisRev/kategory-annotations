package kategory.io.generation

import com.squareup.kotlinpoet.*

/**
 * Generate some Kotlin sample files using KotlinPoet.
 */
class FileGenerator {

    fun createKotlinFile(): KotlinFile {
        return KotlinFile.builder("kategory.io", "HelloWorld")
                .addType(TypeSpec.classBuilder("HelloWorld")
                        .primaryConstructor(FunSpec.constructorBuilder()
                                .addParameter("name", String::class)
                                .build())
                        .addProperty(PropertySpec.builder("name", String::class)
                                .initializer("name")
                                .build())
                        .build())
                .build()
    }
}
