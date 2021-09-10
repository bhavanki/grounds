package xyz.deszaras.grounds.combat;

import com.google.common.base.Splitter;

import java.util.List;
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
public class Npc extends Player {

  private static final UnsupportedOperationException THIS_IS_AN_NPC =
      new UnsupportedOperationException("This is an NPC");

  /**
   * Creates a new NPC.
   *
   * @param  name      NPC name
   * @param  statsSpec combat stats
   */
  public Npc(String name, String statsSpec) {
    super(name);
    populateCombatAttributes(Objects.requireNonNull(statsSpec));
  }

  private static final Splitter STATS_SPEC_SPLITTER =
      Splitter.on(":").limit(8).trimResults();

  private void populateCombatAttributes(String statsSpec) {
    List<String> parts = STATS_SPEC_SPLITTER.splitToList(statsSpec);
    if (parts.size() != 8) {
      throw new IllegalArgumentException("statsSpec must have eight parts");
    }

    boolean hasSkill = false;
    if (!parts.get(0).isEmpty()) {
      hasSkill = true;
      setAttr(Engine.ATTR_NAME_SKILL_4, parts.get(0));
    }
    if (!parts.get(1).isEmpty()) {
      hasSkill = true;
      setAttr(Engine.ATTR_NAME_SKILL_3, parts.get(1));
    }
    if (!parts.get(2).isEmpty()) {
      hasSkill = true;
      setAttr(Engine.ATTR_NAME_SKILL_2, parts.get(2));
    }
    if (!hasSkill) {
      throw new IllegalArgumentException("statsSpec must name at least one skill");
    }

    setAttr(Engine.ATTR_NAME_AP_MAX_SIZE, Integer.parseInt(parts.get(3)));
    setAttr(Engine.ATTR_NAME_DEFENSE, Integer.parseInt(parts.get(4)));
    setAttr(Engine.ATTR_NAME_MAX_WOUNDS, Integer.parseInt(parts.get(5)));
    setAttr(Engine.ATTR_NAME_AD, Integer.parseInt(parts.get(6)));
    setAttr(Engine.ATTR_NAME_SD, Integer.parseInt(parts.get(7)));
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
}
