package com.democrat.ancortodemocrat.element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "feature"
})
public static class FeatureSet {

    protected List<Feature> feature;

    /**
     * Gets the value of the feature property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the feature property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFeature().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Annotations.Relation.Characterisation.FeatureSet.Feature }
     * 
     * 
     */
    public List<Feature> getFeature() {
        if (feature == null) {
            feature = new ArrayList<Annotations.Relation.Characterisation.FeatureSet.Feature>();
        }
        return this.feature;
    }

}