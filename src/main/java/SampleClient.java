import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;

/**
 * Sample client for carrying out search tasks.
 */
public class SampleClient {

	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SampleClient.class);

	public static void main(String[] theArgs) {
		SampleClient client = new SampleClient();
		try {
			client.search();
		} catch (IOException e) {
			log.error("Unable to run search", e);
		}
	}

	public static FhirContext context = FhirContext.forR4();
	private IGenericClient client;

	/**
	 * Default constructor initializing base R4 HAPI client
	 */
	public SampleClient() {
		this(context.newRestfulGenericClient("http://hapi.fhir.org/baseR4"));
	}

	/**
	 * Constructor that sets the specified HAPI client.
	 * 
	 * @param client HAPI client to use for all API calls
	 */
	public SampleClient(IGenericClient client) {
		this.client = client;
	}

	/**
	 * Runs name search three times, disabling HAPI server-side caching for the
	 * third call
	 * 
	 * @throws IOException IOException is thrown in case names can not be retrieved
	 *                     from the classpath.
	 */
	public void search() throws IOException {
		TimingInterceptor ti = new TimingInterceptor();
		client.registerInterceptor(ti);

		try {
			for (int i = 0; i < 3; i++) {
				runSearch(client, i == 2);

				System.out.println("Average response time for iteration %d is %.0f ms.".formatted(i + 1,
						ti.getAverageResponseTime()));

				ti.clear();
			}
		} finally {
			client.unregisterInterceptor(ti);
		}
	}

	protected void runSearch(IGenericClient client, boolean disableCaching) throws IOException {
		try (BufferedReader r = getReader()) {
			String name;
			while ((name = r.readLine()) != null) {
				runSearch(client, disableCaching, name);
			}
		}
	}

	private void runSearch(IGenericClient client, boolean disableCaching, String name) {
		IQuery<?> query = client
				.search()
				.forResource("Patient")
				.where(Patient.FAMILY.matches().value(name));
		
		if (disableCaching) {
			query = query.withAdditionalHeader("CacheControl", "no-cache");
		}
		query.returnBundle(Bundle.class).execute();
	}

	private BufferedReader getReader() {
		return new BufferedReader(
				new InputStreamReader(SampleClient.class.getClassLoader().getResourceAsStream("names.txt")));
	}

}
