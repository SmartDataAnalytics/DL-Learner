/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This file is part of DL Learner Core Components.
 *
 * DL Learner Core Components is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL Learner Core Components is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DL Learner Core Components.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.experiments;

import com.google.common.base.Charsets;
import com.google.common.html.HtmlEscapers;
import com.google.common.io.Files;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import java.io.File;
import java.util.List;

/**
 * @author Lorenz Buehmann
 *
 */
public class Queries {
	
	public static void main(String[] args) throws Exception {
		File queries = new File(args[0]);
		File targetFile = new File(args[1]);
		
		List<String> lines = Files.readLines(queries, Charsets.UTF_8);
		
		String css = "<style>table.reference {\n" + 
				"	width:100%;\n" + 
				"	max-width:100%;\n" + 
				"	border-left:1px solid #dddddd;\n" + 
				"	border-right:1px solid #dddddd;	\n" + 
				"	border-bottom:1px solid #dddddd;		\n" + 
				"}\n" + 
				"table.reference>thead>tr>th, table.reference>tbody>tr>th, table.reference>tfoot>tr>th, table.reference>thead>tr>td, table.reference>tbody>tr>td, table.reference>tfoot>tr>td {\n" + 
				"    padding: 8px;\n" + 
				"    line-height: 1.42857143;\n" + 
				"    vertical-align: top;\n" + 
				"    border-top: 1px solid #ddd;\n" + 
				"}\n" + 
				"table.reference tr:nth-child(odd)	{background-color:#ffffff;}\n" + 
				"table.reference tr:nth-child(even)	{background-color:#f1f1f1;}</style>\n\n";
		
		String html = "<table class=\"reference\">\n";
		
		html += "<thead><tr><th>ID</th><th>Query</th></tr></thead>\n";
		
		html += "<tbody>\n";
		int row = 1;
		for (String line : lines) {
			html += "<tr>\n";
			
			html += "<td>" + row++ + "</td>\n";
			
			Query query = QueryFactory.create(line);
			html += "<td><pre>" + HtmlEscapers.htmlEscaper().escape(query.toString()) + "</pre></td>\n";
			html += "</tr>\n";
		}
		
		html += "</tbody>\n";
		html += "</table>";
		
		Files.write(css+html, targetFile, Charsets.UTF_8);
	}

}
