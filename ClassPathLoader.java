
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JarLoader {
    
    public static void main(String[] args) throws IOException {
        String directoryPath = "your/directory/path";
        String jarRegex = ".*\\.jar$"; // Regular expression to match JAR files
        
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.matches(jarRegex)) {
                        addJarToClasspath(file);
                    }
                }
            }
        }
    }

    private static void addJarToClasspath(File file) throws IOException {
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL url = file.toURI().toURL();
        URL[] urls = classLoader.getURLs();
        boolean alreadyLoaded = false;
        
        for (URL existingUrl : urls) {
            if (existingUrl.equals(url)) {
                alreadyLoaded = true;
                break;
            }
        }
        
        if (!alreadyLoaded) {
            URLClassLoader newClassLoader = new URLClassLoader(new URL[]{url}, classLoader);
            Thread.currentThread().setContextClassLoader(newClassLoader);
            System.out.println("Loaded JAR file: " + file.getAbsolutePath());
        } else {
            System.out.println("JAR file already loaded: " + file.getAbsolutePath());
        }
    }
}
