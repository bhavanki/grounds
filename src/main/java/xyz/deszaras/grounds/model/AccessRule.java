package xyz.deszaras.grounds.model;

import java.util.Objects;
import java.util.UUID;

public final class AccessRule {

  public enum Permission {
    READ,
    WRITE;
  }

  private final UUID accessor;
  private final Attr targetAttr;
  private final Permission permission;

  public AccessRule(Thing accessor, Attr targetAttr, Permission permission) {
    this(Objects.requireNonNull(accessor).getId(), targetAttr, permission);
  }

  public AccessRule(UUID accessor, Attr targetAttr, Permission permission) {
    this.accessor = accessor;
    this.targetAttr = Objects.requireNonNull(targetAttr);
    this.permission = Objects.requireNonNull(permission);
  }

  public UUID getAccessor() {
    return accessor;
  }

  public Attr getTargetAttr() {
    return targetAttr;
  }

  public boolean allowsRead() {
    return true;
  }

  public boolean allowsWrite() {
    return permission == Permission.WRITE;
  }

  public boolean allows(Permission permission) {
    return this.permission == Permission.WRITE || permission == Permission.READ;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (!other.getClass().equals(AccessRule.class)) {
      return false;
    }

    AccessRule o = (AccessRule) other;
    return accessor.equals(o.accessor) && targetAttr.equals(o.targetAttr) &&
        permission == o.permission;
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessor, targetAttr, permission);
  }
}
