package org.dllearner.core.owl;

import java.util.Map;
import java.util.Set;

public class ObjectOneOf extends Description{
	
	private static final long serialVersionUID = 5494347630962268139L;
	
	private Set<Individual> individuals;
	
	
	public ObjectOneOf(Set<Individual> individuals){
		this.individuals = individuals;
	}
	
	public Set<Individual> getIndividuals(){
		return individuals;
	}

	@Override
	public void accept(DescriptionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int getArity() {
		return 0;
	}

	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		StringBuffer sb = new StringBuffer();
		int count = 1;
		sb.append("{");
		for(Individual ind : individuals){
			sb.append(ind.toString(baseURI, prefixes));
			if(count < individuals.size()){
				sb.append(",");
				count++;
			}
		}
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public int getLength() {
		return 1;
	}

	@Override
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		StringBuffer sb = new StringBuffer();
		int count = 1;
		sb.append("{");
		for(Individual ind : individuals){
			sb.append(ind.toKBSyntaxString(baseURI, prefixes));
			if(count < individuals.size()){
				sb.append(",");
				count++;
			}
		}
		sb.append("}");
		
		return sb.toString();
	}

	@Override
	public String toString(String baseURI, Map<String, String> prefixes) {
		StringBuffer sb = new StringBuffer();
		int count = 1;
		sb.append("{");
		for(Individual ind : individuals){
			sb.append(ind.toString(baseURI, prefixes));
			if(count < individuals.size()){
				sb.append(", ");
				count++;
			}
		}
		sb.append("}");
		
		return sb.toString();
	}

}
