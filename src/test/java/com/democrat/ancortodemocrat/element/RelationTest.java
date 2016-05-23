package com.democrat.ancortodemocrat.element;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.democrat.ancortodemocrat.positioning.PositioningRelation;
import com.democrat.ancortodemocrat.positioning.PositioningUnit;

public class RelationTest {

	@Test
	public void test_getElement(){

		Annotation annotation = new Annotation();

		//create a relation
		Relation relation = new Relation();

		PositioningRelation position = new PositioningRelation();

		//unit pointed
		Unit unit = new Unit();
		unit.setId( "6789GHJKL" );
		PositioningUnit positioningUnit = new PositioningUnit();
		positioningUnit.setStart( new Start( 50L ));
		unit.setPositioning( positioningUnit );
		unit.setFeature("NEW", "YES");
		

		Unit unit2 = new Unit();
		unit2.setId( "6fdqsfqsfsJKL" );
		PositioningUnit positioningUnit2 = new PositioningUnit();
		positioningUnit2.setStart( new Start( 79L ));
		unit2.setPositioning( positioningUnit2 );
		unit2.setFeature("NEW", "NO");

		
		List<Term> termList = new ArrayList<Term>();
		termList.add( new Term( unit.getId() ) );
		termList.add( new Term( unit2.getId() ) );
		
		position.setTerm( termList );
		relation.setPositioning( position );
		

		annotation.addUnit( unit );
		annotation.addUnit( unit2 );
		
		assertEquals( "6fdqsfqsfsJKL", relation.getElement( annotation ).getId() );
		
	}

}
