package gov.nasa.jpl.mbee.doorsng.lib;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.HttpMethod;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;

import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.OAuthRedirectException;

public class DoorsOAuthClient extends DoorsOslcClient {

    protected OAuthAccessor accessor;
    private static Logger LOGGER = LogManager.getLogger(DoorsOAuthClient.class);
    private String oauth_real_name;

    /**
     * Initialize an OAuthClient with the required OAuth URLs
     * @param requestTokenURL
     * @param authorizationTokenURL
     * @param accessTokenURL
     * @param consumerKey
     * @param consumerSecret
     */
    public DoorsOAuthClient(String requestTokenURL,
                           String authorizationTokenURL,
                           String accessTokenURL,
                           String consumerKey,
                           String consumerSecret) {
        super();
        OAuthServiceProvider provider = new OAuthServiceProvider(requestTokenURL, authorizationTokenURL, accessTokenURL);
        OAuthConsumer consumer = new OAuthConsumer("",consumerKey,consumerSecret,provider);
        accessor = new OAuthAccessor(consumer);

    }

    public DoorsOAuthClient(String requestTokenURL,
                           String authorizationTokenURL,
                           String accessTokenURL,
                           String consumerKey,
                           String consumerSecret,
                           String oauthRealmName) {
        this(requestTokenURL, authorizationTokenURL, accessTokenURL, consumerKey, consumerSecret);
        oauth_real_name = oauthRealmName;
    }

    public DoorsOAuthClient(String requestTokenURL,
                           String authorizationTokenURL,
                           String accessTokenURL,
                           String consumerKey,
                           String consumerSecret,
                           String oauthRealmName,
                           String callback) {
        super();
        OAuthServiceProvider provider = new OAuthServiceProvider(requestTokenURL, authorizationTokenURL, accessTokenURL);
        OAuthConsumer consumer = new OAuthConsumer(callback,consumerKey,consumerSecret,provider);
        accessor = new OAuthAccessor(consumer);
        oauth_real_name = oauthRealmName;
    }

    /**
     * Creates a two-legged OAuth client. This will only work if the server
     * supports two-legged OAuth and a functional user has been assigned for
     * this OAuth consumer.
     *
     * @param consumerKey the OAuth consumer key
     * @param consumerSecret the OAuth consumer secret
     */
    public DoorsOAuthClient(String consumerKey, String consumerSecret) {
        OAuthServiceProvider provider = new OAuthServiceProvider(null, null, null);
        OAuthConsumer consumer = new OAuthConsumer("", consumerKey, consumerSecret, provider);
        accessor = new OAuthAccessor(consumer);
        accessor.accessToken = "";
    }

    @Override
    /**
     * Get an OAuth protected OSLC resource
     * @see OSLCClient#getResponse(final String url, final String mediaType)
     */
    public ClientResponse getResource(String url, String mediaType) throws IOException, OAuthException, URISyntaxException
    {
        OAuthMessage message = getResourceInternal(url, HttpMethod.GET, false);

        String realm = "Jazz";
        // Change if a different name was detected
        if ( oauth_real_name != null ) {
            realm = oauth_real_name;
        }
        String authHeader = message.getAuthorizationHeader(realm);

        ClientConfig config = getClientConfig();
        RestClient restClient = new RestClient(config);

        return restClient.resource(url).accept(mediaType).header("Authorization",authHeader).header("OSLC-Core-Version", "2.0").get();
    }


    @Override
    public ClientResponse updateResource(final String url, final Object artifact, String mediaType, String acceptType, String ifMatch) throws IOException, OAuthException, URISyntaxException
    {

        OAuthMessage message = getResourceInternal(url, HttpMethod.PUT, false);

        String realm = "Jazz";
        // Change if a different name was detected
        if ( oauth_real_name != null ) {
            realm = oauth_real_name;
        }
        String authHeader = message.getAuthorizationHeader(realm);

        ClientConfig config = getClientConfig();

        RestClient restClient = new RestClient(config);
        return restClient.resource(url).contentType(mediaType).accept(acceptType).header("Authorization",authHeader).header(HttpHeaders.IF_MATCH, ifMatch).header(OSLCConstants.OSLC_CORE_VERSION,"2.0").header(HttpHeaders.IF_MATCH, ifMatch).put(artifact);
    }

    /**
     * Create (POST) an artifact to a URL - usually an OSLC Creation Factory
     * @param url
     * @param artifact
     * @param mediaType
     * @param acceptType
     * @return
     * @throws URISyntaxException
     * @throws OAuthException
     * @throws IOException
     */
    @Override
    public ClientResponse createResource(final String url, final Object artifact, String mediaType, String acceptType) throws IOException, OAuthException, URISyntaxException  {
        OAuthMessage message = getResourceInternal(url, HttpMethod.POST, false);

        String realm = "Jazz";
        // Change if a different name was detected
        if ( oauth_real_name != null ) {
            realm = oauth_real_name;
        }
        String authHeader = message.getAuthorizationHeader(realm);

        ClientConfig config = getClientConfig();
        RestClient restClient = new RestClient(config);

        return restClient.resource(url).contentType(mediaType).accept(acceptType).header("Authorization",authHeader).header(OSLCConstants.OSLC_CORE_VERSION,"2.0").post(artifact);

        // return restClient.resource(url).accept(mediaType).header("Authorization",authHeader).header("OSLC-Core-Version", "2.0").get();
    }

    public String getProject() {
        return oauth_real_name;
    }

    /**
     * Performs necessary OAuth negotiation.
     *  - get request token
     *  - throw redirect exception to authorization URL (and print message)
     *  - exchange request token for access token
     *  - access protected URL and return OAuthMessage
     *
     * @param url
     * @param httpMethod
     * @param restart
     * @return
     * @throws IOException
     * @throws OAuthException
     * @throws URISyntaxException
     */
    protected OAuthMessage getResourceInternal(String url, String httpMethod, boolean restart) throws IOException, OAuthException, URISyntaxException {

        OAuthClient client = new OAuthClient(new HttpClient4(this.getClientPool()));
        if (!isTwoLegged()) {
            //No request token yet, get the request token and throw exception to redirect for authorization.
            if (accessor.requestToken == null) {
                client.getRequestToken(accessor);
                throw new OAuthRedirectException(accessor.consumer.serviceProvider.userAuthorizationURL, accessor);
            }

            //Exchange request token for access token.
            if (accessor.accessToken == null) {
                try {
                   client.getAccessToken(accessor, OAuthMessage.POST, null);
                } catch (OAuthException e) {
                    LOGGER.debug("OAuthException caught: " + e.getMessage());
                    if (restart)
                    {
                        LOGGER.error("Failed to get access key.");
                        e.printStackTrace();
                    } else {
                        //restart the dance
                        accessor.accessToken = null;
                        accessor.requestToken = null;
                        return getResourceInternal(url, httpMethod, true);
                    }
                }
            }
        }

        OAuthMessage message = accessor.newRequestMessage(httpMethod, url, null);


        return message;
    }

    /**
     * Are we using two-legged OAuth?
     *
     * @return true if this is a two-legged OAuth consumer
     */
    protected boolean isTwoLegged() {
        return "".equals(accessor.accessToken);
    }
}
