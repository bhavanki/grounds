package xyz.deszaras.grounds.security;

import java.security.BasicPermission;

/**
 * A permission required to work with
 * {@link xyz.deszaras.grounds.server.ActorDatabase} when a security manager is
 * in place.
 */
public class ActorDatabasePermission extends BasicPermission {

  public ActorDatabasePermission(String name) {
    super(name);
  }

  public ActorDatabasePermission(String name, String actions) {
    super(name, actions);
  }
}
