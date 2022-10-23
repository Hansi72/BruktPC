import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Main {
        public static ConcurrentHashMap<String, ArrayList<FinnAPI.ad>> gpuResult = new ConcurrentHashMap<String, ArrayList<FinnAPI.ad>>();
        public static CountDownLatch finnLatch;

    public static void main(String args[]) {
        //createValueTable("cpu", 15000, 25);
        createValueTable("gpu", 15000, 10);
    }

    //todo make this smarter (remove outliers and get an average instead)
    static int getPrice(ArrayList<FinnAPI.ad> ads){
        for(int i = 0; i < ads.size(); i++){
            Collections.sort(ads);
        }
        if(ads.size() > 10){
        return ads.get(ads.size()/5).price;
        }else{
            return 1000000;
        }
    }

    public static class partValue implements Comparable<partValue>{
        partValue(String name, int price, int score){
            this.name = name;
            this.value = (score*1000)/price;
            this.price = price;
            this.score = score;
        }
        int score;
        int price;
        String name;
        int value;

        @Override
        public int compareTo(partValue o) {
            return Integer.compare(value, o.value);
        }
    }

    static void createValueTable(String partType, int minimumPassMarkScore, int resultCount){
        try {
            ArrayList<passMarkAPI.part> parts;

            parts = passMarkAPI.getPosts(partType, minimumPassMarkScore);
            finnLatch = new CountDownLatch(parts.size());
            FinnAPI[] finnThreads = new FinnAPI[parts.size()];
            //for every passMarkAPI.getPosts result, do a finnAPI.getPosts search.
            for (int i = 0; i < parts.size(); i++) {
                finnThreads[i] = new FinnAPI(i, parts.get(i).name);
                finnThreads[i].start();
            }
            System.out.println("waiting for finn search threads..");
            finnLatch.await();
            partValue[] partValues = new partValue[parts.size()];
            for (int i = 0; i < parts.size(); i++) {
                ArrayList<FinnAPI.ad> ads = gpuResult.get(parts.get(i).name);
                partValues[i] = new partValue(parts.get(i).name, getPrice(ads), parts.get(i).score);
            }
            Arrays.sort(partValues);

            for (int i = partValues.length-resultCount; i < partValues.length; i++) {
                System.out.print("Value: " + partValues[i].value + " ".repeat(10 - Integer.toString(partValues[i].value).length()));
                System.out.print("PassMark Score: " + partValues[i].score + " ".repeat(10 - Integer.toString(partValues[i].score).length()));
                System.out.print("pris: " + partValues[i].price + " kr" + " ".repeat(10 - Integer.toString(partValues[i].price).length()));
                System.out.println(partValues[i].name);
            }
        }catch(InterruptedException e){
            System.out.println("valueTable: " + e);
        }
    }
}
