package org.jewelsea.conception;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaCodeExecutor {
  private URL compiledCodeLoc;
 
  public JavaCodeExecutor(URL compiledCodeLoc) {
    this.compiledCodeLoc = compiledCodeLoc;
  }
  
  public void execute(String mainClassWithoutExtension) {
    try {
      final String[] args = {};
      final URL[]    classloaderURL = { compiledCodeLoc };

      final ClassLoader classloader = new URLClassLoader(
        classloaderURL,
        ClassLoader.getSystemClassLoader().getParent()
      );    

      final Class mainClass = classloader.loadClass(mainClassWithoutExtension);
      final Method main = mainClass.getMethod("main", new Class[]{ args.getClass() });  
      
      Thread executionThread = new Thread(
        new Runnable() {
          @Override public void run() {
            try {
              Thread.currentThread().setContextClassLoader(classloader);
              main.invoke(null, (Object) args);
            } catch (Throwable t) {
              Logger.getLogger(JavaCodeExecutor.class.getName()).log(Level.SEVERE, null, t);
            }
          }
        },
        "program-runner"
      );
      executionThread.setDaemon(true);
      executionThread.start();
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
      Logger.getLogger(JavaCodeExecutor.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
