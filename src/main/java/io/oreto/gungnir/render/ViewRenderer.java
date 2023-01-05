package io.oreto.gungnir.render;

import io.javalin.rendering.FileRenderer;

/**
 * Provides a common way to define a file view renderer and the valid file type extensions
 */
public interface ViewRenderer {

    FileRenderer getRenderer();
    String[] extensions();
}
