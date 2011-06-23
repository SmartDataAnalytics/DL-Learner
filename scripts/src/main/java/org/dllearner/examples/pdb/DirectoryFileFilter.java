package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFileFilter implements FileFilter
{
  public boolean accept(File file)
  {
	  if (file.isDirectory() && file.getName().length() == 4 && !file.getName().startsWith(".")) {
		  return true;
	  }
	  else {
		  return false;
	  }
  }
}
