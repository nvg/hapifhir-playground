import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.CapturingInterceptor;
import ca.uhn.fhir.util.StopWatch;

/**
 * Interceptor for timing average request-response cycles.
 */
public class TimingInterceptor extends CapturingInterceptor {

	private long timeCount;
	private int requestCount;

	/**
	 * Clears this interceptor by resetting the counts.
	 */
	@Override
	public void clear() {
		synchronized (this) {
			timeCount = 0;
			requestCount = 0;
		}
		super.clear();
	}

	/**
	 * Gets average response time.
	 * 
	 * @return Returns the average response time or zero if no requests have been
	 *         made.
	 */
	public double getAverageResponseTime() {
		if (requestCount == 0) {
			return 0;
		}

		return timeCount / requestCount;
	}

	@Override
	public void interceptResponse(IHttpResponse theResponse) {
		StopWatch sw = theResponse.getRequestStopWatch();
		synchronized (this) {
			timeCount += sw.getMillis();
			requestCount++;
		}

		super.interceptResponse(theResponse);
	}

}
