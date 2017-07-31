package kategory.io

val implicitAnnotationKClass = implicit::class
val implicitAnnotationClass = implicitAnnotationKClass.java
val implicitAnnotationName = "@" + implicitAnnotationClass.simpleName
