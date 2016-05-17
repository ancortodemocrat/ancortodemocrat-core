package com.democrat.ancortodemocrat.element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

@XmlRootElement(name = "annotations")
public class Annotation {

	private static Logger logger = Logger.getLogger(Annotation.class);
    
    private MetadataAnnotation metadata;
    private List<Unit> unit;
    private List<Relation> relation;
    private List<Schema> schema;
    
    private String fileName;
    

    
    public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@XmlElement(name="schema")
    public List<Schema> getSchema() {
		return schema;
	}

	public void setSchema(List<Schema> schema) {
		this.schema = schema;
	}

	/**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link Annotations.Metadata }
     *     
     */
    @XmlElement(name="metadata")
    public MetadataAnnotation getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link Annotations.Metadata }
     *     
     */
    public void setMetadata(MetadataAnnotation value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the unit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUnit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Annotations.Unit }
     * 
     * 
     */
    @XmlElement(name="unit")
    public List<Unit> getUnit() {
        if (unit == null) {
            unit = new ArrayList<Unit>();
        }
        return this.unit;
    }
    
    
    
    public void setRelation(List<Relation> relation) {
		this.relation = relation;
	}

	/**
     * with the annotator name
     * @param id
     * @return Return the Unit corresponding, if not found return null
     */
    public Element getElementById(String id){
    	for(Unit unit : this.unit){
    		if(unit.getId().equals( id )){
    			return unit;
    		}
    	}
    	//maybe in schemas
    	for(Schema schema : this.schema){
    		if(schema.getId().equals( id )){
    			return schema;
    		}
    	}
    	//if is not a schema or an unit
    	//we have a relation to a schema
    	for(Relation relation : this.getRelation()){
    		if(relation.getId().equals( id )){
    			return relation;
    		}
    	}
    	return null;
    }
    
    /**
     * search an unit just with the number, ignore 
     * the annotator name
     * @param id
     * @return Return the Unit corresponding, if not found return null
     */
    public Unit getUnitByIdWithoutName(String id){
    	for(Unit unit : this.unit){
    		if(unit.getId().split("_")[1].equals(id)){
    			return unit;
    		}
    	}
    	return null;
    }

    /**
     * Gets the value of the relation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Annotations.Relation }
     * 
     * 
     */
    @XmlElement(name="relation")
    public List<Relation> getRelation() {
        if (relation == null) {
            relation = new ArrayList<Relation>();
        }
        return this.relation;
    }
    
    /**
     * Remove unit with TXT_IMPORTER as id
     */
    public void removeTxtImporter(){
    	for(int u = 0; u < this.unit.size(); u++){
    		if(this.unit.get(u).getId().startsWith( "TXT_IMPORTER" )){
    			this.unit.remove(u);
    			u--;
    		}
    	}
    }

	@Override
	public String toString() {
		String str = "Annotation :";
		str += System.lineSeparator();
		
		str += "    Schemas:";
		

		str += System.lineSeparator();
		for(Schema s : this.getSchema()){
			str += "    --> "+ s + System.lineSeparator();
		}
		
		str += "    Relations:";
		
		str += System.lineSeparator();
		for(Relation r : this.getRelation()){
			str += "    --> "+ r + System.lineSeparator();
		}
		return str;
	}
    
    
}