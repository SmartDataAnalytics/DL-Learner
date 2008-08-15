import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CalculatePageRank {
	
	private final String wikilinks="../pagelinks_en.nt";
	private final String labels="../articles_label_en.nt";
	private final String categories="../yago_en.nt";
	
	private void calculateLinks()
	{
		try{
			Statement stmt;
			ResultSet rs;
			int number;

			Class.forName("com.mysql.jdbc.Driver");
		
			String url =
			            "jdbc:mysql://localhost:3306/navigator_db";
		
			Connection con = DriverManager.getConnection(
			                                 url,"navigator", "dbpedia");
			
			stmt = con.createStatement();
			BufferedReader in = new BufferedReader(new FileReader(wikilinks));
			
			String line;
			String[] split;
			String name;
			int i=0;
			while ((line=in.readLine())!=null)
			{
				split=line.split(" ");
				name=split[2].substring(1, split[2].length()-1);
				rs=stmt.executeQuery("SELECT number FROM rank WHERE name='"+name+"'");
				if (rs.next()){
					number=rs.getInt(1);
					number++;
					stmt.executeUpdate("UPDATE rank SET number="+number+" WHERE name='"+name+"'");
				}
				else{
					stmt.executeUpdate("INSERT INTO rank (name,number) VALUES ('"+name+"',1)");
				}
				if (i%100000==0) System.out.println(i);
				i++;
			}
			
			in.close();
			con.close();
		} catch (FileNotFoundException e)
		{
			System.out.println("File not found");
		} catch (IOException e)
		{
			System.out.println("IOException");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addLabels()
	{
		try{
			Statement stmt;
			ResultSet rs;
			
			Class.forName("com.mysql.jdbc.Driver");
		
			String url =
			            "jdbc:mysql://localhost:3306/navigator_db";
		
			Connection con = DriverManager.getConnection(
			                                 url,"navigator", "dbpedia");
			
			stmt = con.createStatement();
			BufferedReader in = new BufferedReader(new FileReader(labels));
			
			String line;
			String[] split;
			String name;
			String label;
			int i=0;
			while ((line=in.readLine())!=null)
			{
				split=line.split(">");
				name=split[0].substring(1);
				label=split[2].substring(split[2].indexOf("\"")+1, split[2].lastIndexOf("\""));
				rs=stmt.executeQuery("SELECT number FROM rank WHERE name='"+name+"'");
				if (rs.next()){
					stmt.executeUpdate("UPDATE rank SET label=\""+label+"\" WHERE name='"+name+"'");
				}
				else{
					stmt.executeUpdate("INSERT INTO rank (name,label) VALUES ('"+name+"',\""+label+"\")");
				}
				if (i%100000==0) System.out.println(i);
				i++;
			}
			
			in.close();
			con.close();
		} catch (FileNotFoundException e)
		{
			System.out.println("File not found");
		} catch (IOException e)
		{
			System.out.println("IOException");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void calculateCategories()
	{
		try{
			Statement stmt;
						
			Class.forName("com.mysql.jdbc.Driver");
		
			String url =
			            "jdbc:mysql://localhost:3306/navigator_db";
		
			Connection con = DriverManager.getConnection(
			                                 url,"navigator", "dbpedia");
			
			stmt = con.createStatement();
			BufferedReader in = new BufferedReader(new FileReader(categories));
			
			String line;
			String[] split;
			String name;
			String label;
			String pred;
			int i=0;
			while ((line=in.readLine())!=null)
			{
				split=line.split(">");
				name=split[0].substring(1);
				pred=split[1].substring(2);
				if (pred.equals("http://www.w3.org/2000/01/rdf-schema#label"))
					label=split[2].substring(split[2].indexOf("\"")+1, split[2].lastIndexOf("\""));
				else
					label=split[2].substring(2);
				if (pred.equals("http://www.w3.org/2000/01/rdf-schema#label")){
					try{
						stmt.executeUpdate("INSERT INTO categories (category,label) VALUES (\""+name+"\",\""+label+"\")");
					}catch(Exception e)
					{}
				}
				else{
					stmt.executeUpdate("UPDATE rank SET category=\""+label+"\" WHERE name=\""+name+"\"");
				}
				if (i%100000==0) System.out.println(i);
				i++;
			}
			
			in.close();
			con.close();
		} catch (FileNotFoundException e)
		{
			System.out.println("File not found");
		} catch (IOException e)
		{
			System.out.println("IOException");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		CalculatePageRank cal=new CalculatePageRank();
		//cal.calculateLinks();
		//cal.addLabels();
		cal.calculateCategories();
	}
}