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
 * Checks: player is GOD or THAUMATURGE
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
    checkIfAnyRole("You are not a thaumaturge, so you may not " +
                   "change policies on things", Role.THAUMATURGE);
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
}
