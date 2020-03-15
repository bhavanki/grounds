package xyz.deszaras.grounds.model;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

public class ThingClass {

  public class AttrDef {

    private final String name;
    private final Attr.Type type;
    private final boolean required;

    public AttrDef(String name, Attr.Type type, boolean required) {
      this.name = Objects.requireNonNull(name);
      this.type = Objects.requireNonNull(type);
      this.required = required;
    }

    public String getName() {
      return name;
    }

    public Attr.Type getType() {
      return type;
    }

    public boolean isRequired() {
      return required;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) return true;
      if (other == null) return false;
      if (!other.getClass().equals(AttrDef.class)) {
        return false;
      }

      AttrDef o = (AttrDef) other;
      return name.equals(o.name) && type.equals(o.type) && required == o.required;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, type, required);
    }
  }

  private final String name;
  private final Set<AttrDef> attrDefs;

  public ThingClass(String name, Set<AttrDef> attrDefs) {
    this.name = Objects.requireNonNull(name);
    this.attrDefs = attrDefs != null ? ImmutableSet.copyOf(attrDefs) : ImmutableSet.of();
  }

  public String getName() {
    return name;
  }

  public Set<AttrDef> getAttrDefs() {
    return attrDefs;
  }
}
