package xyz.deszaras.grounds.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class AccessRuleKeeper {

  private final Map<UUID, Set<AccessRule>> accessRules;

  public AccessRuleKeeper() {
    accessRules = new HashMap<>();
  }

  public synchronized Map<UUID, Set<AccessRule>> getAccessRules() {
    ImmutableMap.Builder<UUID, Set<AccessRule>> b = ImmutableMap.builder();
    for (Map.Entry<UUID, Set<AccessRule>> e : accessRules.entrySet()) {
      b.put(e.getKey(), ImmutableSet.copyOf(e.getValue()));
    }
    return b.build();
  }

  public synchronized void addAccessRule(AccessRule accessRule) {
    Set<AccessRule> ruleset;
    UUID id = Objects.requireNonNull(accessRule).getAccessor();
    if (accessRules.containsKey(id)) {
      ruleset = accessRules.get(id);
    } else {
      ruleset = new HashSet<>();
      accessRules.put(id, ruleset);
    }
    ruleset.add(accessRule);
  }

  public synchronized void removeAccessRule(AccessRule accessRule) {
    UUID id = Objects.requireNonNull(accessRule).getAccessor();
    if (accessRules.containsKey(id)) {
      Set<AccessRule> ruleset = accessRules.get(id);
      ruleset.remove(accessRule);
    }
  }

  public synchronized boolean grantsAccess(Thing accessor, Attr targetAttr, AccessRule.Permission permission) {
    if (accessRules.containsKey(Thing.EVERYTHING_ID)) {
      if (hasAllowRule(accessRules.get(Thing.EVERYTHING_ID), targetAttr, permission)) {
        return true;
      }
    }
    UUID id = Objects.requireNonNull(accessor).getId();
    if (!accessRules.containsKey(id)) {
      return false;
    }
    return hasAllowRule(accessRules.get(id), targetAttr, permission);
  }

  private synchronized boolean hasAllowRule(Set<AccessRule> ruleset, Attr targetAttr, AccessRule.Permission permission) {
    for (AccessRule rule : ruleset) {
      if (targetAttr.equals(rule.getTargetAttr()) &&
          rule.allows(permission)) {
        return true;
      }
    }
    return false;
  }

}
