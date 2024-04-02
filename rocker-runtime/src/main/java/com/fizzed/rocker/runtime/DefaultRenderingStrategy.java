package com.fizzed.rocker.runtime;

import java.io.IOException;
import com.fizzed.rocker.RenderingException;

public class DefaultRenderingStrategy implements RenderingStrategy {
    @Override
    public void render(DefaultRockerTemplate template) throws RenderingException, IOException {
        template.__doRender(); // Calls the existing rendering logic
    }
}
