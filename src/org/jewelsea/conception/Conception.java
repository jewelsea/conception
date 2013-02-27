package org.jewelsea.conception;

import java.net.URL;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;

/**
 * An example application which demonstrates use of a
 * CodeMirror based JavaScript CodeEditor wrapped in
 * a JavaFX WebView, with the ability to 
 * compile and run the edited code.
 */
public class Conception extends Application {
  static final private String MAIN_CLASS_NAME = "HelloWorld";
  
  // some sample code to be edited.
  static final private String editingCode =
    "public class HelloWorld {\n"+
    "  public static void main(String[] args) {\n" +
    "    System.out.println(\"Hello, World\");\n" +
    "  }\n" +
    "}\n";

  public static void main(String[] args) { launch(args); }
  
  private JavaSourceCompiler compiler;
  private LogArea logArea;
  
  @Override public void init() throws Exception {
    compiler = new JavaSourceCompiler();
  }
  
  @Override public void start(Stage stage) throws Exception {
    // create the editing controls.
    Label title = new Label("Editing: " + MAIN_CLASS_NAME + ".java");
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
        logArea.captureOutput();
        String code = null;
        try {
          code = editor.getCodeAndSnapshot();
        } catch (Exception ex) {
          // if the editor wasn't ready loading code, we may get an exception
          // if this happens we just ignore it and don't try to compile and run the code;
          return;
        }
        if (code != null && compile(code)) {
          runCompiledCode();
        }
      }
    });
    
    if (!compiler.isInitialized()) {
      run.setDisable(true);
    }
    
    return run;
  }

  private boolean compile(String code) {
    return compiler.compile(MAIN_CLASS_NAME, code);
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
}