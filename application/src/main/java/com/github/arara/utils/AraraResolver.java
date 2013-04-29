package com.github.arara.utils;

// needed import
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 * Extends a Resolver class to disable mapping of numeric times, as arara works
 * better with text.
 *
 * @since 1.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraResolver extends Resolver {

    /**
     * {@inheritDoc}
     *
     * Adds the implicit resolvers to the Yaml mechanism.
     */
    @Override
    protected void addImplicitResolvers() {

        // for merges
        addImplicitResolver(Tag.MERGE, MERGE, "<");

        // for null values
        addImplicitResolver(Tag.NULL, NULL, "~nN\0");

        // for empty values
        addImplicitResolver(Tag.NULL, EMPTY, null);

        // for values
        addImplicitResolver(Tag.VALUE, VALUE, "=");
    }
}
