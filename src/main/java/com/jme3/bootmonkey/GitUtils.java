package com.jme3.bootmonkey;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import java.io.*;
import java.util.*;

/**
 * Created by Nehon on 16/10/2016.
 */
public class GitUtils {

    public static List<String> getRemoteBranches(String repoUrl) throws IOException, GitAPIException {
        Collection<Ref> refs = Git.lsRemoteRepository().setRemote(repoUrl).call();
        List<String> names = new ArrayList<>();
        refs.forEach((ref) -> {
         //   System.err.println(ref);
            if(!ref.getName().equals("HEAD" ) && !ref.getName().equals("refs/heads/master") && !ref.getName().startsWith("refs/pull")){
                names.add(ref.getName());
            }
        });
        return names;
    }
}
