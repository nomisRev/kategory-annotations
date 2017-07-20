# IMPLICITS ANNOTATION PROCESSOR PLAYGROUND
This is just a playground to discover the limitations we have with Kotlin and annotation processors.

### Composition for the time being

The project is composed by different modules like most of the annotation processors out there.
* **annotations:** This one would contain the annotations. Client projects like the **app** one depend on it. Also the
**compiler** module.
* **compile-time:** This is the one containing the processor and any related computations. This dependency could be
fetched just for compile time.
* **app:** Just a sample app module to test annotations on top of classes or whatever.
