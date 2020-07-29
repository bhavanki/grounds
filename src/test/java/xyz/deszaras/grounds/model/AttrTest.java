package xyz.deszaras.grounds.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

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
    Thing t = new Thing("something");
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
  public void testAttrInAttrListValue() {
    List<Attr> setAttrs = new ArrayList<>();
    setAttrs.add(new Attr("x1", "b1"));
    setAttrs.add(new Attr("x2", "b2"));
    attr = new Attr("a", setAttrs);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.ATTRLIST, attr.getType());


    Optional<Attr> subAttr = attr.getAttrInAttrListValue("x1");
    assertTrue(subAttr.isPresent());
    assertEquals("x1", subAttr.get().getName());
    assertEquals("b1", subAttr.get().getValue());

    subAttr = attr.getAttrInAttrListValue("x3");
    assertFalse(subAttr.isPresent());
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
  public void testFromStringAttrSpec() {
    String attrSpec = "a=b";
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

  @Test
  public void testFromJson() {
    String attrJson = "{\"name\": \"a\", \"type\": \"STRING\", \"value\": \"b\"}";
    attr = Attr.fromJson(attrJson);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.STRING, attr.getType());
    assertEquals("b", attr.getValue());
  }

  @Test
  public void testFromJsonNoType() {
    String attrJson = "{\"name\": \"a\", \"value\": \"b\"}";
    attr = Attr.fromJson(attrJson);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.STRING, attr.getType());
    assertEquals("b", attr.getValue());
  }

  @Test
  public void testFromJsonYaml() {
    String attrYaml =
        "name: a\n" +
        "type: STRING\n" +
        "value: b";
    attr = Attr.fromJson(attrYaml);
    assertEquals("a", attr.getName());
    assertEquals(Attr.Type.STRING, attr.getType());
    assertEquals("b", attr.getValue());
  }

  @Test
  public void testListFromJson() {
    String listAttrJson = "[" +
        "{\"name\": \"a1\", \"type\": \"STRING\", \"value\": \"b1\"}," +
        "{\"name\": \"a2\", \"type\": \"STRING\", \"value\": \"b2\"}" +
        "]";
    List<Attr> attrs = Attr.listFromJson(listAttrJson);
    assertEquals(2, attrs.size());
    assertTrue(attrs.stream().anyMatch(a -> a.getName().equals("a1") && a.getValue().equals("b1")));
    assertTrue(attrs.stream().anyMatch(a -> a.getName().equals("a2") && a.getValue().equals("b2")));
  }

  @Test
  public void testListFromJsonYaml() {
    String listAttrYaml =
        "- name: a1\n" +
        "  type: STRING\n" +
        "  value: b1\n" +
        "- name: a2\n" +
        "  type: STRING\n" +
        "  value: b2\n";
    List<Attr> attrs = Attr.listFromJson(listAttrYaml);
    assertEquals(2, attrs.size());
    assertTrue(attrs.stream().anyMatch(a -> a.getName().equals("a1") && a.getValue().equals("b1")));
    assertTrue(attrs.stream().anyMatch(a -> a.getName().equals("a2") && a.getValue().equals("b2")));
  }
}
