package xyz.deszaras.grounds.combat.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StatsTest {

  private BaseStats bs;

  @BeforeEach
  public void setUp() {
    // need to use real skills
    bs = BaseStats.builder()
      .skill(Skills.ENDURANCE, 2)
      .skill(Skills.COURAGE, 3)
      .skill(Skills.ACCURACY, 4)
      .apMaxSize(10)
      .defense(2)
      .maxWounds(3)
      .build();
  }

  @Test
  public void testFromProtoWithNoDecorators() {
    ProtoModel.Stats ps = bs.toProto();

    Stats s2 = Stats.fromProto(ps);
    assertTrue(s2 instanceof BaseStats);
    assertEquals(10, ((BaseStats) s2).getApMaxSize());
  }

  @Test
  public void testFromProtoWithDecorators() {
    // new to use real decorators
    Stats s = new StatsDecorators.DefenseBonus(bs, 1);
    s = new StatsDecorators.StrikeBonus(s, 2);
    s = new StatsDecorators.ApMaxSizeBonus(s, 3);

    ProtoModel.Stats ps = s.toProto();
    Stats s2 = Stats.fromProto(ps);

    assertTrue(s2 instanceof StatsDecorators.ApMaxSizeBonus);
    assertEquals(3, ((StatsDecorators.ApMaxSizeBonus) s2).getDelta());

    s2 = ((StatsDecorator) s2).getDelegate();
    assertTrue(s2 instanceof StatsDecorators.StrikeBonus);
    assertEquals(2, ((StatsDecorators.StrikeBonus) s2).getDelta());

    s2 = ((StatsDecorator) s2).getDelegate();
    assertTrue(s2 instanceof StatsDecorators.DefenseBonus);
    assertEquals(1, ((StatsDecorators.DefenseBonus) s2).getDelta());

    s2 = ((StatsDecorator) s2).getDelegate();
    assertTrue(s2 instanceof BaseStats);
    assertEquals(10, ((BaseStats) s2).getApMaxSize());
  }
}
