package sample;

import com.github.rthoth.xysplit.Reference;
import com.github.rthoth.xysplit.SplitResult;
import com.github.rthoth.xysplit.XYSplitter;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class SimpleSample {

	private final WKTReader reader = new WKTReader();

	private final Geometry polygon1 = reader.read("MultiPolygon (((-0.99355848534103908 0.41241346095960507, -0.74954347520719211 -0.38063532197539773, 0.07882326972086751 0.31930247025063718, -0.99355848534103908 0.41241346095960507)), ((-0.63716814159292046 0.10097325065719509, 0.60538197579916919 0.26793088916982732, 0.30678466076696131 -0.24899564545582242, -0.63716814159292046 0.10097325065719509)))");
	private final Geometry polygon2 = reader.read("Polygon ((-0.01107699717054977 0.65000702345835093, -0.76559709429494505 0.15234483173800495, 0.80444685248730763 -0.15588465474685442, -0.01107699717054977 0.65000702345835093))");

	public SimpleSample() throws ParseException {
	}

	public void xSplit() {
		Envelope envelope = new Envelope(polygon1.getEnvelopeInternal());
		envelope.expandToInclude(polygon2.getEnvelopeInternal());

		// middle point.
		Reference x = Reference.x((envelope.getMaxX() + envelope.getMinX()) / 2);	

		XYSplitter splitter = new XYSplitter(x);

		// polygon1 split result.
		SplitResult result1 = splitter.apply(polygon1);

		// polygon2 split result.
		SplitResult result2 = splitter.apply(polygon2);


	}
}
