package xyz.deszaras.grounds.combat.grapple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StatsDecoratorTest {

  private static class TestBonus extends StatsDecorator {
    private final int x;
    private final int y;

    TestBonus(Stats delegate, int x, int y) {
      super(delegate);
      this.x = x;
      this.y = y;
    }

    @Override
    protected List<Integer> getBuildIntList() {
      return List.of(x, y);
    }
  }

  private Stats delegate;
  private TestBonus decorator;

  @BeforeEach
  public void setUp() throws Exception {
    delegate = BaseStats.builder()
      .skill(Skills.ENDURANCE, 2)
      .skill(Skills.COURAGE, 3)
      .skill(Skills.ACCURACY, 4)
      .apMaxSize(10)
      .defense(2)
      .maxWounds(3)
      .build();
    decorator = new TestBonus(delegate, 1, 2);
  }

  @Test
  public void testToDecoratorProto() {
    ProtoModel.StatsDecorator psd = decorator.toDecoratorProto();
    assertEquals("TestBonus", psd.getName());
    assertEquals(List.of(1, 2), psd.getBuildArgsList());
  }

  @Test
  public void testToProtoWithOneDecorator() {
    ProtoModel.Stats ps = decorator.toProto();
    assertEquals(10, ps.getBaseStats().getApMaxSize());

    assertEquals(1, ps.getStatsDecoratorsCount());
    ProtoModel.StatsDecorator psd = ps.getStatsDecorators(0);
    assertEquals("TestBonus", psd.getName());
    assertEquals(List.of(1, 2), psd.getBuildArgsList());
  }

  @Test
  public void testToProtoWithMultipleDecorators() {
    decorator = new TestBonus(decorator, 3, 4);
    decorator = new TestBonus(decorator, 5, 6);
    ProtoModel.Stats ps = decorator.toProto();
    assertEquals(10, ps.getBaseStats().getApMaxSize());

    assertEquals(3, ps.getStatsDecoratorsCount());

    ProtoModel.StatsDecorator psd = ps.getStatsDecorators(0);
    assertEquals("TestBonus", psd.getName());
    assertEquals(List.of(1, 2), psd.getBuildArgsList());
    psd = ps.getStatsDecorators(1);
    assertEquals("TestBonus", psd.getName());
    assertEquals(List.of(3, 4), psd.getBuildArgsList());
    psd = ps.getStatsDecorators(2);
    assertEquals("TestBonus", psd.getName());
    assertEquals(List.of(5, 6), psd.getBuildArgsList());
  }
}
