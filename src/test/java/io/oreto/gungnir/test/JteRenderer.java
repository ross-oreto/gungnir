package io.oreto.gungnir.test;

import com.typesafe.config.Config;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;
import io.javalin.rendering.FileRenderer;
import io.oreto.gungnir.render.ViewRenderer;
import java.util.List;
import java.util.Map;

public class JteRenderer implements ViewRenderer {
    public static final String[] DEFAULT_EXTENSIONS = new String[] { ".jte" };
    private final TemplateEngine templateEngine;
    private final String[] extensions;

    public JteRenderer(String path, List<String> extensions) {
        // This is the directory where your .jte files are located.
        CodeResolver codeResolver = new ResourceCodeResolver(path);

        // create template engine
        templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        this.extensions = extensions == null ? DEFAULT_EXTENSIONS : extensions.toArray(new String[0]);
    }

    public JteRenderer(Config config) {
       this(config.getString("views.path"), config.getStringList("views.extensions"));
    }

    public JteRenderer() {
        this("views", null);
    }

    @SuppressWarnings("unchecked")
    public FileRenderer getRenderer() {
        return (filePath, model, ctx) -> {
            StringOutput output = new StringOutput();
            templateEngine.render(filePath
                    , (Map<String, Object>) model
                    , output);
            return output.toString();
        };
    }

    public String[] extensions() {
        return extensions;
    }
}
