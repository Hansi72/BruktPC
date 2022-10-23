import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class browserPuppet {


    String httpGetBody(String url){
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .join();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            //System.out.println(response.statusCode());
            return response.body();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

}
