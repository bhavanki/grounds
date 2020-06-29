package xyz.deszaras.grounds.command;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import xyz.deszaras.grounds.auth.Role;

/**
 * Enumerates the roles of players who may execute a command. The role check is
 * performed before execution of the command implementation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface PermittedRoles {
  /**
   * Returns the permitted roles.
   *
   * @return permitted roles
   */
  Role[] roles();
  /**
   * Returns the failure message to be reported to the player when they lack
   * permission to execute a command. Optional; default is "Permission denied".
   *
   * @return failure message
   */
  String failureMessage() default "Permission denied";
}
