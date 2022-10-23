import java.util.ArrayList;

public class passMarkAPI {

    final static String[] brands = {"GeForce", "Radeon", "AMD", "Intel"}; //only get parts with these prefixes.
    static int startIndex = 0;
    static int endIndex = 0;
    static int bodyIndex = 0;
    static String body;
    static browserPuppet puppet1 = new browserPuppet();

    //search the html body to fit names with passmark scores in a hashmap. partType is either "cpu" or "gpu"
    public static ArrayList<part> getPosts(String partType, int minPassMarkScore) {
        switch (partType) {
            case "gpu":
                body = puppet1.httpGetBody("https://www.videocardbenchmark.net/gpu_list.php");
                bodyIndex = 0;
                break;
            case "cpu":
                body = puppet1.httpGetBody("https://www.cpubenchmark.net/cpu_list.php");
                bodyIndex = body.indexOf("id=\"" + partType, bodyIndex + 1);
        }

        ArrayList<part> parts = new ArrayList<part>();
        String name;
        int passMarkScore;
        //move towards where the data is stored in the body.
        while ((bodyIndex = body.indexOf("id=\"" + partType, bodyIndex + 1)) != -1) {
            name = scrapeName(partType);
            passMarkScore = scrapeScore(partType);

            if (validPart(name, passMarkScore, minPassMarkScore)) {
                parts.add(new part(name, passMarkScore));
            }
        }
        System.out.println("passMarkAPI: Found " + parts.size() + " " + partType + "s.");
        return parts;
    }

    static String scrapeName(String partType) {
        if (partType.equals("cpu")) {
            startIndex = body.indexOf("\">", bodyIndex) + "\">".length();
            startIndex = body.indexOf("\">", startIndex) + "\">".length();
            endIndex = body.indexOf("<", startIndex);
            return body.substring(startIndex, endIndex);
        }
        if (partType.equals("gpu")) {
            startIndex = body.indexOf("\">", bodyIndex) + "\">".length();
            startIndex = body.indexOf("\">", startIndex) + "\">".length();
            endIndex = body.indexOf("<", startIndex);
            return body.substring(startIndex, endIndex);
        }
        return null;
    }

    static int scrapeScore(String partType) {
        if (partType.equals("cpu")) {
            startIndex = body.indexOf("<td>", startIndex) + "<td>".length();
            endIndex = body.indexOf("<", startIndex);
            return Integer.parseInt(body.substring(startIndex, endIndex).replaceAll(",", ""));
        }
        if (partType.equals("gpu")) {
            startIndex = body.indexOf("</TD>", startIndex) + "</TD>".length();
            startIndex = body.indexOf("<TD>", startIndex) + "<TD>".length();
            endIndex = body.indexOf("<", startIndex);
            return Integer.parseInt(body.substring(startIndex, endIndex));
        }
        return 0;
    }

    static boolean validPart(String name, int score, int minPassMarkScore) {
        if (score < minPassMarkScore) {
            return false;
        }
        for (int i = 0; i < brands.length; i++) {
            if (brands[i].length() < name.length()) {
                if (brands[i].equals(name.substring(0, brands[i].length()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class part {
        part(String name, int score) {
            this.name = name;
            this.score = score;
        }

        String name;
        int score;
    }
}
