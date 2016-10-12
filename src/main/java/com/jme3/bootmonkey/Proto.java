package com.jme3.bootmonkey;

import java.util.*;

/**
 * Created by bouquet on 12/10/16.
 */
public class Proto {

    public static void main(String... argv) {

        Map<String,String> params = new HashMap<>();
        params.put("packageName", "org.myorg.mygame");
        params.put("jmeVersion", "[3.1,)");
        params.put("baseDir", "e:/JME/");
        params.put("projectName", "baseJME");
        params.put("templateUrl", "https://github.com/Nehon/base-jme.git");

        ProjectGenerator generator = new ProjectGenerator();
        generator.addGenerationListener(System.out::println);

        List<String> errors = generator.generate(params);
        for (String error : errors) {
            System.err.println(error);
        }

    }

}
