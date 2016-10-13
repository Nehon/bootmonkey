package com.jme3.bootmonkey;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * Created by Nehon on 12/10/2016.
 * Very Basic project scaffolding tool for JME3.
 * 1. Clones a template repository into a local folder. For adaptation, the template repo must contain a bootmonkey.yml file.
 * 2. attempt to change lines in project files as described in the bootmonkey.yml
 *      the file must be follow that structure:
 *      replace:
 *          [filePath]:
 *              [search1]: [replacement1]
 *              [search2]: [replacement2]
 *              ...
 *  - filePath is the path to the file to modify from the project root.
 *  - searchN is a regular expression to search in the file
 *  - replacementN is the text that will replace the regexp match. You can use template variables in this replacement:
 *      - templateUrl: the project template git repo url.
 *      - baseDir: the directory where to create the repo.
 *      - projectName: the project name.
 *      - packageName: the new project base package.
 *      - packagePath: the folder path of the package ('.' replaced with '/')
 *
 * 3. Clean up
 *      Will copy and delete files according to what's found in the yaml configuration file
 *      the file must be follow that structure:
 *      copy:
 *          [srcPath]: [destPath]
 *          ...
 *      delete:
 *          - [fileORDirectoryPath]
 *          - ...
 *
 * You can add GenerationListener that will be called whenever the generation process reaches a new step, with errors from the previous step.
 *
 */
public class ProjectGenerator {

    private static String CONF_FILE = "bootmonkey.yml";

    private Map<String, Map<String, String>> replacements;
    private Map<String, String> copies;
    private List<String> deletes;
    private String tmpContent;
    private StrSubstitutor sub;
    private List<String> lastGenerationErrors = new ArrayList<>();
    private List<GenerationListener> listeners = new ArrayList<>();

    public ProjectGenerator() {

    }

    private void readConfiguration(String projectPath) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> map = (Map<String, Object>) yaml.load(FileUtils.readFileToString(new File(projectPath + CONF_FILE), "UTF-8"));

            if (map.get("replace") != null) {
                replacements = (Map<String, Map<String, String>>) map.get("replace");
            }
            if (map.get("copy") != null) {
                copies = (Map<String, String>) map.get("copy");
            }
            if (map.get("delete") != null) {
                deletes = (List<String>) map.get("delete");
            }
        } catch (IOException e){
            reportError("No configuration (" + CONF_FILE + ") file found in the template project. No adaptation has been done.", e);
        }
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
     */
    public void generate(Map<String, String> params){

        progress("Initializing...");
        sub = new StrSubstitutor(params, "${", "}");
        String templateUrl = params.get("templateUrl");
        String baseDir = params.get("baseDir");
        String projectName = params.get("projectName");
        String packageName = params.get("packageName");
        String packagePath = packageName.replaceAll("\\.", "/");
        params.put("packagePath", packagePath);
        String projectPath = baseDir + projectName + "/";

        progress("Cloning template project...");
        if(!cloneTemplate(templateUrl, projectPath)){
            progress("Aborted.");
            return;
        }

        progress("Reading configuration...");
        readConfiguration(projectPath);

        progress("Adapting project...");
        replacements.entrySet().forEach((e) -> adaptFile(projectPath + e.getKey(), e.getValue()));

        progress("Cleaning up...");
        moveAndCleanupFiles(projectPath);

        progress("Done.");

    }

    private void moveAndCleanupFiles(String projectPath) {
        try {
            for (Map.Entry<String, String> copy : copies.entrySet()) {
                FileUtils.copyFile(new File(projectPath + copy.getKey()), new File(projectPath + sub.replace(copy.getValue())));
            }
        } catch (IOException e) {
            reportError("Error while moving and cleaning up", e);
        }

        for (String delete : deletes) {
            String subDelete = sub.replace(delete);
            boolean deleted = FileUtils.deleteQuietly(new File(projectPath + subDelete));
            if(!deleted){
                reportError("Error while deleting " + subDelete, new Exception(delete));
            }
        }

        //delete the configuration file if any
        FileUtils.deleteQuietly(new File(projectPath + CONF_FILE));


    }

    private boolean cloneTemplate(String templateUrl, String projectPath) {
        try (Git git = Git.cloneRepository().setURI(templateUrl)
                .setDirectory(new File(projectPath)).call()){
            return true;
        } catch (GitAPIException | JGitInternalException e) {
            reportError("Error cloning repository " + templateUrl , e);
            return false;
        }
    }


    private void adaptFile(String fileName, Map<String, String> replacement) {
        try {

            tmpContent = FileUtils.readFileToString(new File(fileName), "UTF-8");
            replacement.forEach((key, value) -> {
                String finalValue = sub.replace(value);
                tmpContent = tmpContent.replaceAll(key, finalValue);
            });

//System.err.println(tmpContent);
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
        listeners.forEach((l) -> l.progress(newState, new ArrayList<>(lastGenerationErrors)));
        lastGenerationErrors.clear();
    }
}
