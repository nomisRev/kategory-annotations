# Implicits Annotation Processor playground
This is just a playground to discover the limitations we have with Kotlin and annotation processors.

### Current status

* We are analyzing `@implicit` annotations in code and generating kotlin files at compile time using [KotlinPoet](https://github.com/square/kotlinpoet).
* We are generating a double version of the annotated class and excluding the original one from compilation using
Gradle, so the user can only reference the generated one when using it.

Tests would still need to be tweaked to mock the sources dir path we are using know, otherwise they crash with a NPE.

### Composition for the time being

The project is composed by different modules like most of the annotation processors out there.
* **annotations:** This one would contain the annotations. Client projects like the **app** one depend on it. Also the
**compiler** module.
* **compile-time:** This is the one containing the processor and any related computations. This dependency could be
fetched just for compile time.
* **app:** Just a sample app module to test annotations on top of classes, fields, methods or whatever.

### How to run

Just `/gradlew clean :app:build` and you should see some fancy texts with different priorities being printed with the
compile time messager.
