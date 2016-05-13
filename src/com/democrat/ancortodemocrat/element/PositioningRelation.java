package com.democrat.ancortodemocrat.element;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="positioning")
public class PositioningRelation {
	
	private List<Term> term;
	/**
	 * Gets the value of the term property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list,
	 * not a snapshot. Therefore any modification you make to the
	 * returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the term property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * <pre>
	 *    getTerm().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Annotations.Relation.Positioning.Term }
	 * 
	 * 
	 */
	@XmlElement(name="term")
	public List<Term> getTerm() {
		if (term == null) {
			term = new ArrayList<Term>();
		}
		return this.term;
	}

	public void setTerm(List<Term> term) {
		this.term = term;
	}
	
	public String toString(){
		String str = "[PositioningRelation]";
		for(Term term : this.getTerm()){
			str += System.lineSeparator();
			str += "     " + term.getId();
		}
		
		return str;
	}

	
}