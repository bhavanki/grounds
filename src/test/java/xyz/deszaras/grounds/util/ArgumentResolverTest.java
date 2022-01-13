package xyz.deszaras.grounds.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import xyz.deszaras.grounds.model.Place;
import xyz.deszaras.grounds.model.Player;
import xyz.deszaras.grounds.model.Thing;
import xyz.deszaras.grounds.model.Universe;

public class ArgumentResolverTest {

  private Universe universe;
  private Player contextPlayer;
  private Place contextPlayerLocation;
  private Thing keys;
  private Thing wallet;

  @BeforeEach
  public void setUp() throws Exception {
    universe = new Universe("test");
    Universe.setCurrent(universe);

    contextPlayer = new Player("context");
    universe.addThing(contextPlayer);
    contextPlayerLocation = new Place("contextLocation");
    universe.addThing(contextPlayerLocation);

    keys = new Thing("keys");
    universe.addThing(keys);
    wallet = new Thing("wallet");
    universe.addThing(wallet);
  }

  @Test
  public void testResolveMe() throws Exception {
    Player me = ArgumentResolver.INSTANCE.resolve("me", Player.class, contextPlayer);

    assertEquals(contextPlayer, me);
  }

  @Test
  public void testResolveMeFailureWrongType() throws Exception {
    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve("me", Place.class, contextPlayer));
  }

  @Test
  public void testResolveContextThingId() throws Exception {
    Player me = ArgumentResolver.INSTANCE.resolve(contextPlayer.getId().toString(),
                                                  Player.class, contextPlayer);

    assertEquals(contextPlayer, me);
  }

  @Test
  public void testResolveContextThingIdFailureWrongType() throws Exception {
    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve(contextPlayer.getId().toString(),
                                                         Place.class, contextPlayer));
  }

  @Test
  public void testResolveHere() throws Exception {
    contextPlayerLocation.give(contextPlayer);
    contextPlayer.setLocation(contextPlayerLocation);

    Place here = ArgumentResolver.INSTANCE.resolve("here", Place.class, contextPlayer);

    assertEquals(contextPlayerLocation, here);
  }

  @Test
  public void testResolveHereFailureWrongType() throws Exception {
    contextPlayerLocation.give(contextPlayer);
    contextPlayer.setLocation(contextPlayerLocation);

    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve("here", Player.class, contextPlayer));
  }

  @Test
  public void testResolveContextThingLocation() throws Exception {
    contextPlayerLocation.give(contextPlayer);
    contextPlayer.setLocation(contextPlayerLocation);

    Place here = ArgumentResolver.INSTANCE.resolve(contextPlayerLocation.getId().toString(),
                                                   Place.class, contextPlayer);

    assertEquals(contextPlayerLocation, here);
  }

  @Test
  public void testResolveContextThingLocationFailureWrongType() throws Exception {
    contextPlayerLocation.give(contextPlayer);
    contextPlayer.setLocation(contextPlayerLocation);

    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve(contextPlayerLocation.getId().toString(),
                                                         Player.class, contextPlayer));
  }

  @Test
  public void testResolveNameAmongPossessions() throws Exception {
    contextPlayer.give(keys);
    keys.setLocation(contextPlayer);
    contextPlayer.give(wallet);
    wallet.setLocation(contextPlayer);

    Thing resolved = ArgumentResolver.INSTANCE.resolve("keys", Thing.class, contextPlayer);
    assertEquals(keys, resolved);

    resolved = ArgumentResolver.INSTANCE.resolve("wallet", Thing.class, contextPlayer);
    assertEquals(wallet, resolved);
  }

  @Test
  public void testResolveNameAmongPossessionsFailureWrongType() throws Exception {
    contextPlayer.give(keys);
    keys.setLocation(contextPlayer);

    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve("keys", Player.class, contextPlayer));
  }

  @Test
  public void testResolveIdAmongPossessions() throws Exception {
    contextPlayer.give(keys);
    keys.setLocation(contextPlayer);
    contextPlayer.give(wallet);
    wallet.setLocation(contextPlayer);

    Thing resolved = ArgumentResolver.INSTANCE.resolve(keys.getId().toString(),
                                                       Thing.class, contextPlayer);
    assertEquals(keys, resolved);

    resolved = ArgumentResolver.INSTANCE.resolve(wallet.getId().toString(),
                                                 Thing.class, contextPlayer);
    assertEquals(wallet, resolved);
  }

  @Test
  public void testResolveIdAmongPossessionsFailureWrongType() throws Exception {
    contextPlayer.give(keys);
    keys.setLocation(contextPlayer);

    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve(keys.getId().toString(),
                                                         Player.class, contextPlayer));
  }

  @Test
  public void testResolveNameAmongNearby() throws Exception {
    contextPlayerLocation.give(contextPlayer);
    contextPlayer.setLocation(contextPlayerLocation);
    contextPlayerLocation.give(keys);
    keys.setLocation(contextPlayerLocation);
    contextPlayerLocation.give(wallet);
    wallet.setLocation(contextPlayerLocation);

    Thing resolved = ArgumentResolver.INSTANCE.resolve("keys", Thing.class, contextPlayer);
    assertEquals(keys, resolved);

    resolved = ArgumentResolver.INSTANCE.resolve("wallet", Thing.class, contextPlayer);
    assertEquals(wallet, resolved);
  }

  @Test
  public void testResolveNameAmongNearbyFailureWrongType() throws Exception {
    contextPlayerLocation.give(contextPlayer);
    contextPlayer.setLocation(contextPlayerLocation);
    contextPlayerLocation.give(keys);
    keys.setLocation(contextPlayerLocation);

    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve("keys", Player.class, contextPlayer));
  }

  @Test
  public void testResolveIdAmongNearby() throws Exception {
    contextPlayerLocation.give(contextPlayer);
    contextPlayer.setLocation(contextPlayerLocation);
    contextPlayerLocation.give(keys);
    keys.setLocation(contextPlayerLocation);
    contextPlayerLocation.give(wallet);
    wallet.setLocation(contextPlayerLocation);

    Thing resolved = ArgumentResolver.INSTANCE.resolve(keys.getId().toString(),
                                                       Thing.class, contextPlayer);
    assertEquals(keys, resolved);

    resolved = ArgumentResolver.INSTANCE.resolve(wallet.getId().toString(),
                                                 Thing.class, contextPlayer);
    assertEquals(wallet, resolved);
  }

  @Test
  public void testResolveIdAmongNearbyFailureWrongType() throws Exception {
    contextPlayerLocation.give(contextPlayer);
    contextPlayer.setLocation(contextPlayerLocation);
    contextPlayerLocation.give(keys);
    keys.setLocation(contextPlayerLocation);

    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve(keys.getId().toString(),
                                                         Player.class, contextPlayer));
  }

  @Test
  public void testResolveNameGlobally() throws Exception {
    Thing resolved = ArgumentResolver.INSTANCE.resolve("keys", Thing.class, contextPlayer, true);
    assertEquals(keys, resolved);
  }


  @Test
  public void testResolveIdGlobally() throws Exception {
    Thing resolved = ArgumentResolver.INSTANCE.resolve(keys.getId().toString(),
                                                       Thing.class, contextPlayer, true);
    assertEquals(keys, resolved);
  }

  @Test
  public void testResolveFailureAll() throws Exception {
    contextPlayerLocation.give(contextPlayer);
    contextPlayer.setLocation(contextPlayerLocation);
    contextPlayerLocation.give(keys);
    keys.setLocation(contextPlayerLocation);
    contextPlayerLocation.give(wallet);
    wallet.setLocation(contextPlayerLocation);

    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve("tesseract",
                                                         Thing.class, contextPlayer, true));
    assertThrows(ArgumentResolverException.class,
                 () -> ArgumentResolver.INSTANCE.resolve(UUID.randomUUID().toString(),
                                                         Thing.class, contextPlayer, true));
  }
}
