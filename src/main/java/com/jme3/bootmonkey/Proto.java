package com.jme3.bootmonkey;

import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.*;

import static com.jme3.bootmonkey.GitUtils.getRemoteBranches;

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

//        ProjectGenerator generator = new ProjectGenerator();
//        generator.addGenerationListener((text , errors) -> {
//            for (String error : errors) {
//                System.err.println(error);
//            }
//            System.out.println(text);
//        });

        try {
           List<String> list = GitUtils.getRemoteBranches("https://github.com/Nehon/base-jme.git");
            list.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }


        //generator.generate(params);


    }

}
