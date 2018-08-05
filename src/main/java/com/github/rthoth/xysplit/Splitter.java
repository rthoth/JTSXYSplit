package com.github.rthoth.xysplit;

import org.locationtech.jts.geom.Geometry;

import java.util.function.Function;

public abstract class Splitter<G extends Geometry> implements Function<G, SplitResult> {
}
