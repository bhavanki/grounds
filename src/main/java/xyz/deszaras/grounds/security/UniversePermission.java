package xyz.deszaras.grounds.security;

import java.security.BasicPermission;

/**
 * A permission required to work with
 * {@link xyz.deszaras.grounds.model.Universe} when a security manager is in
 * place.
 */
public class UniversePermission extends BasicPermission {

  public UniversePermission(String name) {
    super(name);
  }

  public UniversePermission(String name, String actions) {
    super(name, actions);
  }
}
