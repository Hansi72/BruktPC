import java.util.ArrayList;

public class FinnAPI extends Thread{
    FinnAPI(int threadId, String gpuName){
        this.partName = gpuName;
        this.threadId = threadId;
    }
    int threadId;
    String partName;
    public ArrayList<ad> ads = new ArrayList<ad>();

    //searches finn for a specified gpu, returning a stack of all ads with name + price.
    public static ArrayList<ad> getPosts(String partName){ //todo gjør denne threadable
        browserPuppet puppet1 = new browserPuppet();
        String url = "https://www.finn.no/bap/forsale/search.html?q=" + partName.replaceAll(" ", "+") + "&sort=RELEVANCE";
        String body = puppet1.httpGetBody(url);
        ArrayList<ad> ads = new ArrayList<ad>();

        String name;
        int price;
        int startIndex;
        int endIndex;
        int bodyIndex = 0;
        //a new ad element starts at index of "_price"
        while((bodyIndex = body.indexOf("_price\">", bodyIndex+1)) != -1){
            startIndex = bodyIndex + "_price\">".length();
            endIndex = body.indexOf(" kr", startIndex);
            try{
            price = Integer.parseInt(body.substring(startIndex, endIndex).replaceAll(" ",""));
            }catch(Exception e){
                continue;
            }
            bodyIndex = body.indexOf("unit__link\">", bodyIndex); //index close to the ad name element
            startIndex = bodyIndex + "unit__link\">".length();
            endIndex = body.indexOf("<", startIndex);
            name = body.substring(startIndex, endIndex);
            ads.add(new ad(name, price));
        }
        //System.out.println("FinnAPI: Found " + ads.size() + " ads.");
        return ads;
    }

    @Override
    public void run(){
        Main.gpuResult.put(partName, getPosts(partName));
        Main.finnLatch.countDown();
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class ad implements Comparable<ad>{
        ad(String name, int price){
            this.name = name;
            this.price = price;
        }
        String name;
        int price;

        @Override
        public int compareTo(ad o) {
            return Integer.compare(price, o.price);
        }
    }
}
