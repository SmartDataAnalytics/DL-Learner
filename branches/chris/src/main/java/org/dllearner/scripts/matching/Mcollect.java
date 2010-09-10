package org.dllearner.scripts.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dllearner.utilities.datastructures.RDFNodeTuple;
import org.dllearner.utilities.datastructures.StringTuple;

import com.wcohen.ss.Jaro;
import com.wcohen.ss.api.StringDistance;

public class Mcollect {
	String name;
	List<Pcollect> props = new ArrayList<Pcollect>();
	
	
	public void  add(SameCollect s){
		StringDistance distance = new Jaro();
		
		for (RDFNodeTuple db : s.dbdata) {
			for (StringTuple ld : s.lddata) {
//				System.out.println(ld.b);
//				System.out.println(db.b.toString());
//				System.out.println(istance.score(ld.b,db.b.toString()));
				if ( distance.score(ld.b,db.b.toString())>=0.90){
					boolean found = false;
					for (Pcollect p : props){
						
						if(p.ldp.equals(ld.a) && p.dbp.equals( db.a.toString())){
							p.count +=1;
							found = true;
						}
					}
					if(found==false){
						props.add(new Pcollect(ld.a, db.a.toString()));
					}
					
					
				};
				
			}
		}
	}
	
	@Override
	public String toString(){
		//SortedSet<Pcollect> s = new TreeSet<Pcollect>();
//		for(Pcollect one : s){
//			s.add(one);
//			
//		}
		String ret = "";
		Collections.sort(props );
		for(int a=0; a<props.size();a++){
			
			ret+= props.get(a).toString()+"\n";
			
		}
		return ret;
	}
}
