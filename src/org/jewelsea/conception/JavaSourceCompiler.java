package org.jewelsea.conception;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class JavaSourceCompiler {
  private final static Logger log = Logger.getLogger(JavaSourceCompiler.class.getName());     
    
  private JavaCompiler compiler;    
  private StandardJavaFileManager fileManager;
  private File location;
 
  public JavaSourceCompiler() {
    compiler = findCompiler();
    
    if (compiler != null) {
      fileManager = compiler.getStandardFileManager(null, null, null);   
    }
  }

  public boolean isInitialized() {
    return compiler != null;
  }
  
  public void setLocation(File path) throws IOException {
    this.location = path;
    
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT,  Arrays.asList(path));
    fileManager.setLocation(StandardLocation.CLASS_PATH,    Arrays.asList(path));
    fileManager.setLocation(StandardLocation.SOURCE_PATH,   Arrays.asList(path));
    fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Arrays.asList(path));
    fileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, Arrays.asList(path));
  }

  public boolean compile(Iterable<? extends JavaFileObject> compilationUnits) {    
    boolean compilationSucceeded = compiler.getTask(
      null, 
      fileManager, 
      null, 
      null, 
      null, 
      compilationUnits
    ).call();
    
    if (compilationSucceeded) {
      log.info("Compilation successful");
    } else {
      log.warning("Compilation failed");
    }
    
    return compilationSucceeded;
  }
  
  public boolean compile(String javaFilenameWithoutExtension, String sourceCode) {
    return compile(
      Arrays.asList(
        new JavaSourceFromString(
          javaFilenameWithoutExtension, 
          sourceCode
       )
      )
    );
  }
  
  public boolean compile(String javaFilenameWithoutExtension, File sourceFile) {
    return compile(
      fileManager.getJavaFileObjects(sourceFile)
    );        
  }

  public URL getCompiledCodeLocation(String classFileName) {
    try {
      // eclipse java compiler doesn't seem to return the correct location of compiled files when we ask it
      // so instead we will just return the location we were originally told.
      //    Iterator<? extends JavaFileObject> compiledFiles = fileManager.getJavaFileObjects(classFileName).iterator();
      //    if (compiledFiles.hasNext()) {
      //      try {
      //        JavaFileObject compiledFile = compiledFiles.next();
      //        String compiledFileLocation = compiledFile.toUri().toURL().toExternalForm();
      //        compiledFileLocation = compiledFileLocation.substring(0, compiledFileLocation.length() - (classFileName).length());
      //        
      //        return new URL(compiledFileLocation);
      //      } catch (MalformedURLException ex) {
      //        Logger.getLogger(JavaSourceCompiler.class.getName()).log(Level.SEVERE, null, ex);
      //      }
      //    }

      return location != null 
                ? location.toURI().toURL() 
                : null;
    } catch (MalformedURLException ex) {
      log.log(Level.SEVERE, null, ex);
      return null;
    }
  }

  private JavaCompiler findCompiler() {
    JavaCompiler compiler = null; 

    // if the compiler has been configured as a service, try to load it.
    // our services are setup to first look for the Oracle java compiler
    // if the Oracle java compiler can't be found, fall back to the eclipse one.
    // services are configured in a META-INF/services/javax.tools.JavaCompiler file.
    final Iterator<JavaCompiler> services = ServiceLoader.load(JavaCompiler.class).iterator();
    while (services.hasNext()) {
      try {
        compiler = services.next();
        if (compiler != null) {
          break;
        }    
      } catch (Throwable t) {
        // this compiler for this service could not be found, just skip it.
      }  
    }

    // no compiler service was found, see if we can get a compiler from the jdk ToolProvider.
    if (compiler == null) { 
      compiler = ToolProvider.getSystemJavaCompiler(); // grabs the jdk java compiler.
    }
 
    if (compiler != null) {
      log.info("Using compiler: " + compiler.getClass().getCanonicalName());
    } else {
      log.warning("No compiler found");
    } 
    
    return compiler;
  }
  
}
