package xyz.deszaras.grounds.command.combat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import xyz.deszaras.grounds.combat.Combat;
import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Universe;

public final class CombatCommandTestUtils {

  private CombatCommandTestUtils() {
  }

  static Combat initTestCombat(String name, Place location, Universe testUniverse)
      throws Exception {
    Combat newCombat = mock(Combat.class);
    when(newCombat.getId()).thenReturn(UUID.randomUUID());
    when(newCombat.getName()).thenReturn(name);
    testUniverse.addThing(newCombat);
    when(newCombat.getLocation()).thenReturn(Optional.of(location));
    location.give(newCombat);
    return newCombat;
  }

}
