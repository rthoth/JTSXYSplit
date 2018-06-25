package com.github.rthoth.xysplit;

import org.assertj.core.api.AbstractAssert;
import org.locationtech.jts.geom.Geometry;

public class Assertions extends org.assertj.core.api.Assertions {

	public static GeoChecker checkThat(Geometry geometry) {
		return new GeoChecker(geometry);
	}

	public static class GeoChecker extends AbstractAssert<GeoChecker, Geometry> {

		public GeoChecker(Geometry geometry) {
			super(geometry, GeoChecker.class);
		}
		
		public GeoChecker isEqualsTopo(Geometry geometry) {
			isNotNull();
			boolean equals = false;
			
			try {
				equals = actual.equalsTopo(geometry); 
			} catch (Throwable cause) {
				fail("Unexpected error on " + actual.toText() + "!" , cause);
			}
			
			if (!equals) {
				failWithMessage("%s should be equal to %s", actual, geometry);
			}
			
			return this;
		}
	}	
}
