package com.democrat.ancortodemocrat.element;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "annotations")
public class Annotations {

    @XmlElement(required = true)
    protected Metadata metadata;
    protected List<Unit> unit;
    protected List<Relation> relation;

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link Annotations.Metadata }
     *     
     */
    public Metadata getMetadata() {
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
    public void setMetadata(Metadata value) {
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
    public List<Unit> getUnit() {
        if (unit == null) {
            unit = new ArrayList<Unit>();
        }
        return this.unit;
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
    public List<Relation> getRelation() {
        if (relation == null) {
            relation = new ArrayList<Annotations.Relation>();
        }
        return this.relation;
    }
}