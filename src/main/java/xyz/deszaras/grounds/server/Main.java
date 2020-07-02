package xyz.deszaras.grounds.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import xyz.deszaras.grounds.model.Universe;

/**
 * The entry point for the application.
 */
public class Main {

  public static class Args {
    @Parameter(names = { "-p", "--properties" },
               description = "Server properties (for multi-user mode)",
               required = true,
               converter = FileConverter.class)
    private File propertiesFile = null;
    @Parameter(names = { "-s", "--single-user" },
               description = "Start in single-user mode")
    private boolean singleUser = false;
    @Parameter(names = { "-u", "--universe" },
               description = "Universe to load",
               converter = FileConverter.class)
    private File universeFile = null;
  }

  public static void main(String[] args) throws Exception {
    Args jcArgs = new Args();
    JCommander.newBuilder()
        .addObject(jcArgs)
        .build()
        .parse(args);

    Properties properties = new Properties();
    try (FileReader r = new FileReader(jcArgs.propertiesFile)) {
      properties.load(r);
    }
    if (jcArgs.universeFile != null) {
      Universe universe = Universe.load(jcArgs.universeFile);

      universe.removeGuests();

      Universe.setCurrent(universe);
      Universe.setCurrentFile(jcArgs.universeFile);
    } else {
      Universe.setCurrent(Universe.VOID);
      Universe.setCurrentFile(null);
    }

    if (jcArgs.singleUser) {
      new SingleUser(properties).run();
    } else {
      Server server = new Server(properties);
      server.start();
      server.shutdownOnCommand();
    }
  }
}
