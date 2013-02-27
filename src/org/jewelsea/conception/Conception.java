package org.jewelsea.conception;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;

/**
 * A CodeMirror based JavaScript CodeEditor wrapped in
 * a JavaFX WebView, with the ability to 
 * compile and run the edited code.
 */
public class Conception extends Application {
  private final static Logger log = Logger.getLogger(Conception.class.getName());     

  private static final String MAIN_CLASS_NAME = "HelloWorld";
  
  // some sample code to be edited.
  private static final String editingCode =
    "public class HelloWorld {\n"+
    "  public static void main(String[] args) {\n" +
    "    System.out.println(\"Hello, World\");\n" +
    "  }\n" +
    "}\n";

  private static final String SOURCE_FILE_TYPE = ".java";
  private static final String TEMP_DIRECTORY_NAME = "concept-";
  
  public static void main(String[] args) { launch(args); }
  
  private JavaSourceCompiler compiler;
  private LogArea logArea;
  private Path compilationDir;  

  @Override public void init() throws Exception {
    compiler = new JavaSourceCompiler();
  }
  
  @Override public void stop() throws Exception {
    FileHelper.recursivelyDeleteDir(compilationDir);
  }
  
  @Override public void start(Stage stage) throws Exception {
    stage.setTitle("Conception");
    stage.getIcons().setAll(
      new Image(getResourceLoc("icons/flower-seed-icon-16.png")),
      new Image(getResourceLoc("icons/flower-seed-icon-32.png")),
      new Image(getResourceLoc("icons/flower-seed-icon-64.png")),
      new Image(getResourceLoc("icons/flower-seed-icon-128.png")),
      new Image(getResourceLoc("icons/flower-seed-icon-256.png")),
      new Image(getResourceLoc("icons/flower-seed-icon-512.png"))
    );
      
    // create the editing controls.
    Label title = new Label("Editing: " + MAIN_CLASS_NAME + SOURCE_FILE_TYPE);
    title.setStyle("-fx-font-size: 20;");
    final CodeEditor editor = new CodeEditor(editingCode);
    
    logArea = new LogArea();

    // display the scene.
    stage.setScene(new Scene(
      layoutScene(
        title, 
        editor, 
        createRunButton(editor),
        logArea
      )
    ));
    stage.show();
  }

  private Button createRunButton(final CodeEditor editor) {
    final Button run = new Button("Run");
    run.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        compileAndRunCode(editor);
      }
    });
    
    if (!compiler.isInitialized()) {
      run.setDisable(true);
    }
    
    return run;
  }
  
  private void compileAndRunCode(final CodeEditor editor) {
    logArea.captureOutput();
    String code = null;
    try {
      code = editor.getCodeAndSnapshot();
    } catch (Exception ex) {
      // if the editor wasn't ready loading code, we may get an exception
      // if this happens we just ignore it and don't try to compile and run the code;
      return;
    }
    if (code != null) {
      try {
        FileHelper.recursivelyDeleteDir(compilationDir);
        compilationDir = Files.createTempDirectory(TEMP_DIRECTORY_NAME);  
        compiler.setLocation(compilationDir.toFile());
        Path sourceFile = compilationDir.resolve(MAIN_CLASS_NAME + SOURCE_FILE_TYPE);
        Files.write(sourceFile, code.getBytes());

        boolean compiled = compile(sourceFile.toFile());
        if (compiled) {
          runCompiledCode();
        }  
      } catch (IOException ex) {
        log.log(Level.SEVERE, "Unable to create temporary storage for compilation", ex);
      }
    }
  }
  
  private boolean compile(String code) {
    return compiler.compile(MAIN_CLASS_NAME, code);
  }
  
  private boolean compile(File file) {
    return compiler.compile(MAIN_CLASS_NAME, file);
  }
  
  private void runCompiledCode() {
    URL compiledCodeLoc = compiler.getCompiledCodeLocation(MAIN_CLASS_NAME);
    JavaCodeExecutor executor = new JavaCodeExecutor(compiledCodeLoc);
    executor.execute(MAIN_CLASS_NAME);
  }
  
  private VBox layoutScene(Node... nodes) {
    final VBox layout = VBoxBuilder.create().spacing(10).children(nodes).build();
    layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");
    return layout;
  }

  private String getResourceLoc(String name) {
    return getClass().getResource("resources/" + name).toExternalForm();
  }
}