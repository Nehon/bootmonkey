package com.jme3.bootmonkey;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * Created by Nehon on 12/10/2016.
 * Very Basic project scaffolding tool for JME3.
 * 1. Clones a template repository into a local folder
 * 2. attempt to change lines in project files as described in the replacement.yml
 *      the file must be of that structure:
 *      [filePath]:
 *          [search1]: [replacement1]
 *          [search2]: [replacement2]
 *          ...
 *  - filePath is the path to the file to modify from the project root.
 *  - searchN is a regular expression to search in the file
 *  - replacementN is the text that will replace the regexp match. You can use template variables in this replacement:
 *      - templateUrl: the project template git repo url.
 *      - baseDir: the directory where to create the repo.
 *      - projectName: the project name.
 *      - packageName: the new project base package.
 *
 * 3. Clean up
 *      delete the .git folder (we don't want to enforce a VCS but yeah...people should use one, so maybe later on)
 *      moves the Main.java file to the proper application package.
 *
 * The generate method will return a list of errors if any.
 * You can add GenerationListener that will be called whenever the generation process reaches a new step.
 *
 */
public class ProjectGenerator {

    private Map<String, Map<String, String>> replacements;
    private String tmpContent;
    private StrSubstitutor sub;
    private List<String> lastGenerationErrors = new ArrayList<>();
    private List<GenerationListener> listeners = new ArrayList<>();

    public ProjectGenerator() {
        Yaml yaml = new Yaml();
        replacements = (Map<String, Map<String, String>>) yaml.load(this.getClass().getResourceAsStream("/replacement.yml"));
    }


    /**
     * Generates a project given a param map.
     * The param map must contain :
     *
     * templateUrl: the project template git repo url.
     * baseDir: the directory where to create the repo.
     * projectName: the project name.
     * packageName: the new project base package.
     *
     * Those params can be used as template names in the replacement.yml file (ie: ${projectName})
     *
     * this method returns a list of error that occurred during the generation
     * @param params the params map.
     * @return the errors that occurred during the generation.
     */
    public List<String> generate(Map<String, String> params){
        progress("Initializing...");
        sub = new StrSubstitutor(params, "${", "}");
        lastGenerationErrors.clear();
        String templateUrl = params.get("templateUrl");
        String baseDir = params.get("baseDir");
        String projectName = params.get("projectName");
        String packageName = params.get("packageName");
        String projectPath = baseDir + projectName + "/";

        progress("Cloning template project...");
        cloneTemplate(templateUrl, projectPath);

        progress("Adapting project...");
        replacements.entrySet().forEach((e) -> adaptFile(projectPath + e.getKey(), e.getValue()));

        progress("Cleaning up...");
        moveAndCleanupFiles(baseDir, projectName, packageName, projectPath);

        if(lastGenerationErrors.isEmpty()) {
            progress("Done.");
        } else {
            progress("Done with errors.");
        }
        return Collections.unmodifiableList(lastGenerationErrors);
    }

    private void moveAndCleanupFiles(String baseDir, String projectName, String packageName, String projectPath) {
        try {
            FileUtils.moveFile(new File(projectPath + "src/main/java/mygame/Main.java"), new File(projectPath + "src/main/java/" + packageName.replaceAll("\\.", "/") + "/Main.java"));
            //delete .git directory
            FileUtils.deleteDirectory(new File(baseDir + projectName + "/src/main/java/mygame"));
            FileUtils.deleteDirectory(new File(baseDir + projectName + "/.git"));
        } catch (IOException e) {
            reportError("Error while moving and cleaning up", e);
        }
    }

    private void cloneTemplate(String templateUrl, String projectPath) {
        try (Git git = Git.cloneRepository().setURI(templateUrl)
                .setDirectory(new File(projectPath)).call()){
        } catch (GitAPIException e) {
            reportError("Error cloning repository " + templateUrl , e);
        }
    }


    private void adaptFile(String fileName, Map<String, String> replacement) {
        try {

            tmpContent = FileUtils.readFileToString(new File(fileName), "UTF-8");
            replacement.forEach((key, value) -> {
                String finalValue = sub.replace(value);
                tmpContent = tmpContent.replaceAll(key, finalValue);
            });

          //  System.err.println(tmpContent);
            FileUtils.writeStringToFile(new File(fileName), tmpContent, "UTF-8");
        } catch (IOException e) {
            reportError("Issue adapting file " + fileName,  e);
        }
    }

    private void reportError(String message, Exception e){
        lastGenerationErrors.add(message+ ": " +e.getMessage());
        e.printStackTrace();
    }

    public void addGenerationListener(GenerationListener l){
        listeners.add(l);
    }

    public void removeGenerationListener(GenerationListener l){
        listeners.add(l);
    }

    private void progress(String newState){
        listeners.forEach((l) -> l.progress(newState));
    }
}
