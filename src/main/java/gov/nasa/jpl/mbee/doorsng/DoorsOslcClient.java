package gov.nasa.jpl.mbee.doorsng;

import java.io.IOException;
import java.net.URISyntaxException;
import net.oauth.OAuthException;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

import org.eclipse.lyo.client.oslc.OSLCConstants;

public class DoorsOslcClient extends org.eclipse.lyo.client.oslc.OslcClient
{

	private static Logger logger = Logger.getLogger(DoorsFormAuthClient.class);

	public DoorsOslcClient()
	{
		super();
	}

    /**
	 * Create (POST) an artifact to a URL - usually an OSLC Creation Factory
	 * @param url
	 * @param artifact
	 * @param mediaType
	 * @param acceptType
     * @param customHeaders
	 * @return
	 * @throws URISyntaxException
	 * @throws OAuthException
	 * @throws IOException
	 */
	public ClientResponse createResource(String url, final Object artifact, String mediaType, String acceptType, Map<String, String> customHeaders) throws IOException, OAuthException, URISyntaxException {

		ClientResponse response = null;
		RestClient restClient = new RestClient(this.getClientConfig());
		boolean redirect = false;

		do {
            Resource client = restClient.resource(url).contentType(mediaType).accept(acceptType).header(OSLCConstants.OSLC_CORE_VERSION,"2.0");

            for (String key : customHeaders.keySet()) {
                String value = customHeaders.get(key);
                client.header(key, value);
            }

			response = client.post(artifact);

			if (response.getStatusType().getFamily() == Status.Family.REDIRECTION) {
				url = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				response.consumeContent();
				redirect = true;
			} else {
				redirect = false;
			}
		} while (redirect);

		return response;
	}
}
