package xyz.deszaras.grounds.combat;

import java.util.Objects;
import java.util.Optional;

import xyz.deszaras.grounds.command.Actor;
import xyz.deszaras.grounds.command.Message;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;

/**
 * An NPC is a stub player for use in combat. Most normal player functionality
 * doesn't work for an NPC.
 */
public abstract class Npc extends Player {

  private static final UnsupportedOperationException THIS_IS_AN_NPC =
      new UnsupportedOperationException("This is an NPC");

  /**
   * Creates a new NPC.
   *
   * @param  name      NPC name
   */
  protected Npc(String name) {
    super(name);
  }

  @Override
  public Optional<Place> getLocationAsPlace() {
    throw THIS_IS_AN_NPC;
  }

  @Override
  public void setCurrentActor(Actor actor) {
    throw THIS_IS_AN_NPC;
  }

  @Override
  public boolean trySetCurrentActor(Actor actor) {
    throw THIS_IS_AN_NPC;
  }

  @Override
  public void sendMessage(Message message) {
  }

  @Override
  public Message getNextMessage() {
    throw THIS_IS_AN_NPC;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Npc that = (Npc) o;
    return getName().equals(that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName());
  }
}
