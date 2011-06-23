package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileFilter;

public class RdfFileFilter implements FileFilter
{
  private final String extension = new String("rdf");

  public boolean accept(File file)
  {
    return file.getName().toLowerCase().endsWith(extension);
  }
}

