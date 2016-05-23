package com.democrat.ancortodemocrat.element;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.democrat.ancortodemocrat.feature.Feature;
import com.democrat.ancortodemocrat.feature.FeatureSet;

public class ElementTest {

	@Test
	public void test_getFeature(){
		Element element = new Element();
		Characterisation characterisation = new Characterisation();
		FeatureSet featureSet = new FeatureSet();
		
		
		List<Feature> list = new ArrayList<Feature>();
		
		list.add( new Feature("test", "value"));
		featureSet.setFeature( list );
		
		characterisation.setFeatureSet( featureSet );
		element.setCharacterisation( characterisation );
		assertEquals( "value", element.getFeature( "test" ) );
		
	}
	
	@Test
	public void test_setFeature(){
		Element element = new Element();
		Characterisation characterisation = new Characterisation();
		FeatureSet featureSet = new FeatureSet();
		
		
		List<Feature> list = new ArrayList<Feature>();
		
		list.add( new Feature("test", "value"));
		featureSet.setFeature( list );
		
		characterisation.setFeatureSet( featureSet );
		element.setCharacterisation( characterisation );
		
		assertEquals( "value", element.getCharacterisation().getFeatureSet().getFeature().get( 0 ).getValue() );
		assertEquals( "test", element.getCharacterisation().getFeatureSet().getFeature().get( 0 ).getName() );
	}
	
}
