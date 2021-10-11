package xyz.deszaras.grounds.combat.grapple;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import xyz.deszaras.grounds.combat.Engine;
import xyz.deszaras.grounds.combat.Npc;
import xyz.deszaras.grounds.combat.System;
import xyz.deszaras.grounds.combat.Team;

/**
 * The Grapple combat system.
 */
public class GrappleSystem implements System {

  @Override
  public Team.Builder getTeamBuilder(String name) {
    return GrappleTeam.builder(name);
  }

  @Override
  public Engine.Builder getEngineBuilder() {
    return GrappleEngine.builder();
  }

  @Override
  public GrappleEngine restore(byte[] state) {
    try {
      return GrappleEngine.fromProto(ProtoModel.Engine.parseFrom(state));
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Npc buildNpc(String name, List<String> args) {
    checkArgument(args.size() == 1,
                  "args must be length 1 (stats string)");
    return new GrappleNpc(name, args.get(0));
  }
}
