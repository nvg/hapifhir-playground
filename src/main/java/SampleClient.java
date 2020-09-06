import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.CapturingInterceptor;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.util.StopWatch;

public class SampleClient {

	public static void main(String[] theArgs) throws Exception {
		FhirContext ctx = FhirContext.forR4();
		IGenericClient client = ctx.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
		TimingInterceptor ti = new TimingInterceptor();
		client.registerInterceptor(ti);

		for (int i = 0; i < 3; i++) {
			runSearch(client, i == 2);

			System.out.println(
					"Average response time for iteration %d is %.0f ms.".formatted(i + 1, ti.getAverageResponseTime()));

			ti.reset();
		}

	}

	private static void runSearch(IGenericClient client, boolean disableCaching) throws Exception {
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(SampleClient.class.getClassLoader().getResourceAsStream("names.txt")))) {

			String name;
			while ((name = r.readLine()) != null) {
				runSearch(client, disableCaching, name);
			}
		}
	}

	private static void runSearch(IGenericClient client, boolean disableCaching, String name) {
		IQuery<?> query = client.search().forResource("Patient").where(Patient.FAMILY.matches().value(name));

		if (disableCaching) {
			query = query.withAdditionalHeader("CacheControl", "no-cache");
		}

		query.returnBundle(Bundle.class).execute();
	}

	/**
	 * Interceptor for timing average request-response cycles. 
	 *
	 */
	private static class TimingInterceptor extends CapturingInterceptor {

		private long timeCount;
		private int requestCount;

		/**
		 * Resets the counts.
		 */
		public void reset() {
			synchronized (this) {
				timeCount = 0;
				requestCount = 0;
			}
		}

		/**
		 * Gets average response time.
		 * 
		 * @return
		 * 		Returns the average response time or zero if no requests have been made.
		 */
		public double getAverageResponseTime() {
			if (requestCount == 0) {
				return 0;
			}

			return timeCount / requestCount;
		}


		@Override
		public void interceptResponse(IHttpResponse theResponse) {
			synchronized (this) {
				StopWatch sw = theResponse.getRequestStopWatch();
				timeCount += sw.getMillis();
				requestCount++;
			}

			super.interceptResponse(theResponse);

		}

	}

}
