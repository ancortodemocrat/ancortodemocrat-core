package com.democrat.ancortodemocrat.element;

import static org.junit.Assert.*;

import org.junit.Test;

import com.democrat.ancortodemocrat.positioning.PositioningUnit;

public class UnitTest {
	
	@Test
	public void test_getStart(){
		Unit unit = new Unit();
		PositioningUnit positioning = new PositioningUnit();
		positioning.setStart( new Start( 50 ));
		unit.setPositioning( positioning ); 
		
		assertEquals( 50L, unit.getStart( null ));
		
	}
	

}
