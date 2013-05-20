
package org.holoeverywhere.resbuilder;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal optimize
 * @phase initialize
 */
public class OptimizeMojo extends ResMojo {
    private static final class ImageOptimizer {
        private static final File which(String programm) {
            for (String path : System.getenv("PATH").split(":")) {
                File file = new File(new File(path), programm).getAbsoluteFile();
                if (file.isFile() && file.canExecute()) {
                    return file;
                }
            }
            return null;
        }

        private final File mOptipngBin;

        public ImageOptimizer() throws MojoExecutionException {
            mOptipngBin = which("optipng");
            if (mOptipngBin == null) {
                throw new MojoExecutionException("You should install optipng in PATH");
            }
        }

        public void process(File[] includeDirs) throws MojoExecutionException {
            if (includeDirs == null) {
                return;
            }
            for (File file : includeDirs) {
                process(file.listFiles(DIRS_FILTER));
                processFile(file.listFiles(PNG_FILTER));
            }
        }

        private void processFile(File file) throws MojoExecutionException {
            try {
                System.out.println(file + "");
                Runtime.getRuntime().exec(mOptipngBin.getAbsolutePath(), new String[] {
                        "-o7", file.getAbsolutePath()
                }).waitFor();
            } catch (Exception e) {
                throw new MojoExecutionException("Error in ImageOptimizer", e);
            }
        }

        private void processFile(File[] files) throws MojoExecutionException {
            if (files == null) {
                return;
            }
            for (File file : files) {
                processFile(file);
            }
        }
    }

    private static final class XmlOptimizer {
        private final Comparator<Attribute> mEventComparator = new Comparator<Attribute>() {

            @Override
            public int compare(Attribute o1, Attribute o2) {
                return o1.getName().getLocalPart().compareTo(o1.getName().getLocalPart());
            }
        };

        private final XMLInputFactory mInputFactory = XMLInputFactory.newFactory();

        private final XMLOutputFactory mOutputFactory = XMLOutputFactory.newFactory();

        private void process(File file) throws MojoExecutionException {
            try {
                XMLEventReader reader =
                        mInputFactory.createXMLEventReader(new FileInputStream(file), "utf-8");
                StringWriter buffer = new StringWriter();
                XMLEventWriter writer = mOutputFactory.createXMLEventWriter(buffer);
                XMLEvent event;
                SortedSet<Attribute> mAttrsSet = new TreeSet<Attribute>(mEventComparator);
                while (reader.hasNext()) {
                    event = reader.nextEvent();
                    if (event.getEventType() == XMLStreamConstants.COMMENT) {
                        continue;
                    }
                    if (event.getEventType() == XMLStreamConstants.ATTRIBUTE) {
                        mAttrsSet.add((Attribute) event);
                    } else {
                        if (mAttrsSet.size() > 0) {
                            for (Attribute attr : mAttrsSet) {
                                writer.add(attr);
                            }
                            mAttrsSet.clear();
                        }
                        writer.add(event);
                    }
                }
                writer.flush();
                writer.close();
                reader.close();
                byte[] xmlData = buffer.toString().getBytes("utf-8");
                OutputStream os = new FileOutputStream(file);
                os.write(xmlData);
                os.flush();
                os.close();
            } catch (Exception e) {
                throw new MojoExecutionException("XML Optimizing error", e);
            }
        }

        private void process(File[] files) throws MojoExecutionException {
            if (files == null) {
                return;
            }
            for (File file : files) {
                process(file);
            }
        }

        private void process(File[] includeDirs, File[] excludeFirs) throws MojoExecutionException {
            Path[] excludePaths = null;
            if (excludeFirs != null) {
                excludePaths = new Path[excludeFirs.length];
                for (int i = 0; i < excludeFirs.length; i++) {
                    excludePaths[i] = excludeFirs[i].getAbsoluteFile().toPath();
                }
            }
            process(includeDirs, excludePaths);

        }

        private void process(File[] includeDirs, Path[] excludePaths) throws MojoExecutionException {
            for (File include : includeDirs) {
                if (excludePaths != null) {
                    boolean skip = false;
                    Path path = include.getAbsoluteFile().toPath();
                    for (Path excludePath : excludePaths) {
                        if (path.startsWith(excludePath)) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip) {
                        continue;
                    }
                }
                process(include.listFiles(DIRS_FILTER), excludePaths);
                process(include.listFiles(XML_FILTER));
            }
        }
    }

    private static final FileFilter DIRS_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };
    private static final FileFilter PNG_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(".png");
        }
    };
    private static final FileFilter XML_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(".xml");
        }
    };;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (optimizeSkip) {
            getLog().info("Resource optimizing skipped");
            return;
        }
        if (optimizeXmlIncludeDirs != null) {
            new XmlOptimizer().process(optimizeXmlIncludeDirs, optimizeXmlExcludeDirs);
        }
        if (optimizeImageIncludeDirs != null) {
            new ImageOptimizer().process(optimizeImageIncludeDirs);
        }
    }
}
