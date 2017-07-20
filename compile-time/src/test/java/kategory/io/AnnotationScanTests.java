package kategory.io;

import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static kategory.io.TestProcessors.processors;

public class AnnotationScanTests {

    @Test
    public void addJobTest() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n')
                .join("package test;",
                        "import kategory.io.implicit;",
                        "public class Test {",
                        "public static String testImplicits(@implicit String a) { return a; }",
                        "}"));

        //Truth assertion
        Truth.ASSERT.about(javaSource())
                .that(source)
                .processedWith(processors())
                .compilesWithoutError();
    }
}
