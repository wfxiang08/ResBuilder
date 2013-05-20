
package org.holoeverywhere.resbuilder;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.holoeverywhere.resbuilder.type.FileProcesser;
import org.holoeverywhere.resbuilder.type.FileProcesser.FileProcesserException;

/**
 * @goal build
 * @phase initialize
 */
public class BuildMojo extends ResMojo {
    private static final FileFilter BUILD_ALL_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().length() > 5
                    && pathname.getName().endsWith(".json");
        }
    };

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Flag 'Skip' is true");
            return;
        }
        if (androidSdkPath == null || !androidSdkPath.exists()) {
            final String path = System.getenv("ANDROID_HOME");
            if (path != null) {
                androidSdkPath = new File(path);
            }
        }
        if (androidSdkPath == null || !androidSdkPath.exists()) {
            throw new MojoExecutionException("Couldn't find Android SDK by path: " + androidSdkPath);
        }
        if (buildAll) {
            if (includeDirs == null || includeDirs.length == 0) {
                getLog().warn(
                        "BuildAll: You want build all files from all include dirs, but you don't specify any include dir. Nothing to build. Skip.");
                return;
            }
            List<String> list = new ArrayList<String>();
            for (File dir : includeDirs) {
                File[] files = dir.listFiles(BUILD_ALL_FILTER);
                for (File file : files) {
                    getLog().info("BuildAll: add " + file.getAbsolutePath());
                    list.add(file.getAbsolutePath());
                }
            }
            if (inputFiles != null && inputFiles.length > 0) {
                list.addAll(0, Arrays.asList(inputFiles));
            }
            inputFiles = list.toArray(new String[list.size()]);
            if (inputFiles.length == 0) {
                getLog().info("BuildAll: nothing to build");
                return;
            }
        }
        if (inputFiles == null || inputFiles.length == 0) {
            getLog().info("Don't specify input files, skip");
            return;
        }
        if (!buildAll && includeDirs == null || includeDirs.length == 0) {
            getLog().warn("Include dirs don't specified");
        }
        if (verbose) {
            getLog().info("");
            getLog().info("Final configuration:");
            getLog().info(
                    " # androidSdkPath: " + androidSdkPath);
            getLog().info(" # androidSdkVersion: " + androidSdkVersion);
            if (includeDirs == null || includeDirs.length == 0) {
                getLog().info(" # includeDirs: empty");
            } else {
                getLog().info(" # includeDirs: [");
                for (File dir : includeDirs) {
                    getLog().info(" # # " + dir.getAbsolutePath());
                }
                getLog().info(" # ]");
            }
            if (inputFiles == null || inputFiles.length == 0) {
                getLog().info(" # input: empty");
            } else {
                getLog().info(" # input: [");
                for (String input : inputFiles) {
                    getLog().info(" # # " + input);
                }
                getLog().info(" # ]");
            }
            getLog().info(" # outputDir: " + outputDir);
        }
        try {
            FileProcesser.process(this);
        } catch (FileProcesserException e) {
            throw new MojoFailureException("Error in FileProcesser", e);
        }
    }
}
