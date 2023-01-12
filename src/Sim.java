import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

public class Sim {

    private ArrayList<Integer> outNode = new ArrayList<>();
    private static int demandType; //0 low; 1 mid; 2 high
    private static int LOW_RATE = 100;
    private static int MEDIUM_RATE = 500;
    private static int HIGH_RATE = 1000;

    private int demand = 100;
    private int[][] adjMatrix, pathMatrix, next;
    private Random rand = new Random();

    private String outPath = "", outDemand = "";

    private Sim() throws Exception {
        readFile();
        floydWarshall();
        setDemand();

        int i = 0;
        for (int k = 0; k < outNode.size(); k++) {
            for (int j = 0; j < outNode.size(); j++) {
                if (k == j) continue;

                Integer src = outNode.get(k);
                Integer dest = outNode.get(j);

                printResult(src, dest);
                //printResult(dest, src);

                i++;
            }
        }
        outPath = i + "\n" + outPath;
        outDemand = i + "\n" + outDemand;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("path.txt"))) {
            bw.write(outPath);
            System.out.println(outPath);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("demand.txt"))) {
            bw.write(outDemand);
            System.out.println(outDemand);
        }

        modifyDemand();
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
        floydWarshall();

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

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("path.txt"))) {
            bw.write(outPath);
            System.out.println(outPath);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("demand.txt"))) {
            bw.write(outDemand);
            System.out.println(outDemand);
        }
    }

    private void readFile() throws Exception {
        int numOfLinks = Integer.parseInt(new BufferedReader(new FileReader("link.txt")).readLine());

        try (BufferedReader br = new BufferedReader(new FileReader("node.txt"))) {
            int numOfNodes = Integer.parseInt(br.readLine());

            int[][] tempMatrix = new int[numOfLinks][numOfNodes];

            for (int i = 0; i < numOfNodes; i++) {
                String[] split = br.readLine().split(" ");
                String[] links = new String[split.length - 3];
                System.arraycopy(split, 3, links, 0, links.length);

                for (String link : links) {
                    tempMatrix[Integer.parseInt(link)][i] = 1;
                }

                //System.out.println(Arrays.toString(links));
                if (links.length == 1) {
                    outNode.add(i);
                }
            }

//            for (int i = 0; i < numOfLinks; i++) {
//                System.out.println(Arrays.toString(tempMatrix[i]));
//            }
            adjMatrix = new int[numOfNodes][numOfNodes];
            pathMatrix = new int[numOfNodes][numOfNodes];

            for (int i = 0; i < numOfNodes; i++) {
                int INF = 99999;
                Arrays.fill(adjMatrix[i], INF);
                adjMatrix[i][i] = 0;
            }

            for (int i = 0; i < numOfLinks; i++) {
                ArrayList<Integer> arrayList = new ArrayList<>();
                for (int j = 0; j < numOfNodes; j++) {
                    arrayList.add(tempMatrix[i][j]);
                }

                int row = arrayList.indexOf(1);
                int col = arrayList.lastIndexOf(1);

                //System.out.println(row + ", " + col);
                adjMatrix[row][col] = 1;
                adjMatrix[col][row] = 1;

                pathMatrix[row][col] = i;
                pathMatrix[col][row] = i;
            }

        }

        try (BufferedReader br = new BufferedReader(new FileReader("parameter.txt"))) {
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

    private void modifyDemand() {
        int i = outNode.size() - 1;
        int acceptableNode = setDemand();


        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("demand.txt"));
             PrintWriter pw = new PrintWriter(new File("demand_mod.txt"))) {
            int limit = Integer.parseInt(br.readLine());
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
        File f1 = new File("demand_mod.txt");
        File f2 = new File("demand.txt");
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
