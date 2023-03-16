import test.Passenger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

public class PrintStat {
    public static void main(String[] args) {

        //let's check the passengers activity here at first
        ArrayList<Passenger> passengers = new ArrayList<>();
        Random random = new Random();
        for(int i  = 0 ; i < 3000 ; i++){
            int node = random.nextInt(11);
            passengers.add(new Passenger(i, node));
        }

        for(int i  = 0 ; i < 3000 ; i++){
            if(i%5 == 0) {
                int node = passengers.get(i).getCurrentNodeID();
                int type = random.nextInt(6);
                passengers.get(i).startJourney(node, type, i * 2);
            }
        }
        for(int i  = 0 ; i < 3000 ; i++){
            if(i%5 == 0) {
                int node = random.nextInt(11);
                if(node == passengers.get(i).getCurrentNodeID()){
                    node += 1;
                }
                passengers.get(i).addNodeToPathWhileTravelling(node, i * 4);
            }
        }
        for(int i  = 0 ; i < 3000 ; i++){
            if(i%5 == 0) {
                int node = random.nextInt(11);
                if(node == passengers.get(i).getCurrentNodeID()){
                    node += 1;
                }
                passengers.get(i).finishJourney(node, i * 6);
            }
        }

        ///2nd one

        for(int i  = 0 ; i < 3000 ; i++){
            if(i%5 == 0) {
                int node = passengers.get(i).getCurrentNodeID();
                int type = random.nextInt(6);
                passengers.get(i).startJourney(node, type, i * 8);
            }
        }
        for(int i  = 0 ; i < 3000 ; i++){
            if(i%5 == 0) {
                int node = random.nextInt(11);
                if(node == passengers.get(i).getCurrentNodeID()){
                    node += 1;
                }
                passengers.get(i).addNodeToPathWhileTravelling(node, i * 10);
            }
        }

        for(int i  = 0 ; i < 3000 ; i++){
            if(i%5 == 0) {
                int node = random.nextInt(11);
                if(node == passengers.get(i).getCurrentNodeID()){
                    node += 1;
                }
                passengers.get(i).addNodeToPathWhileTravelling(node, i * 12);
            }
        }

        for(int i  = 0 ; i < 3000 ; i++){
            if(i%5 == 0) {
                int node = random.nextInt(11);
                if(node == passengers.get(i).getCurrentNodeID()){
                    node += 1;
                }
                passengers.get(i).finishJourney(node, i * 15);
            }
        }

        ///



        for(int i  = 0 ; i < 3000 ; i++){
            if(i%5 == 0){
                System.out.println(passengers.get(i));
            }
        }
        System.out.println("size "+passengers.size());
//
//        try{
//            BufferedReader br = new BufferedReader(new FileReader("statistics/journeys.csv"));
//            String str = br.readLine();
//            int u = 10, v = 12, totalPassengers = 0, totalTime = 0;
//            while(str != null){
//                //System.out.println(str);
//                String[] items = str.split(",");
////                System.out.println(items);
//                int source = Integer.parseInt(items[1]);
//                int destination = Integer.parseInt(items[2]);
//                if ((source == u && destination == v )||(source == v && destination == u )||(source == 13 && destination == 14 ) ){
//                    int count = Integer.parseInt(items[3]);
//                    int time = Integer.parseInt(items[4]);
//                    totalPassengers += count;
//                    totalTime += time*count;
//                }
//                str = br.readLine();
//            }
//            System.out.println(" count "+totalPassengers+" time "+totalTime);
//            System.out.println(" avg time per passenger "+totalTime*1.0/totalPassengers);
//        }catch (Exception e){
//            System.out.println(e.getMessage());
//        }
    }
}
