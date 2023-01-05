package io.oreto.gungnir.render;

import io.javalin.rendering.FileRenderer;

public interface ViewRenderer {

    FileRenderer getRenderer();
    String[] extensions();
}
