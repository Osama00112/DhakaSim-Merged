import java.io.*;
import java.util.*;

import static java.lang.Math.sqrt;

// to find train links
class TrainNodePair{
    public int src ;
    public int dest ;

    public TrainNodePair(int src, int dest) {
        this.src = src;
        this.dest = dest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainNodePair)) return false;

        TrainNodePair that = (TrainNodePair) o;

        if (src != that.src) return false;
        return dest == that.dest;
    }

    @Override
    public int hashCode() {
        int result = src;
        result = 31 * result + dest;
        return result;
    }

    @Override
    public String toString() {
        return src + " "+ dest;
    }
}
public class Sim {

    private ArrayList<Integer> outNode = new ArrayList<>();
    private static int demandType; //0 low; 1 mid; 2 high
    private static int LOW_RATE = 100;
    private static int MEDIUM_RATE = 500;
    private static int HIGH_RATE = 1000;

    private int demand = 100;
    private int[][] adjMatrix, pathMatrix, next;
    private List<Integer>[] adjacentLinks;  // osama        // CSE18
    private double[][] DistMatrix;  // Rumi
    private Random rand = new Random();

    private String outPath = "", outDemand = "";
    ArrayList<Integer> trainNodes = new ArrayList<Integer>();   // contains train nodes
    ArrayList<TrainNodePair> trainNodePairs = new ArrayList<TrainNodePair>();  // contains train pairs

    private double getDistance(double x1, double y1,double z1, double x2, double y2,double z2) {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)+(z1 - z2)*(z1 - z2));
    }
    // public from private
    public Sim() throws Exception {
        readFile();
        //floydWarshall();
        floydWarshall_usingdistance();          // CSE18
        setDemand();

        int i = 0,abcd=0;
        for (int k = 0; k < outNode.size(); k++) {
            for (int j = 0; j < outNode.size(); j++) {

                if (k == j) continue;

                Integer src = outNode.get(k);
                Integer dest = outNode.get(j);

                printResult(src, dest);
                //printResult(dest, src);

                if(trainNodes.contains(src) || trainNodes.contains(dest)){
                    continue;
                }
                else{
                    i++;
                }

            }
        }
        i += trainNodePairs.size();
        outPath = i + "\n" + outPath;
        outDemand = i + "\n" + outDemand;
        //System.out.println(">>>>>>>>>>>>>  \n"+outDemand);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("inputTest/path.txt"))) {
            bw.write(outPath);
            System.out.println(outPath);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("inputTest/demand.txt"))) {
            bw.write(outDemand);
            //System.out.println("\n\ndemand \n");
            //System.out.println(outDemand);
            System.out.println(outDemand);
        }

        modifyDemand();
    }
    // Rumi                                 // CSE18
    public int[][] getNext()
    {
        return next;
    }
    public int[][] getPathMatrix() {
        return pathMatrix;
    }

    private int setDemand() {
        int i = outNode.size() - 1;
        int acceptableNode;
        if (outNode.size() * i > 100) {
            if (demandType == 0) {
                // low demand
                acceptableNode = (int) Math.round(i / 3.0);
                demand = Math.round(LOW_RATE / acceptableNode);
            } else if (demandType == 1) {
                // medium demand
                acceptableNode = (int) Math.round(2.0 * i / 3.0);
                demand = Math.round(MEDIUM_RATE / acceptableNode);
            } else if (demandType == 2) {
                // high demand
                acceptableNode = i;
                demand = Math.round(HIGH_RATE / acceptableNode);
            } else {
                acceptableNode = i;
                demand = Math.round(HIGH_RATE / acceptableNode);
            }
        } else {
            acceptableNode = i;
            if (demandType == 0) {
                // low demand
                demand = Math.round(LOW_RATE / acceptableNode);
            } else if (demandType == 1) {
                // medium demand
                demand = Math.round(MEDIUM_RATE / acceptableNode);
            } else if (demandType == 2) {
                // high demand
                demand = Math.round(HIGH_RATE / acceptableNode);
            } else {
                demand = Math.round(HIGH_RATE / acceptableNode);
            }
        }
        return acceptableNode;
    }

    public void mySim() throws Exception {

        readFile();

        //floydWarshall();
        floydWarshall_usingdistance();      // CSE18

        Random rand = new Random();

        int i = 0;
        while (outNode.size() > 1) {

            int s = rand.nextInt(outNode.size());
            int d = rand.nextInt(outNode.size());

            if (s == d) {
                continue;
            }

            Integer src = outNode.get(s);
            Integer dest = outNode.get(d);
            
            printResult(src, dest);
            printResult(dest, src);

            if (rand.nextInt(2) == 0) {
                outNode.remove(src);
            } else {
                outNode.remove(dest);
            }

            i++;
        }
        outPath = i * 2 + "\n" + outPath;
        outDemand = i * 2 + "\n" + outDemand;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("inputTest/path.txt"))) {
            bw.write(outPath);
            System.out.println(outPath);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("inputTest/demand.txt"))) {
            bw.write(outDemand);
            System.out.println(outDemand);
        }
    }

    private void readFile() throws Exception {
        int numOfLinks = Integer.parseInt(new BufferedReader(new FileReader("inputTest/link.txt")).readLine());

        try (BufferedReader br = new BufferedReader(new FileReader("inputTest/node.txt"))) {
            int numOfNodes = Integer.parseInt(br.readLine());

            adjacentLinks = new List[numOfNodes]; // osama      // CSE18
            for(int i=0;i<adjacentLinks.length;i++)
            {
                adjacentLinks[i]=new ArrayList<>();
            }

            int[][] tempMatrix = new int[numOfLinks][numOfNodes];

            for (int i = 0; i < numOfNodes; i++) {
                String[] split = br.readLine().split(" ");
                System.out.println("######    "+Arrays.toString(split));
                String[] links = new String[split.length - 5];
                System.arraycopy(split, 5, links, 0, links.length);
                if (split[4].equals("1")){
                    trainNodes.add(Integer.parseInt(split[0]));
                }

                for (String link : links) {
                    tempMatrix[Integer.parseInt(link)][i] = 1;
                }

                System.out.println("********   "+Arrays.toString(links));
                if (links.length == 1) {
                    outNode.add(i);
                }
            }
            //System.out.println("trainNodes : "+trainNodes);


            for (int i = 0; i < numOfLinks; i++) {
                System.out.println(Arrays.toString(tempMatrix[i]));

                for (int j = 0; j < trainNodes.size() ; j++) {
                    if(tempMatrix[i][trainNodes.get(j)] == 1){
                        for (int k = j+1; k < trainNodes.size(); k++) {
                            if(tempMatrix[i][trainNodes.get(k)] == 1){
                                trainNodePairs.add(new TrainNodePair(trainNodes.get(j),trainNodes.get(k)));
                            }
                        }
                    }
                }

            }

            System.out.println("trainNodePairs  "+ trainNodePairs);
            adjMatrix = new int[numOfNodes][numOfNodes];        // CSE18
            pathMatrix = new int[numOfNodes][numOfNodes];
            DistMatrix = new double[numOfNodes][numOfNodes];  // Rumi

            for (int i = 0; i < numOfNodes; i++) {
                int INF = 99999;
                Arrays.fill(adjMatrix[i], INF);
                adjMatrix[i][i] = 0;
                Arrays.fill(DistMatrix[i], INF);  // Rumi       // CSE18
                DistMatrix[i][i]=0;
            }

            for (int i = 0; i < numOfLinks; i++) {
                ArrayList<Integer> arrayList = new ArrayList<>();
                for (int j = 0; j < numOfNodes; j++) {
                    //System.out.println("  ..  "+tempMatrix[i][j]);
                    arrayList.add(tempMatrix[i][j]);
                }

                int row = arrayList.indexOf(1);
                int col = arrayList.lastIndexOf(1);

                System.out.println(">>>"+row + ", " + col);
                adjMatrix[row][col] = 1;
                adjMatrix[col][row] = 1;

                pathMatrix[row][col] = i;
                pathMatrix[col][row] = i;
            }

        }

        // Rumi
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("inputTest/link.txt"))){
            //BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("input/link.txt"))));
            String dataLine = bufferedReader.readLine();
            int numLinks = Integer.parseInt(dataLine);
            for (int i = 0; i < numLinks; i++) {
                dataLine = bufferedReader.readLine();
                StringTokenizer stringTokenizer = new StringTokenizer(dataLine, " ");
                int linkId = Integer.parseInt(stringTokenizer.nextToken());
                int nodeId1 = Integer.parseInt(stringTokenizer.nextToken());
                int nodeId2 = Integer.parseInt(stringTokenizer.nextToken());
                int segmentCount = Integer.parseInt(stringTokenizer.nextToken());

                double distance=0.0;
                for (int j = 0; j < segmentCount; j++) {
                    dataLine = bufferedReader.readLine();
                    stringTokenizer = new StringTokenizer(dataLine, " ");
                    int segmentId = Integer.parseInt(stringTokenizer.nextToken());
                    double startX = Double.parseDouble(stringTokenizer.nextToken());
                    double startY = Double.parseDouble(stringTokenizer.nextToken());
                    double startZ = Double.parseDouble(stringTokenizer.nextToken());
                    double endX = Double.parseDouble(stringTokenizer.nextToken());
                    double endY = Double.parseDouble(stringTokenizer.nextToken());
                    double endZ = Double.parseDouble(stringTokenizer.nextToken());
                    double segmentWidth = Double.parseDouble(stringTokenizer.nextToken());

                    distance+= getDistance(startX,startY,startZ,endX,endY,endZ);

                }
                DistMatrix[nodeId1][nodeId2] = distance;     // CSE18
                DistMatrix[nodeId2][nodeId1] = distance;
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }
        // Rumi

        try (BufferedReader br = new BufferedReader(new FileReader("inputTest/parameter.txt"))) {
            while (true) {
                String dataLine = br.readLine();
                if (dataLine == null) {
                    br.close();
                    break;
                }
                StringTokenizer stringTokenizer = new StringTokenizer(dataLine);
                String name = stringTokenizer.nextToken();
                String value = stringTokenizer.nextToken();
                if (name.equalsIgnoreCase("DemandType")) {
                    demandType = Integer.parseInt(value);
                } else if (name.equalsIgnoreCase("RandomSeed")) {
                    int seed = Integer.parseInt(value);
                    rand = seed < 0 ? new Random() : new Random(seed);
                } else if (name.equalsIgnoreCase("LowRate")) {
                    LOW_RATE = Integer.parseInt(value);
                } else if (name.equalsIgnoreCase("MediumRate")) {
                    MEDIUM_RATE = Integer.parseInt(value);
                } else if (name.equalsIgnoreCase("HighRate")) {
                    HIGH_RATE = Integer.parseInt(value);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Rumi
    private void floydWarshall_usingdistance() {        // CSE18

        next = new int[DistMatrix.length][DistMatrix.length];

        for (int i = 0; i < next.length; i++) {
            for (int j = 0; j < next.length; j++) {
                if (i != j) {
                    next[i][j] = j;
                }
            }
        }

        for (int k = 0; k < DistMatrix.length; k++) {
            for (int i = 0; i < DistMatrix.length; i++) {
                for (int j = 0; j < DistMatrix.length; j++) {
                    if (DistMatrix[i][k] + DistMatrix[k][j] < DistMatrix[i][j]) {
                        DistMatrix[i][j] = DistMatrix[i][k] + DistMatrix[k][j];
                        //System.out.println("dist of "+i+" "+j+" "+ DistMatrix[i][j]);
                        next[i][j] = next[i][k];
                    }
                }
            }
        }
        //Dist Matrix
        for (int i = 0; i < DistMatrix.length; i++) {
            for (int j = 0; j < DistMatrix.length; j++) {
                System.out.println("dist of "+i+" "+j+" = "+DistMatrix[i][j]);
            }
        }
    }
    // Rumi

    private void floydWarshall() {

        next = new int[adjMatrix.length][adjMatrix.length];

        for (int i = 0; i < next.length; i++) {
            for (int j = 0; j < next.length; j++) {
                if (i != j) {
                    next[i][j] = j;
                }
            }
        }

        for (int k = 0; k < adjMatrix.length; k++) {
            for (int i = 0; i < adjMatrix.length; i++) {
                for (int j = 0; j < adjMatrix.length; j++) {
                    if (adjMatrix[i][k] + adjMatrix[k][j] < adjMatrix[i][j]) {
                        adjMatrix[i][j] = adjMatrix[i][k] + adjMatrix[k][j];
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

    }

    private void printResult(int src, int dest) {
        int u = src;
        int v = dest;
        if(trainNodePairs.contains(new TrainNodePair(src,dest)) ){

            System.out.println("Dhukse ++++++++++++++++++"+  src + " "+dest );
            outPath += u + " " + v + " ";
            outDemand += u + " " + v + " ";

            StringBuilder path = new StringBuilder("" + u);

            do {
                u = next[u][v];
                path.append(" ").append(u);
            } while (u != v);

            String[] split = path.toString().split(" ");
            path = new StringBuilder();

            for (int i = 0; i < split.length - 1; i++) {
                path.append(pathMatrix[Integer.parseInt(split[i])][Integer.parseInt(split[i + 1])]).append(" ");
            }

            outPath += path + "\n";
            outDemand+= 1 + "\n";
        }else if(trainNodes.contains(src) || trainNodes.contains(dest)){

        }else{
            outPath += u + " " + v + " ";
            outDemand += u + " " + v + " ";

            StringBuilder path = new StringBuilder("" + u);

            do {
                u = next[u][v];
                path.append(" ").append(u);
            } while (u != v);

            String[] split = path.toString().split(" ");
            path = new StringBuilder();

            for (int i = 0; i < split.length - 1; i++) {
                path.append(pathMatrix[Integer.parseInt(split[i])][Integer.parseInt(split[i + 1])]).append(" ");
            }

            outPath += path + "\n";
            outDemand += demand + "\n";
        }



    }

    private void modifyDemand() {
        int i = outNode.size() - 1;
        int acceptableNode = setDemand();


        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("inputTest/demand.txt"));
             PrintWriter pw = new PrintWriter(new File("inputTest/demand_mod.txt"))) {
            int limit = Integer.parseInt(br.readLine());
            System.out.println("limit : "+limit);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < limit; j++) {
                String s = br.readLine();
                if (rand.nextInt(i) < acceptableNode) {
                    sb.append(s).append("\n");
                    count++;
                }
            }
            pw.printf("%d\n", count);
            pw.print(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File f1 = new File("inputTest/demand_mod.txt");
        File f2 = new File("inputTest/demand.txt");
        boolean b = f1.renameTo(f2);
        if (!b)
            System.out.println("Rename failed");
    }

    public static void main(String[] args) {
        try {
            new Sim();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
