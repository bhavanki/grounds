package xyz.deszaras.grounds.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Main {

  public static class Args {
    @Parameter(names = { "-p", "--properties" },
               description = "Server properties",
               converter = FileConverter.class)
    private File propertiesFile = null;
    @Parameter(names = { "-s", "--single-user" },
               description = "Start in single-user mode")
    private boolean singleUser = false;
  }

  public static void main(String[] args) throws Exception {
    Args jcArgs = new Args();
    JCommander.newBuilder()
        .addObject(jcArgs)
        .build()
        .parse(args);

    if (jcArgs.singleUser) {
      new SingleUser().run();
    } else {
      Properties properties = new Properties();
      if (jcArgs.propertiesFile != null) {
        try (FileReader r = new FileReader(jcArgs.propertiesFile)) {
          properties.load(r);
        }
      }
      Server server = new Server(properties);

      server.start();
      server.shutdownOnCommand();
    }
  }
}
