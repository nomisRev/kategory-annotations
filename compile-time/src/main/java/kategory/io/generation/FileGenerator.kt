package kategory.io.generation

import com.squareup.kotlinpoet.*

/**
 * Generate some Kotlin sample files using KotlinPoet.
 */
class FileGenerator {

    fun createKotlinFile(): KotlinFile {
        val greeterClass = ClassName("", "Greeter")

        return KotlinFile.builder("", "HelloWorld")
                .addType(TypeSpec.classBuilder("Greeter")
                        .primaryConstructor(FunSpec.constructorBuilder()
                                .addParameter("name", String::class)
                                .build())
                        .addProperty(PropertySpec.builder("name", String::class)
                                .initializer("name")
                                .build())
                        .addFun(FunSpec.builder("greet")
                                .addStatement("println(%S)", "Hello, \$name")
                                .build())
                        .build())
                .addFun(FunSpec.builder("main")
                        .addParameter("args", String::class, KModifier.VARARG)
                        .addStatement("%T(args[0]).greet()", greeterClass)
                        .build())
                .build()
    }
}
