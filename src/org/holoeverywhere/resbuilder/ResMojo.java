
package org.holoeverywhere.resbuilder;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.holoeverywhere.resbuilder.type.FileProcesser;

public abstract class ResMojo extends AbstractMojo {
    /**
     * Path to android sdk
     * 
     * @parameter property="android.sdk.path"
     */
    public File androidSdkPath;

    /**
     * @parameter property="android.sdk.platform" default-value="16"
     * @required
     */
    public int androidSdkVersion;

    /**
     * Build all files from all include dirs
     * 
     * @parameter property="holo.resbuilder.buildAll" default-value="false"
     */
    public boolean buildAll;

    /**
     * Dirs for search of input files
     * 
     * @parameter property="holo.resbuilder.includeDirs"
     */
    public File[] includeDirs;

    /**
     * Files for processing
     * 
     * @parameter property="holo.resbuilder.input" alias="input"
     */
    public String[] inputFiles;

    /**
     * Dirs with images for optimizing
     * 
     * @parameter property="holo.resbuilder.optimize.image.include"
     */
    public File[] optimizeImageIncludeDirs;
    /**
     * If true - skip resource build
     * 
     * @parameter property="holo.resbuilder.optimize.skip" default-value="false"
     */
    public boolean optimizeSkip;

    /**
     * @parameter property="holo.resbuilder.optimize.xml.exclude"
     */
    public File[] optimizeXmlExcludeDirs;

    /**
     * Dirs with xml files for optimizing
     * 
     * @parameter property="holo.resbuilder.optimize.xml.include"
     *            default-value="${basedir}/res"
     */
    public File[] optimizeXmlIncludeDirs;

    /**
     * Default output dir, if input file don't specify it
     * 
     * @parameter property="holo.resbuilder.outputDir"
     *            default-value="${basedir}/res"
     */
    public File outputDir;
    public FileProcesser processer;
    /**
     * If true - skip resource build
     * 
     * @parameter property="holo.resbuilder.skip" default-value="false"
     */
    public boolean skip;

    /**
     * Be verbose
     * 
     * @parameter property="holo.resbuilder.verbose" default-value="true"
     */
    public boolean verbose;
}
