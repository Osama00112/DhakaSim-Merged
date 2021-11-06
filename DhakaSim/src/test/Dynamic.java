// CSE18
package test;

import test.DhakaSimFrame;
import test.DhakaSimPanel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

import static java.lang.Math.sqrt;

//Rumi
//created this class to execute the paths dynamically by selecting best route with the help of average time of the link
public class Dynamic {
    private static int[][] next;//next will contain further nodes to execute path
    private static int[][] link;//link between two nodes
    private double[][] timeMatrix;// for every node the average time of link is stored here
    private double[] linkWeight_uptodown;
    private double[] linkWeight_downtoup;
    private int step;
    private int numOfNodes;
    //private double almost_zero = 0.0000001;
    public int nextLink(int currentNode,int destination)
    {
        if(DhakaSimFrame.simulationStep>=step+50 && DhakaSimFrame.simulationStep>=500 )
        {
            step= DhakaSimFrame.simulationStep;
            System.out.println(step);
            recalculate_graphinfo_usingFloydWarshall();
            //if this function ( recalculate_graphinfo_usingFloydWarshall()) is comment out then the path will be static
        }
        System.out.println(currentNode+" "+next[currentNode][destination]+" "+link[currentNode][next[currentNode][destination]]);
        return link[currentNode][next[currentNode][destination]];

    }
    // Rumi
    void recalculate_graphinfo_usingFloydWarshall()
    {
        for(int i=0;i<linkWeight_uptodown.length;i++)
        {
            linkWeight_uptodown[i]= DhakaSimPanel.linkList.get(i).getAverageTimeonLink1();
            //if(linkWeight_uptodown[i]==0.0)linkWeight_uptodown[i] = almost_zero;
            // System.out.println("id "+DhakaSimPanel.linkList.get(i).getId());
            System.out.println(i+" 1 "+linkWeight_uptodown[i]);
        }
        for(int i=0;i<linkWeight_downtoup.length;i++)
        {
            linkWeight_downtoup[i]= DhakaSimPanel.linkList.get(i).getAverageTimeonLink2();
            //if(linkWeight_downtoup[i]==0.0)linkWeight_downtoup[i] = almost_zero;
            System.out.println(i+" 2 "+linkWeight_downtoup[i]);
        }
        for(int i=0;i<numOfNodes;i++) Arrays.fill(timeMatrix[i], 99999999.00);
        for(int i=0;i<numOfNodes;i++)
        {
            for(int j=0;j<numOfNodes;j++)
            {
                if(i==j)
                {
                    timeMatrix[i][j]=0.0;
                }
                if(link[i][j]!=-1)
                {
                    if(j == DhakaSimPanel.linkList.get(link[i][j]).getDownNode())
                    {
                        timeMatrix[i][j]=linkWeight_uptodown[link[i][j]];
                    }
                    else
                    {
                        timeMatrix[i][j]=linkWeight_downtoup[link[i][j]];
                    }
                }

            }
        }
        for (int i = 0; i < next.length; i++) {
            for (int j = 0; j < next.length; j++) {
                if (i != j) {
                    next[i][j] = j;
                }
            }

        }

        for (int k = 0; k < timeMatrix.length; k++) {
            for (int i = 0; i < timeMatrix.length; i++) {
                for (int j = 0; j < timeMatrix.length; j++) {
                    if (timeMatrix[i][k] + timeMatrix[k][j] < timeMatrix[i][j]) {
                        timeMatrix[i][j] = timeMatrix[i][k] + timeMatrix[k][j];
                        //System.out.println("dist of "+i+" "+j+" "+ DistMatrix[i][j]);
                        next[i][j] = next[i][k];
                    }
                }
            }
        }

    }
    Dynamic()
    {
        try {
            Sim s=new Sim();
            next=s.getNext();
            link=s.getPathMatrix();
            numOfNodes=next.length;
            timeMatrix=new double[numOfNodes][numOfNodes];
            step=0;
            //System.out.println(DhakaSimPanel.linkList.size());
            linkWeight_uptodown=new double[DhakaSimPanel.linkList.size()];
            linkWeight_downtoup=new double[DhakaSimPanel.linkList.size()];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Created sim class again to get the matrixes
    public class Sim {

        private ArrayList<Integer> outNode = new ArrayList<>();

        private int[][]  pathMatrix, next;
        private double[][] DistMatrix;  // Rumi
        private Random rand = new Random();

        private String outPath = "", outDemand = "";

        private double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
            return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        }
        // public from private
        public Sim() throws Exception {
            readFile();
            //floydWarshall();
            floydWarshall_usingdistance();
        }
        // Rumi
        public int[][] getNext()
        {
            return next;
        }
        public int[][] getPathMatrix() {
            return pathMatrix;
        }

        private void readFile() throws Exception {
            int numOfLinks = Integer.parseInt(new BufferedReader(new FileReader("inputTest/link.txt")).readLine());

            try (BufferedReader br = new BufferedReader(new FileReader("inputTest/node.txt"))) {
                int numOfNodes = Integer.parseInt(br.readLine());

                int[][] tempMatrix = new int[numOfLinks][numOfNodes];

                for (int i = 0; i < numOfNodes; i++) {
                    String[] split = br.readLine().split(" ");
                    System.out.println("######    "+Arrays.toString(split));
                    String[] links = new String[split.length - 5];
                    System.arraycopy(split, 5, links, 0, links.length);
                    if (split[4].equals("1")){
                        //trainNodes.add(Integer.parseInt(split[0]));
                    }

                    for (String link : links) {
                        tempMatrix[Integer.parseInt(link)][i] = 1;
                    }

                    System.out.println("********   "+Arrays.toString(links));
                    if (links.length == 1) {
                        outNode.add(i);
                    }
                }

//            for (int i = 0; i < numOfLinks; i++) {
//                System.out.println(Arrays.toString(tempMatrix[i]));
//            }

                pathMatrix = new int[numOfNodes][numOfNodes];
                DistMatrix = new double[numOfNodes][numOfNodes];  // Rumi

                for (int i = 0; i < numOfNodes; i++) {
                    int INF = 99999;

                    Arrays.fill(DistMatrix[i], INF);  // Rumi
                    Arrays.fill(pathMatrix[i], -1);
                    DistMatrix[i][i]=0;
                }

                for (int i = 0; i < numOfLinks; i++) {
                    ArrayList<Integer> arrayList = new ArrayList<>();
                    for (int j = 0; j < numOfNodes; j++) {
                        arrayList.add(tempMatrix[i][j]);
                    }

                    int row = arrayList.indexOf(1);
                    int col = arrayList.lastIndexOf(1);

                    //System.out.println(row + ", " + col);

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
                    DistMatrix[nodeId1][nodeId2] = distance;
                    DistMatrix[nodeId2][nodeId1] = distance;
                }
            }catch(IOException e)
            {
                e.printStackTrace();
            }            // Rumi



        }


        // Rumi
        private void floydWarshall_usingdistance() {

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
        }       // Rumi


    }

    public static void main(String[] args) {
        Dynamic d=new Dynamic();
    }

}
