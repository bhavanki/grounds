package xyz.deszaras.grounds.script;

import com.google.common.collect.ImmutableList;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.model.Player;

/**
 * A callable for executing a {@link Script}. Groovy is supported.
 */
public class ScriptCallable implements Callable<String> {

  private static final Logger LOG = LoggerFactory.getLogger(ScriptCallable.class);

  private final Actor actor;
  private final Player player;
  private final Script script;
  private final List<String> scriptArguments;

  /**
   * Creates a new callable.
   *
   * @param actor actor executing the script
   * @param player player executing the script
   * @param script script to execute
   * @param scriptArguments arguments to pass to the script
   * @throws NullPointerException if any argument is null
   */
  public ScriptCallable(Actor actor, Player player, Script script, List<String> scriptArguments) {
    this.actor = Objects.requireNonNull(actor);
    this.player = Objects.requireNonNull(player);
    this.script = Objects.requireNonNull(script);
    this.scriptArguments = ImmutableList.copyOf(scriptArguments);
  }

  /**
   * Executes the script.<p>
   *
   * A binding is established for the script to run with. Each script argument
   * is bound (as a string) as "arg0", "arg1", and so on.<p>
   *
   * The return value of the script is returned by this method after calling
   * {@code toString()} on it (unless it's null).
   *
   * @return return value of script
   */
  @Override
  public String call() {
    // Create a binding for the script, including each argument.
    Binding binding = new Binding();
    for (int i = 0; i < scriptArguments.size(); i++) {
      binding.setProperty("arg" + i, scriptArguments.get(i));
    }
    binding.setProperty("extensionId", script.getExtension().getId().toString());

    // Compile the script in a Groovy shell.
    CompilerConfiguration compilerConfig = new CompilerConfiguration();
    compilerConfig.setScriptBaseClass(GroundsScript.class.getName());

    GroovyShell commandShell = new GroovyShell(binding, compilerConfig);
    GroundsScript gscript;
    try {
      gscript = (GroundsScript) commandShell.parse(script.getContent());
    } catch (Exception e) {
      LOG.error("Failed to parse script in extension {}",
                script.getExtension().getId(), e);
      return null;
    }
    gscript.setActor(actor);
    gscript.setPlayer(player);
    gscript.setOwner(script.getOwner());

    // Run it!
    Object result;
    try {
      result = gscript.run();
    } catch (Exception e) {
      LOG.error("Failed to run script in extension {}",
                script.getExtension().getId(), e);
      return null;
    }

    if (result == null) {
      return null;
    } else {
      return result.toString();
    }
  }
}
