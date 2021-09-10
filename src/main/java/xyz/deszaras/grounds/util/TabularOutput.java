package xyz.deszaras.grounds.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.fusesource.jansi.Ansi;

/**
 * A helper class for producing a formatted table of information. Start by
 * defining columns, and then add rows of values. Finally, use
 * {@link #toString()} to get the table content for output.
 */
public class TabularOutput {

  private static class ColumnDef {
    private final String header;
    private final String formatString;
    private final String dataFormatString;

    private ColumnDef(String header, String formatString, String dataFormatString) {
      this.header = header;
      this.formatString = formatString;
      this.dataFormatString = dataFormatString;
    }
  }

  private List<ColumnDef> columnDefs;
  private List<List<CharSequence>> rows;

  /**
   * Start a new table.
   */
  public TabularOutput() {
    columnDefs = new ArrayList<>();
    rows = new ArrayList<>();
  }

  /**
   * Defines a new table column.
   *
   * @param  header       column header
   * @param  formatString format string for header and for column values
   * @return              this
   * @throws IllegalStateException if any rows have already been added
   */
  public TabularOutput defineColumn(String header, String formatString) {
    return defineColumn(header, formatString, formatString);
  }


  /**
   * Defines a new table column.
   *
   * @param  header           column header
   * @param  formatString     format string for header
   * @param  dataFormatString format string for data in column
   * @return                  this
   * @throws IllegalStateException if any rows have already been added
   */
  public TabularOutput defineColumn(String header, String formatString,
                                    String dataFormatString) {
    if (!rows.isEmpty()) {
      throw new IllegalStateException("Table already has rows in it");
    }
    columnDefs.add(new ColumnDef(header, formatString, dataFormatString));
    return this;
  }

  /**
   * Adds a row of data to this table.
   *
   * @param  values row values
   * @return        this
   * @throws IllegalStateException if the number of values does not match the
   *                               number of columns in the table
   */
  public TabularOutput addRow(String... values) {
    if (values.length != columnDefs.size()) {
      throw new IllegalArgumentException(
        String.format("The table has %d columns, but this row has %d values",
                      columnDefs.size(), values.length));
    }
    rows.add(Arrays.stream(values)
             .map(s -> new AnsiString(s))
             .collect(Collectors.toList()));
    return this;
  }

  /**
   * Adds a row of data to this table.
   *
   * @param  values row values
   * @return        this
   * @throws IllegalStateException if the number of values does not match the
   *                               number of columns in the table
   */
  public TabularOutput addRow(List<String> values) {
    if (values.size() != columnDefs.size()) {
      throw new IllegalArgumentException(
        String.format("The table has %d columns, but this row has %d values",
                      columnDefs.size(), values.size()));
    }
    rows.add(values.stream()
             .map(s -> new AnsiString(s))
             .collect(Collectors.toList()));
    return this;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    int numColumns = columnDefs.size();

    StringBuilder h = new StringBuilder();
    StringBuilder l = new StringBuilder();
    for (int i = 0; i < numColumns; i++) {
      ColumnDef columnDef = columnDefs.get(i);
      if (i != 0) {
        h.append(" ");
        l.append(" ");
      }
      h.append(String.format(columnDef.formatString, columnDef.header));
      l.append(String.format(columnDef.formatString, lineFor(columnDef.header)));
    }
    String hline = h.toString().stripTrailing();
    String lline = l.toString().stripTrailing();
    b.append(String.format("%s\n%s\n",
                           AnsiUtils.color(hline, Ansi.Color.CYAN, false),
                           AnsiUtils.color(lline, Ansi.Color.CYAN, false)));

    if (rows.isEmpty()) {
      b.append(AnsiUtils.color("<no data>", Ansi.Color.RED, false));
    } else {
      int numRows = rows.size();
      for (int i = 0; i < numRows; i++) {
        List<CharSequence> row = rows.get(i);
        StringBuilder r = new StringBuilder();
        for (int j = 0; j < numColumns; j++) {
          if (j != 0) {
            r.append(" ");
          }
          r.append(String.format(columnDefs.get(j).dataFormatString, row.get(j)));
        }
        b.append(r.toString().stripTrailing());
        if (i < numRows - 1) {
          b.append("\n");
        }
      }
    }

    return b.toString();
  }

  private static String lineFor(String s) {
    return "-".repeat(s.length());
  }
}
