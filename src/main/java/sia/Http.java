package sia;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

/**
 * Simple utility for executing HTTP requests from a
 * Solr In Action listing against your local Solr server.
 */
public class Http implements ExampleDriver.Example {

    static final Map<String, String> listings = new HashMap<String, String>();
    static {
        // chapter 4: Configuring Solr
        listings.put("4.4",
          "/collection1/select?q=iPod&fq=manu%3ABelkin&sort=price+asc&fl=name%2Cprice%2Cfeatures%2Cscore&df=text&wt=xml&start=0&rows=10");
        listings.put("4.7",
          "/collection1/browse?q=iPod&wt=xml&echoParams=all");
        listings.put("4.8",
          "/collection1/select?q=*%3A*&wt=xml&stats=true&stats.field=price");
        listings.put("4.9",
          "/collection1/browse?q=iPod&wt=xml&debugQuery=true");
        listings.put("4.12",
          "/collection1/select?q=iPod&fq=manu%3ABelkin&sort=price+asc&fl=name%2Cprice%2Cfeatures%2Cscore&df=text&wt=xml&start=0&rows=10");

        // chapter 9: Hit Highlighting
        listings.put("9.1",
            "/ufo/select?q=blue+fireball+in+the+rain&df=sighting_en&rows=10&wt=xml&hl=true");
        listings.put("9.2",
            "/ufo/select?q=blue+fireball+in+the+rain&df=sighting_en&wt=xml&hl=true&hl.snippets=2");
        listings.put("9.4",
            "/ufo/select?q=blue+fireball+in+the+rain&df=sighting_en&wt=xml&hl=true&hl.snippets=2&hl.fl=sighting_en&" +
            "facet=true&facet.limit=4&facet.field=shape_s&facet.field=location_s&facet.field=month_s");
        listings.put("9.5",
            "/ufo/select?q=blue+fireball+in+the+rain&df=sighting_en&wt=xml&hl=true&hl.snippets=2&hl.fl=sighting_en&" +
            "hl.q=blue+fireball+in+the+rain+light&fq=shape_s%3Alight");
        listings.put("9.6",
            "/ufo/select?q=fire+cluster+clouds+thunder&df=nearby_objects_en&wt=xml&hl=true&hl.snippets=2");
        listings.put("9.7",
            "/ufo/select?q=blue+fireball+in+the+rain&df=sighting_en&wt=xml&hl=true&hl.snippets=2&hl.useFastVectorHighlighter=true");

        listings.put("10.1", "/solrpedia/select?q=atmosphear");
        listings.put("10.2", "/solrpedia/select?q=title:anaconda");
        listings.put("10.3", "/solrpedia/select?q=Julius&df=suggest&fl=title");
        listings.put("10.7", "/solrpedia/select?q=northatlantic+curent&df=suggest&wt=xml");
        listings.put("10.8", "/solrpedia/select?q=northatlantic+curent&df=suggest&q.op=AND&spellcheck.dictionary=wordbreak&spellcheck.dictionary=default");
        listings.put("10.10", "/solrpedia/suggest?q=atm");
        listings.put("10.15", "/solrpedia_instant/select?q=query_ngram:willia&sort=popularity+desc&rows=1&fl=query&wt=json");
        listings.put("10.16", "/solrpedia_instant/select?q=%7B!boost+b%3D%24recency+v%3D%24qq%7D&sort=score+desc&rows=1&wt=json&qq=query_ngram:willia&recency=product(recip(ms(NOW/HOUR,last_executed_on),1.27E-10,0.08,0.05),popularity)");

        listings.put("15.1", "/salestax/select?q=*:*&userSalesTax=0.07&fl=id,basePrice,totalPrice:product(basePrice, sum(1, $userSalesTax))");
        listings.put("15.5", "/geospatial/select?q=*:*&fl=id,city,distance:geodist(location,37.77493, -122.41942)");
        listings.put("15.6", "/geospatial/select?q=*:*&fl=id,city,distance:geodist(location,37.77493, -122.41942)&sort=geodist(location,37.77493, -122.41942) asc, score desc");
        listings.put("15.8", "/distancefacet/select/?q=*:*&rows=0&fq={!geofilt sfield=location pt=37.777,-122.420 d=80}&facet=true&facet.field=city&facet.limit=10");
        listings.put("15.9", "/pivotfaceting/select?q=*:*&fq=rating:[4 TO 5]&facet=true&facet.limit=3&facet.pivot.mincount=1&facet.pivot=state,city,rating");

        listings.put("16.1", "/no-title-boost/select?defType=edismax&q=red lobster&qf=restaurant_name description&debug=true");
        listings.put("16.2", "/no-title-boost/select?defType=edismax&q=red lobster&qf=restaurant_name description&fl=id,restaurant_name,description,[explain]");
        listings.put("16.3", "/no-title-boost/select?defType=edismax&q=red lobster&qf=restaurant_name description&fl=id,restaurant_name,description");
    }

    @Override
    @SuppressWarnings("static-access")
    public Option[] getOptions() {
        return new Option[] {
            OptionBuilder.withArgName("#").hasArg().isRequired(true).withDescription("Listing #, such as 4.4")
                .create("listing"),
            OptionBuilder.withArgName("URL").hasArg().isRequired(false)
                .withDescription("Base URL for the Solr server; default: http://localhost:8983/solr")
                .create("solr"),
            OptionBuilder.withArgName("json|xml|csv").hasArg().isRequired(false)
                .withDescription("Override response writer type parameter \"wt\"")
                .create("wt")
        };
    }

    @Override
    public void runExample(ExampleDriver driver) throws Exception {
        String wt = driver.getCommandLine().getOptionValue("wt", "xml");
        String listingKey = driver.getCommandLine().getOptionValue("listing");
        String listing = listings.get(listingKey);
        if (listing == null) {
            System.err.println("ERROR: Listing " + listingKey
                + " not found! Please double-check your -listing parameter.");
            System.exit(1);
        }

        if (!"xml".equals(wt)) {
            if (listing.indexOf("wt=") != -1) {
                listing = listing.replaceAll("wt=xml", "wt="+wt);
            } else {
                listing += "&wt="+wt;
            }
        }

        String serverUrl = driver.getCommandLine().getOptionValue("solr", "http://localhost:8983/solr");

        // trick - get the HTTP Client from the SolrServer client
        HttpSolrServer solr = new HttpSolrServer(serverUrl);
        HttpClient httpClient = solr.getHttpClient();

        // Prepare a request object
        String getUrl = serverUrl + listing;
        HttpGet httpget = new HttpGet(getUrl+"&indent=true");

        // Execute the request
        System.out.println("Sending HTTP GET request (listing " + listingKey + "):");
        System.out.println("\n\t"+getUrl.replace("&", "&\n\t\t") + "\n");
        HttpResponse response = httpClient.execute(httpget);

        // Examine the response status
        System.out.println("Solr returned: "+response.getStatusLine()+"\n");

        // Get hold of the response entity
        HttpEntity entity = response.getEntity();

        // If the response does not enclose an entity, there is no need
        // to worry about connection release
        if (entity != null) {
            String line;
            InputStream instream = entity.getContent();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (RuntimeException ex) {
                // In case of an unexpected exception you may want to abort
                // the HTTP request in order to shut down the underlying
                // connection and release it back to the connection manager.
                httpget.abort();
                throw ex;

            } finally {
                // Closing the input stream will trigger connection release
                instream.close();
            }
        }
    }

    @Override
    public String getDescription() {
        return "Utility to execute an HTTP request from a listing in the book.";
    }
}
