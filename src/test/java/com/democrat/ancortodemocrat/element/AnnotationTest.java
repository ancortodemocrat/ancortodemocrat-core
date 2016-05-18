package com.democrat.ancortodemocrat.element;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class AnnotationTest {
	
	@Test
	public void test_getElementById(){
		Annotation annotation = new Annotation();
		Schema schema = new Schema();
		schema.setId("45678FGHJ09876");

		List<Schema> elements = new ArrayList<Schema>();
		
		elements.add( schema );
		
		annotation.setSchema( elements );
		
		assertEquals( "45678FGHJ09876", annotation.getElementById( "45678FGHJ09876" ).getId() );
		
		assertEquals( null, annotation.getElementById( "fizqnifuz989" ) );
		
	}

}
