package xyz.deszaras.grounds.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AttrTest {

  @Test
  public void testFromAttrSpec() {
    String attrSpec = "a[STRING]=b";
    Attr attr = Attr.fromAttrSpec(attrSpec);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.STRING, attr.getType());
    assertEquals("b", attr.getValue());
  }
}
