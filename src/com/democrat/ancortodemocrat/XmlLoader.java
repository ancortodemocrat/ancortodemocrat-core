package com.democrat.ancortodemocrat;

import java.io.InputStream;

import javax.xml.bind.JAXB;

import com.democrat.ancortodemocrat.element.Annotation;
import com.democrat.ancortodemocrat.element.PositioningUnit;
import com.democrat.ancortodemocrat.element.Relation;
import com.democrat.ancortodemocrat.element.Unit;

public class XmlLoader {


	public static void loadAnnotationFromFile(String xmlFile) { 


		InputStream xmlStream = AncorToDemocrat.class.getClassLoader().getResourceAsStream(xmlFile);

		Annotation annotations = JAXB.unmarshal(xmlStream, Annotation.class); 

		for (Unit unit : annotations.getUnit()) { 

			System.out.println(unit.getId());
			/**
				String groupName = group.getName(); 

				Map<String, String> queryGroup = new LinkedHashMap<String, String>(); 

				queriesGroup.put(groupName, queryGroup); 

				for (Query query : group.getQueries()) { 

					String queryName = query.getName(); 

					String sqlQuery = query.getQuery(); 

					queryGroup.put(queryName, sqlQuery.trim()); 

				} 
			 **/

		}
		for(Relation relation : annotations.getRelation()){

		}

	}

}
