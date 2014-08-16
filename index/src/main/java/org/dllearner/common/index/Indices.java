package org.dllearner.common.index;

import java.util.Arrays;
import java.util.Collection;
import org.dllearner.kb.sparql.SparqlEndpoint;
import com.hp.hpl.jena.rdf.model.Model;

public class Indices
{
	public final Index resourceIndex;
	public final Index classIndex;
	public final Index objectPropertyIndex;
	public final Index dataPropertyIndex;	
	public final MappingBasedIndex mappingIndex;;
	
	public Index propertyIndex;

	public Collection<Index> getIndices() {	return Arrays.asList(new Index[]{resourceIndex,classIndex,objectPropertyIndex,dataPropertyIndex});}
	
	public Indices(Index resourceIndex, Index classIndex, Index objectPropertyIndex, Index dataPropertyIndex)
	{this(resourceIndex, classIndex, objectPropertyIndex, dataPropertyIndex,null);}
	
	public Indices(Index resourceIndex, Index classIndex, Index objectPropertyIndex, Index dataPropertyIndex,MappingBasedIndex mappingIndex)
	{
		super();
		this.resourceIndex = resourceIndex;
		this.classIndex = classIndex;
		this.objectPropertyIndex = objectPropertyIndex;
		this.dataPropertyIndex = dataPropertyIndex;
		this.mappingIndex=mappingIndex;
	}

	public Index getResourceIndex() {return resourceIndex;}
	public Index getClassIndex() {return classIndex;}	
	public Index getObjectPropertyIndex() {return objectPropertyIndex;}
	public Index getDataPropertyIndex() {return dataPropertyIndex;}
	public MappingBasedIndex getMappingIndex() 	{return mappingIndex;}
//	public MappingBasedIndex getMappingIndex() {return mappingIndex;}

	@Deprecated public Index getPropertyIndex()
	{
		if(propertyIndex==null) propertyIndex = new HierarchicalIndex(objectPropertyIndex,dataPropertyIndex); 
		return propertyIndex;
	}	
	
//	public Indices(SparqlEndpoint endpoint)
//	 {
//		 this(new LemmatizedIndex(new SPARQLIndex(endpoint)), new LemmatizedIndex(new SPARQLClassesIndex(endpoint)), new LemmatizedIndex(new SPARQLObjectPropertiesIndex(endpoint)), new LemmatizedIndex(new SPARQLDatatypePropertiesIndex(endpoint)));
//	 }
//	
//	public Indices(SparqlEndpoint endpoint,MappingBasedIndex mappingIndex)
//	 {
//		 this(new LemmatizedIndex(new SPARQLIndex(endpoint)), new LemmatizedIndex(new SPARQLClassesIndex(endpoint)), new LemmatizedIndex(new SPARQLObjectPropertiesIndex(endpoint)), new LemmatizedIndex(new SPARQLDatatypePropertiesIndex(endpoint)),mappingIndex);
//	 }
//
//	 public Indices(Model model)
//	 {
//		 this(new LemmatizedIndex(new SPARQLIndex(model)), new LemmatizedIndex(new SPARQLClassesIndex(model)), new LemmatizedIndex(new SPARQLObjectPropertiesIndex(model)), new LemmatizedIndex(new SPARQLDatatypePropertiesIndex(model)));
//	 }
//	 
//	 public Indices(Model model,MappingBasedIndex mappingIndex)
//	 {
//		 this(new LemmatizedIndex(new SPARQLIndex(model)), new LemmatizedIndex(new SPARQLClassesIndex(model)), new LemmatizedIndex(new SPARQLObjectPropertiesIndex(model)), new LemmatizedIndex(new SPARQLDatatypePropertiesIndex(model)),mappingIndex);
//	 }
}