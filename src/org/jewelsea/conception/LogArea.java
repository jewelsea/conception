package org.jewelsea.conception;

import java.io.*;
import java.util.Arrays;
import java.util.logging.*;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/** 
 * A text area which can capture anything written to stdout and stderr.
 * 
 * WARNING: 
 * Currently this class is not quite correct and has some output errors.
 * 
 * However in simple cases this class will output fairly easily understood output
 * and currently suffices for the purpose it was created.
 */
public class LogArea extends TextArea {
  private PipedInputStream  outin;
  private PipedOutputStream out;

  private PipedOutputStream err;
  private PipedInputStream  errin;
  
  private Thread outputLoggingThread;
  private Thread errorLoggingThread;
  
  private boolean capturingOutput = false;
  private boolean capturingErrors = false;
  
  public LogArea() {
    this.setEditable(false);
  }
  
  public void captureOutput() {
    final TextArea textArea = this;
    textArea.clear();
    
    if (outputLoggingThread != null) {
      outputLoggingThread.interrupt();
      try {
        outputLoggingThread.join();
      } catch (InterruptedException ex) {
        // no action required.
      }  
      outputLoggingThread = null;
    }
    if (errorLoggingThread != null) {
      errorLoggingThread.interrupt();
      try {
        errorLoggingThread.join();
      } catch (InterruptedException ex) {
        // no action required.
      }  
      errorLoggingThread = null;
    }

    System.err.flush();
    System.out.flush();
    
    try {
      outin  = new PipedInputStream();
      out = new PipedOutputStream(outin);
      System.setOut(new PrintStream(out));

      errin  = new PipedInputStream();
      err = new PipedOutputStream(errin);
      System.setErr(new PrintStream(err));
      
      outputLoggingThread = new Thread(
        new LogArea.StreamCapture(textArea, outin), 
        "output-logger"
      );
      outputLoggingThread.setDaemon(true);
      outputLoggingThread.start();

      errorLoggingThread = new Thread(
        new LogArea.StreamCapture(textArea, errin), 
        "error-logger"
      );
      errorLoggingThread.setDaemon(true);
      errorLoggingThread.start();
    } catch (IOException ex) {
      Logger.getLogger(LogArea.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  private class StreamCapture implements Runnable {
    private final TextArea    textArea;
    private final InputStream in;
    
    StreamCapture(TextArea textArea, InputStream in) {
      this.textArea = textArea;
      this.in = in;
      setStyle("-fx-font-family: monospace;");
    }
    
    @Override public void run() {
      try {
        while (true) {
          if (Thread.currentThread().getName().equals("output-logger") && outputLoggingThread == null)  {
            break;
          }
          if (Thread.currentThread().getName().equals("error-logger") && errorLoggingThread == null)  {
            break;
          }
          InputStreamReader is = new InputStreamReader(in);
          BufferedReader br = new BufferedReader(is);
          String read = br.readLine();
          while(read != null) {
            textArea.appendText(read + "\n");
            read = br.readLine();
          }
          Thread.currentThread().sleep(50);
        }  
      } catch (InterruptedIOException ex) {
        // interrupted io is somewhat expected for our usage case, so ignore it.  
      } catch (IOException ex) {
        // when the target application finishes it seems to close it's output thread,
        // which will result in a Write end dead exception which we can ignore.
        if (!"Write end dead".equals(ex.getMessage())) { 
          Logger.getLogger(LogArea.class.getName()).log(Level.SEVERE, null, ex);
        } 
      } catch (Throwable t) {
        Logger.getLogger(LogArea.class.getName()).log(Level.SEVERE, null, t);
      }
    }
  }
}