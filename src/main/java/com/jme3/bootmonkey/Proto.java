package com.jme3.bootmonkey;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by bouquet on 12/10/16.
 */
public class Proto {

    private static String templateUrl = "https://github.com/Nehon/base-jme.git";
    private static String projectName = "baseJME";
    private static String baseDir = "/Users/bouquet/";
    private static String packageName = "org.myorg.mygame";
    private static String jmeVersion = "[3.1,)";

    private static Properties buildReplacement = new Properties();
    static{
        buildReplacement.put("jcenter\\(\\)", "jcenter {\n" +
                "        url \"http://jcenter.bintray.com/\"\n" +
                "    }");

        buildReplacement.put("mainClassName='mygame.Main'","mainClassName='" + packageName + ".Main'");

        buildReplacement.put("ext.jmeVersion\\s*=\\s*.*","ext.jmeVersion = '" + jmeVersion + "'");

    }


    private static Properties mainReplacement = new Properties();
    static{

        mainReplacement.put("package mygame;","package " + packageName + ";");

    }

    private static String tmpContent;


    public static void main(String... argv) {
        try {
            Git.cloneRepository().setURI(templateUrl)
                    .setDirectory(new File(baseDir + projectName)).call();

            adaptFile("/build.gradle", buildReplacement);
            adaptFile("/src/main/java/mygame/Main.java", mainReplacement);

            FileUtils.moveFile(new File(baseDir + projectName +"/src/main/java/mygame/Main.java"), new File(baseDir + projectName +"/src/main/java/"+ packageName.replaceAll("\\.", "/") +"/Main.java"));
            //delete .git directory
            FileUtils.deleteDirectory(new File(baseDir + projectName + "/.git"));
            FileUtils.deleteDirectory(new File(baseDir + projectName + "/src/main/java/mygame"));
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void adaptFile(String fileName, Properties replacement) throws IOException {
        tmpContent = FileUtils.readFileToString(new File(baseDir + projectName + fileName), "UTF-8");

        replacement.forEach((key, value) -> tmpContent = tmpContent.replaceAll((String)key, (String)value));

        System.err.println(tmpContent);

        FileUtils.writeStringToFile(new File(baseDir + projectName + fileName), tmpContent, "UTF-8");
    }
}
