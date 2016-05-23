package com.democrat.ancortodemocrat.element;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.democrat.ancortodemocrat.positioning.PositioningSchema;
import com.democrat.ancortodemocrat.positioning.PositioningUnit;

public class SchemaTest {

	

	@Test
	public void test_getStart(){
		Annotation annotation = new Annotation();
		
		//unit pointed
		Unit unit = new Unit();
		unit.setId( "6789GHJKL" );
		PositioningUnit positioning = new PositioningUnit();
		positioning.setStart( new Start( 50L ));
		unit.setPositioning( positioning ); 
		

		Unit unit2 = new Unit();
		unit2.setId( "IOJHJG6898" );
		PositioningUnit positioning2 = new PositioningUnit();
		positioning.setStart( new Start( 20L ));
		unit.setPositioning( positioning ); 
		
		List<Unit> units = new ArrayList<Unit>();
		units.add( unit );
		units.add( unit2 );
		
		//create some embedded focus units
		EmbeddedUnit embedded = new EmbeddedUnit();
		EmbeddedUnit embedded2 = new EmbeddedUnit();
		
		embedded.setId( "6789GHJKL" );
		embedded2.setId( "IOJHJG6898" );
		
		PositioningSchema positioningSchema = new PositioningSchema();
		List<EmbeddedUnit> list = new ArrayList<EmbeddedUnit>();
		
		list.add(embedded );
		list.add( embedded2 );
		
		positioning.setEmbeddedUnit( list );
		
		
		assertEquals( 20L, unit.getStart( annotation ));
		
	}
	
}
