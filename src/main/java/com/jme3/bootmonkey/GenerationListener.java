package com.jme3.bootmonkey;

/**
 * Created by Nehon on 12/10/2016.
 */
@FunctionalInterface
public interface GenerationListener {
    void progress(String newState);
}
