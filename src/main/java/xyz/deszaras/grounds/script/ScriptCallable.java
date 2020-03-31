package xyz.deszaras.grounds.script;

import com.google.common.collect.ImmutableList;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import xyz.deszaras.grounds.model.Thing;

/**
 * A callable for executing a {@link Script}. Groovy is supported.
 */
public class ScriptCallable implements Callable<Boolean> {

  private final Thing caller;
  private final Script script;
  private final List<Object> scriptArguments;

  /**
   * Creates a new callable.
   *
   * @param caller thing executing the script (often a player)
   * @param script script to execute
   * @param scriptArguments resolved script arguments
   * @throws NullPointerException if any argument is null
   */
  public ScriptCallable(Thing caller, Script script, List<Object> scriptArguments) {
    this.caller = Objects.requireNonNull(caller);
    this.script = Objects.requireNonNull(script);
    this.scriptArguments = ImmutableList.copyOf(scriptArguments);
  }

  /**
   * Executes the script.<p>
   *
   * A binding is established for the script to run with. The caller
   * is bound as the "caller" property, and each resolved script
   * argument is bound as "arg0", "arg1", and so on.<p>
   *
   * If the script returns a Boolean (it should), that value is
   * returned by this method. Otherwise, true is returned if the
   * return value is null.
   *
   * @return return value of script, either directly or derived
   */
  @Override
  public Boolean call() {
    // Create a binding for the script, including caller
    // and each argument.
    Binding binding = new Binding();
    binding.setProperty("caller", caller);
    for (int i = 0; i < scriptArguments.size(); i++) {
      binding.setProperty("arg" + i, scriptArguments.get(i));
    }

    // Evaluate the script in a Groovy shell.
    GroovyShell commandShell = new GroovyShell(binding);
    Object result = commandShell.evaluate(script.getContent());

    // Derive a return value.
    if (result instanceof Boolean) {
      return ((Boolean) result).booleanValue();
    } else {
      return result == null;
    }
  }
}
