package xyz.deszaras.grounds.command;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import xyz.deszaras.grounds.auth.Policy;
import xyz.deszaras.grounds.auth.Role;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;

/**
 * Changes the policy on a thing.<p>
 *
 * Arguments: thing, change instruction<br>
 * Checks: player is GOD or THAUMATURGE in the thing's universe
 */
public class ChangePolicyCommand extends Command<String> {

  @VisibleForTesting
  static class ChangeInstruction {

    final Map<Policy.Category, Set<Role>> rolesToAdd;
    final Map<Policy.Category, Set<Role>> rolesToRemove;

    ChangeInstruction(String s) {
      rolesToAdd = new HashMap<>();
      rolesToRemove = new HashMap<>();

      for (String mod : Objects.requireNonNull(s).split(",")) {
        mod = mod.trim();
        if (mod.length() < 3) {
          throw new IllegalArgumentException("Invalid modification: " + mod);
        }

        Policy.Category c;
        switch (mod.charAt(0)) {
          case 'g':
            c = Policy.Category.GENERAL;
            break;
          case 'r':
            c = Policy.Category.READ;
            break;
          case 'w':
            c = Policy.Category.WRITE;
            break;
          case 'u':
            c = Policy.Category.USE;
            break;
          default:
            throw new IllegalArgumentException("Invalid policy category in " + mod);
        }

        Set<Role> rs = new HashSet<>();
        for (char rc : mod.substring(2).toCharArray()) {
          switch (rc) {
            case 'g':
              rs.add(Role.GUEST);
              break;
            case 'd':
              rs.add(Role.DENIZEN);
              break;
            case 'o':
              rs.add(Role.OWNER);
              break;
            case 'B':
              rs.add(Role.BARD);
              break;
            case 'A':
              rs.add(Role.ADEPT);
              break;
            case 'T':
              rs.add(Role.THAUMATURGE);
              break;
            default:
              throw new IllegalArgumentException("Invalid role in " + mod);
          }
        }

        switch (mod.charAt(1)) {
          case '+':
            rolesToAdd.put(c, rs);
            break;
          case '-':
            rolesToRemove.put(c, rs);
            break;
          default:
            throw new IllegalArgumentException("Invalid operator in " + mod);
        }
      }

      for (Policy.Category c : Policy.Category.values()) {
        Set<Role> repeatedRoles = new HashSet<>(rolesToAdd.getOrDefault(c, Set.of()));
        repeatedRoles.retainAll(rolesToRemove.getOrDefault(c, Set.of()));
        if (!repeatedRoles.isEmpty()) {
          throw new IllegalArgumentException("Invalid instruction '" + s + "': " +
                                             "both adds and removes " + repeatedRoles +
                                             " for category " + c);
        }
      }
    }
  }

  private final Thing thing;
  private final ChangeInstruction changeInstruction;

  public ChangePolicyCommand(Actor actor, Player player, Thing thing,
                             ChangeInstruction changeInstruction) {
    super(actor, player);
    this.thing = Objects.requireNonNull(thing);
    this.changeInstruction = Objects.requireNonNull(changeInstruction);
  }


  @Override
  public String execute() throws CommandException {
    checkMayChangePolicy();
    // FUTURE: if allowing lower-level wizards, check for privilege escalation

    Policy policy = thing.getPolicy();
    synchronized (policy) {
      for (Policy.Category category : Policy.Category.values()) {
        Set<Role> roles = new HashSet<>(policy.getRoles(category));
        roles.addAll(changeInstruction.rolesToAdd.getOrDefault(category, Set.of()));
        roles.removeAll(changeInstruction.rolesToRemove.getOrDefault(category, Set.of()));
        policy.setRoles(category, roles);
      }
    }

    return policy.toString();
  }

  private void checkMayChangePolicy() throws CommandException {
    if (player.equals(Player.GOD)) {
      return;
    }
    Set<Role> roles = thing.getUniverse().getRoles(player);
    if (roles.contains(Role.THAUMATURGE)) {
      return;
    }
    throw new PermissionException("You are not a thaumaturge in this universe, " +
                                  "so you may not change policies on things");
  }

  public static ChangePolicyCommand newCommand(Actor actor, Player player,
                                               List<String> commandArgs)
      throws CommandFactoryException {
    ensureMinArgs(commandArgs, 2);
    Thing policyThing =
        CommandArgumentResolver.INSTANCE.resolve(commandArgs.get(0), Thing.class, player);
    ChangeInstruction changeInstruction;
    try {
      changeInstruction = new ChangeInstruction(commandArgs.get(1));
    } catch (IllegalArgumentException e) {
      throw new CommandFactoryException("Failed to create change instruction", e);
    }
    return new ChangePolicyCommand(actor, player, policyThing, changeInstruction);
  }

  public static String help() {
    return "CHANGE_POLICY <thing> <instruction>\n\n" +
        "Changes the policy on a thing\n\n" +
        "An instruction is a comma-separated list of modifications.\n" +
        "A modification has the following form:\n" +
        "    <category><+|-><roles>\n" +
        "- <category> is a character:\n" +
        "    g = GENERAL   r = READ      w = WRITE     u = USE\n" +
        "- + indicates to add roles, - indicated to remove roles\n" +
        "- <roles> is one or more characters (in any order):\n" +
        "    g = GUEST     d = DENIZEN   o = OWNER\n" +
        "    B = BARD      A = ADEPT     T = THAUMATURGE\n\n" +
        "Examples:\n" +
        "- g+g = add GUEST to GENERAL\n" +
        "- w-BA,r+g = remove BARD and ADEPT from WRITE, add GUEST to READ";
  }

}