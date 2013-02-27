package org.jewelsea.conception;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class JavaSourceCompiler {
  private JavaCompiler compiler;    
  private StandardJavaFileManager fileManager;
 
  public JavaSourceCompiler() {
    compiler = ToolProvider.getSystemJavaCompiler(); // grabs the jdk java compiler.
    if (compiler == null) { 
      // no jdk java compiler available => try to get a java compiler from the service loader instead . . .
      // for example if tools.jar is on the classpath (and META-INF/services configured), use that . . .
      final Iterator<JavaCompiler> services = ServiceLoader.load(JavaCompiler.class).iterator();
      if (services.hasNext()) {
        compiler = services.next();
      } else {
        // A valid java compiler still is not found, try to instantiate the sun java compiler from a hardcoded path
        try {
          compiler = new com.sun.tools.javac.api.JavacTool();
        } catch (Exception e) {
          // ok all is lost, no java compiler could be found.
        }  
      }  
    }
    
    if (compiler != null) {
      fileManager = compiler.getStandardFileManager(null, null, null);    
    } else {
      System.out.println("No compiler found");
    } 
  }

  public boolean isInitialized() {
    return compiler != null;
  }

  public boolean compile(String javaFilenameWithoutExtension, String sourceCode) {
    final List<JavaSourceFromString> compilationUnits = Arrays.asList(
      new JavaSourceFromString(
        javaFilenameWithoutExtension, 
        sourceCode
      )
    );
    
    boolean compilationSucceeded = compiler.getTask(
      null, 
      fileManager, 
      null, 
      null, 
      null, 
      compilationUnits
    ).call();
    
    if (compilationSucceeded) {
      System.out.println("Compilation was successful");
    } else {
      System.out.println("Compilation failed");
    }
    
    return compilationSucceeded;
  }
  
  public URL getCompiledCodeLocation(String classFileName) {
    Iterator<? extends JavaFileObject> compiledFiles = fileManager.getJavaFileObjects(classFileName).iterator();
    if (compiledFiles.hasNext()) {
      try {
        JavaFileObject compiledFile = compiledFiles.next();
        String compiledFileLocation = compiledFile.toUri().toURL().toExternalForm();
        compiledFileLocation = compiledFileLocation.substring(0, compiledFileLocation.length() - (classFileName).length());
        
        return new URL(compiledFileLocation);
      } catch (MalformedURLException ex) {
        Logger.getLogger(JavaSourceCompiler.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    
    return null;
  }
}
