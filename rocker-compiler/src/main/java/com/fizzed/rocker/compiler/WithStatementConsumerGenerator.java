package com.fizzed.rocker.compiler;

import com.fizzed.rocker.model.WithStatement;
import com.fizzed.rocker.runtime.WithBlock;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

import static com.fizzed.rocker.compiler.JavaGenerator.CRLF;

/**
 * We use this class to generate a withblock consumer while generating the template.
 * Since we allow 1 or more with assignments, we cannot rely on a static consumer anymore.
 * The JavaParser gathers the relevant WithBlocks it comes across, and at the end of the
 * template we generate the relevant consumers using this helper class.
 *
 * @author mreuvers
 */
public class WithStatementConsumerGenerator {

    protected static final String WITH_BLOCKS_GENERATED_CLASS_NAME = "WithBlocksGenerated0";

    // Each number registered represents the consumer we must generate, where
    // the number represents the number of types.
    private final Set<Integer> withStatementsTypeCounts = new TreeSet<>();

    public String register(final WithStatement statement) {
        final int typeCount = statement.getVariables().size();
        if(typeCount > 1) {
            final String className = "WithStatementConsumer" + typeCount;

            withStatementsTypeCounts.add(typeCount);
            return WITH_BLOCKS_GENERATED_CLASS_NAME + "." + className;
        }
        return RockerUtil.qualifiedClassName(WithBlock.Consumer1.class);
    }

    public void generate(final JavaGenerator generator, final Writer w) throws IOException {
        if (withStatementsTypeCounts.isEmpty()) {
            return;
        }

        generator.tab(w, 1).append("private static class ").append(WITH_BLOCKS_GENERATED_CLASS_NAME).append(" { ").append(CRLF);

        for (final Integer typeCount : withStatementsTypeCounts) {
            final String className = "WithStatementConsumer" + typeCount;

            // Types it accepts.
            generator.tab(w, 2)
                .append("interface ").append(className).append('<');
            for (int i = 0; i < typeCount; i++) {
                if (i > 0) {
                    w.append(", ");
                }
                w.append("V" + i);
            }
            w.append('>').append(" { ").append(CRLF).append(CRLF);
            generator.tab(w, 3)
                .append("void accept(");
            // Variables for the accept
            for (int i = 0; i < typeCount; i++) {
                if (i > 0) {
                    w.append(", ");
                }
                w.append("V" + i).append(" v" + i);
            }
            w.append(") throws IOException;").append(CRLF).append(CRLF);
            generator.tab(w, 2)
                .append('}').append(CRLF);
        }

        // Generate the static with(..) methods
        for (final Integer typeCount : withStatementsTypeCounts) {
            final String className = "WithStatementConsumer" + typeCount;

            generator.tab(w, 2)
                .append("static public <");
            for (int i = 0; i < typeCount; i++) {
                if (i > 0) {
                    w.append(", ");
                }
                w.append("V" + i);
            }
            w.append("> void with (");
            for (int i = 0; i < typeCount; i++) {
                if (i > 0) {
                    w.append(", ");
                }
                w.append("V" + i).append(" v" + i);
            }
            w.append(", boolean nullSafe, ").append(className).append(" consumer) throws IOException {").append(CRLF);
            generator.tab(w, 3)
                .append("consumer.accept(");
                for (int i = 0; i < typeCount; i++) {
                    if (i > 0) {
                        w.append(", ");
                    }
                    w.append("v" + i);
                }
                w.append(");").append(CRLF);
            generator.tab(w, 2)
                .append("}").append(CRLF);
        }

        generator.tab(w, 1).append("}").append(CRLF);
    }
}

/*
    public interface Consumer0 {
        void accept() throws IOException;
    }

    public interface Consumer1<V> {
        void accept(V v) throws IOException;
    }

    static public <V> void with(V v, boolean nullSafe, Consumer1<V> consumer) throws IOException {
        if (!nullSafe || v != null) {
            consumer.accept(v);
        }
    }

    static public <V> void with(V v, boolean nullSafe, Consumer1<V> consumer, Consumer0 elseConsumer) throws IOException {
        if (!nullSafe || v != null) {
            consumer.accept(v);
        } else {
            elseConsumer.accept();
        }
    }
 */