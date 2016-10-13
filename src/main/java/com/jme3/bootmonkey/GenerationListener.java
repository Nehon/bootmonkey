package com.jme3.bootmonkey;

import java.util.List;

/**
 * Created by Nehon on 12/10/2016.
 */
@FunctionalInterface
public interface GenerationListener {
    void progress(String newState, List<String> errors);
}
