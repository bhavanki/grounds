package xyz.deszaras.grounds.script;

import com.google.common.collect.ImmutableList;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.codehaus.groovy.control.CompilerConfiguration;
import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.model.Player;

/**
 * A callable for executing a {@link Script}. Groovy is supported.
 */
public class ScriptCallable implements Callable<Boolean> {

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
   * is bound as "arg0", "arg1", and so on.<p>
   *
   * If the script returns a Boolean (it should), that value is
   * returned by this method. Otherwise, true is returned if the
   * return value is null.
   *
   * @return return value of script, either directly or derived
   */
  @Override
  public Boolean call() {
    // Create a binding for the script, including each argument.
    Binding binding = new Binding();
    for (int i = 0; i < scriptArguments.size(); i++) {
      binding.setProperty("arg" + i, scriptArguments.get(i));
    }

    // Compile the script in a Groovy shell.
    CompilerConfiguration compilerConfig = new CompilerConfiguration();
    compilerConfig.setScriptBaseClass(GroundsScript.class.getName());

    GroovyShell commandShell = new GroovyShell(binding, compilerConfig);
    GroundsScript gscript = (GroundsScript) commandShell.parse(script.getContent());
    gscript.setActor(actor);
    gscript.setPlayer(player);

    // Run it!
    Object result = gscript.run();

    // Derive a return value.
    if (result instanceof Boolean) {
      return ((Boolean) result).booleanValue();
    } else {
      return result == null;
    }
  }
}
