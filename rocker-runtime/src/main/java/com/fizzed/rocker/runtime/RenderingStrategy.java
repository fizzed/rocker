package com.fizzed.rocker.runtime;

import java.io.IOException;
import com.fizzed.rocker.RenderingException;

public interface RenderingStrategy {
    void render(DefaultRockerTemplate template) throws RenderingException, IOException;
}
