package xyz.deszaras.grounds.server;

/**
 * A security manager that prohibits the use of System.exit() and its ilk. This
 * prevents softcode from halting the VM. It also happens to prevent Grounds
 * proper from doing the same, but it doesn't have the need for it.
 */
public class GroundsSecurityManager extends SecurityManager {

  @Override
  public void checkExit(int status) {
    throw new SecurityException("Halting the VM is not permitted");
  }
}
