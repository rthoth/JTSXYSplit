package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;



public abstract class PolygonTest extends GeometryTest {

  protected static final CoordinateSequence SHELL_1 =
		coordinateSequence(0, 10, 4, 9, 2, 7, 2, 6, 4, 5, -4, 5, -2, 7, -3, 8, 0, 10);
  protected static final CoordinateSequence SHELL_2 =
		coordinateSequence(2, -3, 10, 6, 0, -10, -10, -4, -7, 4, -7, -3, -2, 2, -5, -3, 9, 7, 2, -3);
  protected static final CoordinateSequence HOLE_1 = coordinateSequence(-2, 6, 3, 9, -1, 9, -2, 6);
  protected static final CoordinateSequence HOLE_2 = coordinateSequence(-2, -3, 4, 2, -2, -5, -2, -3);
  protected static final CoordinateSequence HOLE_3 = coordinateSequence(-5, -4, -3, -6, -6, -5, -5, -4);

	protected static final CoordinateSequence SHELL_0 = coordinateSequence(1, -3, -2, -2, -2, 1, 1, 2, 3, -1, 4, 1, 1, 5, -4, 3, -4, -4, 1, -3);

  protected static final LinearRing SHELL_1_RING = FACTORY.createLinearRing(SHELL_1);
  protected static final LinearRing SHELL_2_RING = FACTORY.createLinearRing(SHELL_2);
  protected static final LinearRing HOLE_1_RING = FACTORY.createLinearRing(HOLE_1);
  protected static final LinearRing HOLE_2_RING = FACTORY.createLinearRing(HOLE_2);
  protected static final LinearRing HOLE_3_RING = FACTORY.createLinearRing(HOLE_3);

  protected static final Polygon POLYGON_1 =
		FACTORY.createPolygon(SHELL_1_RING, new LinearRing[] {HOLE_1_RING});
  protected static final Polygon POLYGON_2 =
		FACTORY.createPolygon(SHELL_2_RING, new LinearRing[] {HOLE_2_RING, HOLE_3_RING});
  protected static final MultiPolygon MULTI =
		FACTORY.createMultiPolygon(new Polygon[] {POLYGON_1, POLYGON_2});

	protected static final Polygon POLYGON_0 = FACTORY.createPolygon(SHELL_0);

  protected static final Reference REF_X_1 = Reference.x(2);
  protected static final Reference REF_X_2 = Reference.x(-2);
  protected static final Reference REF_Y_1 = Reference.y(6);
  protected static final Reference REF_Y_2 = Reference.y(-3);

	protected static final Reference REF_X_0 = Reference.x(0);
	protected static final Reference REF_Y_0 = Reference.y(0);
}
