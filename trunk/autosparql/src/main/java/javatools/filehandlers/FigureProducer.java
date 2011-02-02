package javatools.filehandlers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javatools.parsers.Char;
import javatools.parsers.NumberFormatter;

import javax.imageio.ImageIO;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).
  

  
 

 This class handles table data of the form
 <PRE>
 header1 | header2 | ...
 ------------------------
 entry1  | entry2  | ...
 ...
 </PRE>
 Each column has a ColumnDescriptor and a table consists of column-descriptors
 and values for the rows, e.g. as follows:
 <PRE>
 Table table=new Table("Table caption",
 // left-aligned, non-emphasized column
 new ColumnDescriptor("header1", Alignment.LEFT, false),
 // a column with numerical values and 5 digits after the dot
 new ColumnDescriptor("header2", "##.#####"));
 
 table.addRow("blah",7);
 table.addRow("blub",8.876);
 </PRE>
 Tables can be rendered as Latex-tables and as JPG graphics as follows:
 <PRE>
 FigureProducer.latexTable(table,"output.tex");
 FigureProducer.figure(table,"output.jpg");
 <PRE>
 */
public class FigureProducer {

  /** Alignment within a cell (left/right/center) */
  public enum Alignment {
    LEFT, RIGHT, CENTER
  };

  /** describes a column */
  public static class ColumnDescriptor {

    public String header;

    public Alignment alignment;

    public boolean emphasize;

    public NumberFormatter formi;

    public ColumnDescriptor(String header, Alignment alignment, boolean emphasize) {
      super();
      this.header = header;
      this.alignment = alignment;
      this.emphasize = emphasize;
    }

    public ColumnDescriptor(String header, Alignment alignment) {
      this(header, alignment, false);
    }

    public ColumnDescriptor(String header) {
      this(header, Alignment.LEFT);
    }

    public ColumnDescriptor(String header, String numberPattern, boolean alignment) {
      this(header, Alignment.LEFT, alignment);
      formi = new NumberFormatter(numberPattern);
    }

    public ColumnDescriptor(String header, String numberPattern) {
      this(header, numberPattern, false);
    }

    public String render(Object value) {
      String result = formi == null || !(value instanceof Number) ? value.toString() : formi.format(((Number) value).doubleValue());
      if (emphasize) result = "{\\textbf " + result + "}";
      return (result);
    }

  }

  /** Holds table data */
  public static class Table {

    protected List<ColumnDescriptor> columnDescriptors;

    protected List<List<Object>> rows = new ArrayList<List<Object>>();

    protected String title;

    protected boolean hasHead;

    public Table(String title, ColumnDescriptor... columnDescriptors) {
      this.columnDescriptors = Arrays.asList(columnDescriptors);
      this.title = title;
      this.hasHead = columnDescriptors.length > 0 && columnDescriptors[0].header != null;
    }

    public int numColumns() {
      return (columnDescriptors.size());
    }

    public int numRows() {
      return (rows.size());
    }

    public void addRow(Object... values) {
      rows.add(Arrays.asList(values));
    }

    public ColumnDescriptor descriptorFor(int i) {
      return (columnDescriptors.get(i));
    }

    public boolean hasHeader() {
      return (hasHead);
    }
  }

  /** Produces a latex table for a table */
  public static String latexTable(Table table) {
    StringBuilder result = new StringBuilder();
    result.append("\\begin{tabular}{|");
    for (ColumnDescriptor c : table.columnDescriptors) {
      result.append(Character.toLowerCase(c.alignment.toString().charAt(0))).append('|');
    }
    result.setLength(result.length() - 1);
    result.append("|}\n\\hline\n    ");
    if (table.hasHeader()) {
      for (ColumnDescriptor column : table.columnDescriptors) {
        result.append(column.render(column.header)).append(" & ");
      }
      result.setLength(result.length() - 2);
      result.append("\\\\\n\\hline\n");
    }
    for (List<Object> row : table.rows) {
      result.append("    ");
      for (int i = 0; i < table.numColumns(); i++) {
        result.append(table.descriptorFor(i).render(row.get(i)));
        result.append(" & ");
      }
      result.setLength(result.length() - 2);
      result.append(" \\\\\n");
    }
    result.append("\\hline\n\\end{tabular}\n");
    return (result.toString());
  }

  /** Produces a latex table and stores it in a file*/
  public static void latexTable(Table table, File output) throws IOException {
    Writer out = new BufferedWriter(new FileWriter(output));
    out.write(latexTable(table));
    out.close();
  }

  /** Writes a neat figure to a jpg file for a table */
  public static void figure(Table table, File output) throws IOException {
    int COLHEIGHT = 100;
    int LABELHEIGHT = 20;
    int COLWIDTH = 10;
    int ROWWIDTH = 50;
    int SPACELEFT = 30;
    int SPACERIGHT = table.numColumns() == 2 ? 0 : 60;
    int TITLEHEIGHT = 20;
    int TEXTHEIGHT = 10;
    int SPACEBELOW = TEXTHEIGHT + 2;
    double maxValue = 0;
    for (List<Object> row : table.rows) {
      for (int i = 1; i < table.numColumns(); i++) {
        double v = ((Number) row.get(i)).doubleValue();
        if (v > maxValue) maxValue = v;
      }
    }
    double scale = COLHEIGHT / maxValue;
    Color[] colors = { null, Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA };
    BufferedImage image = new BufferedImage(SPACELEFT + ROWWIDTH * table.numRows() + SPACERIGHT, TITLEHEIGHT + COLHEIGHT + LABELHEIGHT + SPACEBELOW,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = image.createGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
    graphics.setColor(Color.BLACK);
    graphics.drawString(table.title, SPACELEFT, 2 + TEXTHEIGHT);
    graphics.drawString(table.descriptorFor(0).header, SPACELEFT + ROWWIDTH / 2, image.getHeight() - 2);
    graphics.drawLine(SPACELEFT, TITLEHEIGHT, SPACELEFT, TITLEHEIGHT + COLHEIGHT);
    graphics.drawLine(SPACELEFT - 2, TITLEHEIGHT + COLHEIGHT, ROWWIDTH * table.numRows(), TITLEHEIGHT + COLHEIGHT);
    int exponent = (int) Math.floor(Math.log10(maxValue));
    int step = (int) Math.floor(Math.pow(10, exponent) * scale);
    graphics.drawString("0", SPACELEFT - 10, TITLEHEIGHT + COLHEIGHT + TEXTHEIGHT / 2);
    for (int pos = step; pos < COLHEIGHT; pos += step) {
      graphics.drawLine(SPACELEFT - 2, TITLEHEIGHT + COLHEIGHT - pos, SPACELEFT, TITLEHEIGHT + COLHEIGHT - pos);
      String label = "";
      switch (exponent) {
        case 0:
          label = "    " + (pos / step);
          break;
        case 1:
          label = "   " + (pos / step) + "0";
          break;
        case 2:
          label = " " + (pos / step) + "00";
          break;
        case -1:
          label = "   ." + (pos / step);
          break;
        case -2:
          label = "  .0" + (pos / step);
          break;
        default:
          label = (pos / step) + "E" + exponent;
      }
      graphics.drawString(label, 2, TITLEHEIGHT + COLHEIGHT - pos + TEXTHEIGHT / 2);
    }
    if (table.numColumns() > 2) {
      for (int i = 1; i < table.numColumns(); i++) {
        graphics.setColor(colors[i]);
        graphics.drawString(table.descriptorFor(i).header, SPACELEFT + ROWWIDTH * table.numRows(), TITLEHEIGHT + TEXTHEIGHT * i);
      }
    }
    for (int row = 0; row < table.numRows(); row++) {
      graphics.setColor(Color.BLACK);
      String header = table.rows.get(row).get(0).toString();
      while (graphics.getFontMetrics().stringWidth(header) > ROWWIDTH)
        header = Char.cutLast(header);
      if (header.length() != table.rows.get(row).get(0).toString().length()) header += '.';
      graphics.drawString(header, SPACELEFT + ROWWIDTH * row, TITLEHEIGHT + COLHEIGHT + LABELHEIGHT - 2);
      for (int column = 1; column < table.numColumns(); column++) {
        graphics.setColor(colors[column]);
        int h = (int) (((Number) table.rows.get(row).get(column)).doubleValue() * scale);
        graphics.fillRect(SPACELEFT + 2 + row * ROWWIDTH + (column - 1) * COLWIDTH, TITLEHEIGHT + COLHEIGHT - h, COLWIDTH, h);
      }
    }
    ImageIO.write(image, "jpg", output);
  }

  /** Test method */
  public static void main(String[] args) throws Exception {
    Table test = new Table("Avg # edges, 3 terminals", // Header
        new ColumnDescriptor("k", Alignment.LEFT, true), // 1st column
        new ColumnDescriptor("SIN", "#.###"), // 2nd column
        new ColumnDescriptor("SIN(BI)", "#.###"), // 3rd column
        new ColumnDescriptor("BANKS I", "#.###"), // 3rd column
        new ColumnDescriptor("BANKS II", "#.###") // 3rd column
    );
    // Add rows
    test.addRow("1",6981,85931,84171,81462);
    test.addRow("3",18027,124566,153078,132141);
    test.addRow("6",43474,138852,159130,175045);
    figure(test, new File("3term.jpg"));
    test = new Table("Avg # edges, 6 terminals", // Header
        new ColumnDescriptor("k", Alignment.LEFT, true), // 1st column
        new ColumnDescriptor("SIN", "#.###"), // 2nd column
        new ColumnDescriptor("SIN(BI)", "#.###"), // 3rd column
        new ColumnDescriptor("BANKS I", "#.###"), // 3rd column
        new ColumnDescriptor("BANKS II", "#.###") // 3rd column
    );
    // Add rows
    test.addRow("1",9559,375523,372634,365004);
    test.addRow("3",27085,397004,460521,409414);
    test.addRow("6",76259,447813,503054,491786);
    figure(test, new File("6term.jpg"));
  }
}
