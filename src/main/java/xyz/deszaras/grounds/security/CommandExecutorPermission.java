package xyz.deszaras.grounds.security;

import java.security.BasicPermission;

/**
 * A permission required to work with
 * {@link xyz.deszaras.grounds.command.CommandExecutor} when a security
 * manager is in place.
 */
public class CommandExecutorPermission extends BasicPermission {

  public CommandExecutorPermission(String name) {
    super(name);
  }

  public CommandExecutorPermission(String name, String actions) {
    super(name, actions);
  }
}
