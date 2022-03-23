package xyz.deszaras.grounds.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.fusesource.jansi.Ansi;

/**
 * A helper class for producing a formatted presentation of a data record,
 * predominantly as key/value pairs. Add each key/value pair as a field, and
 * finally use {@link #toString()} to get the record content for output.
 */
public class RecordOutput {

  private List<String> keys;
  private List<String> values;

  /**
   * Start a new record.
   */
  public RecordOutput() {
    keys = new ArrayList<>();
    values = new ArrayList<>();
  }

  /**
   * Adds a new key/value field. Pass a null key to have the value alone in
   * the output.
   *
   * @param  key   key
   * @param  value value
   * @return       this
   */
  public RecordOutput addField(String key, String value) {
    if (key != null) {
      keys.add(AnsiUtils.color(key + ":", Ansi.Color.CYAN, false));
    } else {
      keys.add(null);
    }
    values.add(value);
    return this;
  }

  /**
   * Adds a value with no corresponding key. The value is emitted alone in
   * the output.
   *
   * @param  value value
   * @return       this
   */
  public RecordOutput addValue(String value) {
    return addField(null, value);
  }

  /**
   * Adds a blank line to the output (equivalent to an empty string value with
   * no key).
   *
   * @return       this
   */
  public RecordOutput addBlankLine() {
    return addField(null, "");
  }

  @Override
  public String toString() {
    Optional<Integer> maxKeyLength = keys.stream()
        .filter(k -> k != null)
        .map(k -> k.length())
        .collect(Collectors.maxBy((a, b) -> Integer.compare(a, b)));
    if (maxKeyLength.isEmpty()) {
      return "";
    }
    int keyColumnSize = maxKeyLength.get();
    String rowFormat = String.format("%%-%d.%ds %%s\n",
                                     keyColumnSize, keyColumnSize);

    StringBuilder b = new StringBuilder();
    int numRows = keys.size();
    for (int i = 0; i < numRows; i++) {
      String key = keys.get(i);
      if (key != null) {
        b.append(String.format(rowFormat, key, values.get(i)));
      } else {
        b.append(String.format("%s\n", values.get(i)));
      }
    }

    return b.toString();
  }

  /**
   * Creates an output from a map, instead of building it piecemeal. The map
   * must have a "keys" row for the list of keys and a "values" row for the list
   * of values (so the lists must have the same size). Use an empty string in
   * the "keys" list for a null record key.
   *
   * @param  m map of keys and values
   * @return   new output
   * @throws IllegalArgumentException if keys or values are missing, or if the
   *                                  lists are different sizes
   */
  public static RecordOutput from(Map<String, List<String>> m) {
    RecordOutput rec = new RecordOutput();
    if (m == null) {
      return rec;
    }

    if (!m.containsKey("keys")) {
      throw new IllegalArgumentException("Map must contain a keys list");
    }
    if (!m.containsKey("values")) {
      throw new IllegalArgumentException("Map must contain a values list");
    }

    List<String> keys = m.get("keys");
    List<String> values = m.get("values");
    if (keys.size() != values.size()) {
      throw new IllegalArgumentException("Spec has " + keys.size() +
                                         " keys but " + values.size() +
                                         " values");
    }

    for (int i = 0; i < keys.size(); i++) {
      String key = keys.get(i);
      String value = values.get(i);
      if (!key.isEmpty()) {
        rec.addField(key, value);
      } else {
        rec.addField(null, value);
      }
    }
    return rec;
  }
}
