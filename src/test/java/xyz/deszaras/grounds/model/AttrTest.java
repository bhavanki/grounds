package xyz.deszaras.grounds.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
  public void testThingAttr() {
    Thing t = new Thing("something", new Universe("test"));
    attr = new Attr("a", t);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.THING, attr.getType());
    assertEquals(t.getId().toString(), attr.getThingValue());
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
  public void testAttrListAttrAsMap() {
    List<Attr> setAttrs = new ArrayList<>();
    setAttrs.add(new Attr("x1", "b1"));
    setAttrs.add(new Attr("x2", "b2"));
    attr = new Attr("a", setAttrs);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.ATTRLIST, attr.getType());
    Map<String, Attr> m = attr.getAttrListValueAsMap();
    assertEquals(2, m.size());
    assertEquals("b1", m.get("x1").getValue());
    assertEquals("b2", m.get("x2").getValue());
  }

  @Test
  public void testFromAttrSpec() {
    String attrSpec = "a[STRING]=b";
    attr = Attr.fromAttrSpec(attrSpec);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.STRING, attr.getType());
    assertEquals("b", attr.getValue());
  }

  @Test
  public void testFromAttrSpecJsonFile() throws Exception {
    String attrSpec = "a[ATTR]=@" +
        Paths.get(ClassLoader.getSystemResource("attrvalue.json").toURI());
    attr = Attr.fromAttrSpec(attrSpec);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.ATTR, attr.getType());
    Attr a2 = attr.getAttrValue();
    assertEquals("key1", a2.getName());
    assertEquals(Attr.Type.STRING, a2.getType());
    assertEquals("value1", a2.getValue());
  }

  @Test
  public void testFromAttrSpecYamlFile() throws Exception {
    String attrSpec = "a[ATTR]=@" +
        Paths.get(ClassLoader.getSystemResource("attrvalue.yaml").toURI());
    attr = Attr.fromAttrSpec(attrSpec);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.ATTR, attr.getType());
    Attr a2 = attr.getAttrValue();
    assertEquals("key1", a2.getName());
    assertEquals(Attr.Type.STRING, a2.getType());
    assertEquals("value1", a2.getValue());
  }

  @Test
  public void testToAttrSpec() {
    attr = new Attr("a", "b");
    assertEquals("a[STRING]=b", attr.toAttrSpec());
  }
}
