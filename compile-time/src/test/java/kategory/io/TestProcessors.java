package kategory.io;

import javax.annotation.processing.Processor;
import java.util.Collections;

final class TestProcessors {
    static Iterable<? extends Processor> processors() {
        return Collections.singletonList(new ImplicitsProcessor());
    }
}
