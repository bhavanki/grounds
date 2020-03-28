package xyz.deszaras.grounds.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class AttrTest {

  private Attr attr;

  @Test
  public void testStringAttr() {
    attr = new Attr("a", "b");
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.STRING, attr.getType());
    assertEquals("b", attr.getValue());
  }

  @Test
  public void testIntAttr() {
    attr = new Attr("a", 42);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.INTEGER, attr.getType());
    assertEquals(42, attr.getIntValue());
  }

  @Test
  public void testBooleanAttr() {
    attr = new Attr("a", true);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.BOOLEAN, attr.getType());
    assertTrue(attr.getBooleanValue());
  }

  @Test
  public void testAttrAttr() {
    attr = new Attr("a", new Attr("x", "b"));
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.ATTR, attr.getType());
    assertEquals("x", attr.getAttrValue().getName());
    assertEquals("b", attr.getAttrValue().getValue());
  }

  @Test
  public void testAttrListAttr() {
    List<Attr> setAttrs = new ArrayList<>();
    setAttrs.add(new Attr("x1", "b1"));
    setAttrs.add(new Attr("x2", "b2"));
    attr = new Attr("a", setAttrs);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.ATTRLIST, attr.getType());
    assertEquals(setAttrs, attr.getAttrListValue());
  }

  @Test
  public void testFromAttrSpec() {
    String attrSpec = "a[STRING]=b";
    attr = Attr.fromAttrSpec(attrSpec);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.STRING, attr.getType());
    assertEquals("b", attr.getValue());
  }
}
