BootMonkey
==========

Very Basic project scaffolding tool for JME3.

1. Clones a template repository into a local folder. For adaptation, the template repo must contain a bootmonkey.yml file.

2. Attempts to change lines in project files as described in the bootmonkey.yml
      the file must be follow that structure:
      ```
      replace:
          [filePath]:
              [search1]: [replacement1]
              [search2]: [replacement2]
              ...
      ```
    - filePath is the path to the file to modify from the project root.
    - searchN is a regular expression to search in the file
    - replacementN is the text that will replace the regexp match. You can use template variables in this replacement:
       - templateUrl: the project template git repo url.
       - baseDir: the directory where to create the repo.
       - projectName: the project name.
       - packageName: the new project base package.

3. Clean up

       Will copy and delete files according to what's found in the yaml configuration file
       the file must be follow that structure:
       ```
       copy:
           [srcPath]: [destPath]
           ...
       delete:
           - [fileORDirectoryPath]
           - ...
       ```
