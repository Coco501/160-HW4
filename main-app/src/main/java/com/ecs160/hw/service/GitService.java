package com.ecs160.hw.service;

public class GitService {

    public static void cloneRepo(String htmlUrl, String clonePath) {
        ProcessBuilder pb = new ProcessBuilder("git", "clone", htmlUrl, clonePath, "--depth", "1");
        pb.inheritIO(); // inherits stdin, stdout, stderr from parent (our project), allowing us to use
                        // the relative path
        try {
            Process cloningProcess = pb.start();
            cloningProcess.waitFor();
        } catch (Exception e) {
            System.err.println("Error during cloning: " + e.getMessage());
        }
    }
}
