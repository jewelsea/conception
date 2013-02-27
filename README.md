# Conception

<img href="src/org/jewelsea/conception/resources/icons/flower-seed-icon-128.png"/>

Conception allows you to edit, compile and run Java programs.

### Screen Shot

<img href="http://i.stack.imgur.com/eHkxu.png"/>"

### Build Prerequisites
 * Oracle JDK 7u15
 * NetBeans 7.3

Please use the exact specified JDK version and NetBeans versions for building the application.
  
### Build Instructions
The applicaton may be built in NetBeans.

 1. Open the conception project directory in NetBeans.
 2. Choose a run configuration (Default | Standalone | In Browser).
 3. Press the green arrow to run the program.
 
### Deployment Instructions
 
Build artifacts are located in the dist directory.  To allow for distribution, just copy all files to a webserver.  
 
It is recommended to install [Oracle Java Runtime Environment (JRE) 7u15](http://www.java.com/en/) on the client to allow it to execute the application.

### Technology Used

[CodeMirror](http://codemirror.net) code editor wrapped in a [JavaFX WebView](http://docs.oracle.com/javafx/2/api/javafx/scene/web/WebView.html) with the [Eclipse Java Compiler](http://www.eclipse.org/jdt/core/index.php) used for code compilation.

### License 

[LGPLv3](http://www.gnu.org/copyleft/lesser.html)

### Attributions

The Conception Icon is Icon Linkware (backlink to the creators website: http://raindropmemory.deviantart.com).

See the CodeMirror, Eclipse Java Compiler and Oracle Java websites previously linked for licensing information on those respective components.

### Known Issues

 1. On a Mac, you must serve the application from an http server (not the file system) for it to work in Applet and WebStart modes.
 2. When running in applet mode, you may see a message `incorrect classpath: /Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/classes`.  This message can be ignored and does not affect the operation of the program.