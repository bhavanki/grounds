package xyz.deszaras.grounds.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A policy holds permissions for a thing. A permission has a category as
 * defined in the {@link Category} enum. A permission also has zero or more
 * roles that are granted it. For example, the READ permission in a thing's
 * policy is often granted to most roles, since that lets most players "see"
 * and inspect it.
 */
public class Policy {

  /**
   * The permission categories. Exactly what a category means depends on the
   * type of thing that its permission's policy is attached to.
   */
  public enum Category {
    /**
     * A general / default permission.
     */
    GENERAL,
    /**
     * A read / examine permission.
     */
    READ,
    /**
     * A write / control permission.
     */
    WRITE,
    /**
     * A use permission.
     */
    USE;
  }

  /**
   * The default policy for new things. This policy grants GENERAL, READ, and
   * USE permissions to non-guest roles, and WRITE permissions to wizard roles
   * and the OWNER role.
   */
  public static final Policy DEFAULT;
  /**
   * A policy that is wide open. All permissions are granted to non-guest
   * roles.
   */
  public static final Policy OPEN;

  static {
    DEFAULT = new Policy();
    DEFAULT.setRoles(Category.GENERAL, Role.NON_GUEST_ROLES);
    DEFAULT.setRoles(Category.READ, Role.NON_GUEST_ROLES);
    Set<Role> wizardsAndOwner = ImmutableSet.<Role>builder()
        .addAll(Role.WIZARD_ROLES)
        .add(Role.OWNER)
        .build();
    DEFAULT.setRoles(Category.WRITE, wizardsAndOwner);
    DEFAULT.setRoles(Category.USE, Role.NON_GUEST_ROLES);

    OPEN = new Policy();
    for (Category c : Category.values()) {
      OPEN.setRoles(c, Role.NON_GUEST_ROLES);
    }
  }

  private final Map<Category, ImmutableSet<Role>> permissions;

  /**
   * Creates a new empty policy, containing no permissions.
   */
  public Policy() {
    permissions = new HashMap<>();
  }

  @JsonCreator
  public Policy(
      @JsonProperty("permissions") Map<Category, Set<Role>> permissions) {
    this();
    permissions.entrySet().forEach(e ->
        this.permissions.put(e.getKey(), ImmutableSet.copyOf(e.getValue()))
    );
  }

  /**
   * Gets all of the permissions in this policy.
   *
   * @return permissions
   */
  @JsonProperty
  public synchronized Map<Category, ImmutableSet<Role>> getPermissions() {
    return ImmutableMap.copyOf(permissions);
  }

  /**
   * Gets the roles permitted for a category, i.e., the roles granted a
   * permission in this policy.
   *
   * @param category category
   * @return roles permitted for the category
   */
  public synchronized Set<Role> getRoles(Category category) {
    return permissions.getOrDefault(category, ImmutableSet.of());
  }

  /**
   * Sets the roles permitted for a category, i.e., the roles granted a
   * permission in this policy.
   *
   * @param category category
   * @param roles roles permitted for the category
   */
  public synchronized void setRoles(Category category, Set<Role> roles) {
    permissions.put(category, ImmutableSet.copyOf(roles));
  }

  /**
   * Gets the categories (permissions) for which the given role is permitted.
   *
   * @param role role
   * @return permitted categories
   */
  public synchronized Set<Category> getCategories(Role role) {
    return ImmutableSet.copyOf(permissions.entrySet().stream()
        .filter(e -> e.getValue().contains(role))
        .map(e -> e.getKey())
        .collect(Collectors.toSet()));
  }

  /**
   * Checks if any role in the given set of roles is permitted for a
   * category, i.e., if any role has been granted a permission.
   *
   * @param category category to check permission for
   * @param roles roles to check permission for
   * @return true if any role is permitted for the category
   */
  public synchronized boolean passes(Category category, Set<Role> roles) {
    Set<Role> permittedRoles = getRoles(category);
    return roles.stream().filter(r -> permittedRoles.contains(r)).count() > 0;
  }
}
