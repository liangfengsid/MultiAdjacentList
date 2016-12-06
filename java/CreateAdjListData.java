import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


/**
 * Synthetic data generation for AdjList application. We assume
 *
 * There are three steps of data generation:
 * 1. creating vertices : count = numVertices, vertex-size = 20 bytes
 *              each vertex is accompanied by two numbers, first is out-degree, next is in-degree.
 *              average value for rank (out-degree and in-degree) is assumed to be equal to avgRank.
 *              the rank generation follows power-law (a zipf distribution)
 * 2. creating ranks file by normalizing vertex ranks as per avgRank.
 * 3. creating edges from the out-degree and in-degree of nodes.
 *
 * Output file size is approximated by: avgRank * numVertices * 2 * 20B
 *
 * Ref: Ravi Kumar, P Raghavan, Extracting Large-Scale Knowledge Bases from the Web, VLDB '99
 *
 * NOTE: This file requires Colt library to be installed http://acs.lbl.gov/software/colt/install.html
 * @author Feng Liang 
 * The University Hong Kong
 * 
 * Acknowledge: This code is modified from the code written by  Faraz Ahmad.
 * The related information can be found in https://engineering.purdue.edu/~puma/pumabenchmarks.htm
 *
 */

public class CreateAdjListData {

        /**
         * @param args
         * Usage: CreateAdjListData [-out<i>outputdir</i>][-N][-L][-D][-T][-I][-B]
         */
        private static int numVertices = 25000000;
        private static double skew = 1.0;
        private static double avgRank = 2.0; // out-degree(/in-degree) of a vertex (8.0)
//      private static int vertexLength = 20; // vertex length in bytes
        private static int MAX_RANK = 6; // max out-degree = max in-degree
        private static File dataPath = new File(".");
        private static File verticesFile = new File(dataPath + "/combined.txt");
        private static File rankFile = new File(dataPath + "/ranks.txt");
        private static File edgeFile = new File(dataPath + "/edges.txt");


        public static void createVertices() throws IOException {
                System.out.println("Step 1 of 3: Vertices generation");
                System.out.println("=============================");
                System.out.println("Number of vertices = " + numVertices);


                String entry = null;
                String formatted = new String("");

                if(!verticesFile.exists()){
                        verticesFile.createNewFile();
                }
                else {
                        System.out.println("File " + verticesFile.getAbsolutePath() + " exists, skipping Item Generation step");
                        return;
                }
                OutputStream out = new FileOutputStream(verticesFile);


                ZipfDistribution zipf = new ZipfDistribution(MAX_RANK, skew);

                int nextRank, nextInRank;
                byte[] buf;
                System.out.println("Writing to file: " + verticesFile.getAbsolutePath());

                for(int k = 0; k < numVertices; k++){
                        entry = new String("");
                        formatted = String.format("%010d", k);
                        nextRank = zipf.next() + 1; // offsetting by 1 to help normalization
                        nextInRank = zipf.next() + 1;
                        entry = entry + formatted + "," + nextRank + "," + nextInRank + "\n";
                        //System.out.println(nextRank);
                        buf = entry.getBytes();
                        out.write(buf);
                }
                out.close();
        }

        public static void normalizeRanks() throws IOException {
                System.out.println("Step 2 of 3: Normalization of ranks");
                System.out.println("=============================");
                System.out.println("Average rank (out-degree) = " + avgRank);

                String entry = new String("");

			    Calendar calendar = new GregorianCalendar();
			    String am_pm;
			    int hour, minute, second;


                if(!rankFile.exists()){
                        rankFile.createNewFile();
                }
                else {
                        System.out.println("File " + rankFile.getAbsolutePath() + " exists, skipping Item Generation step");
                        return;
                }
                OutputStream out = new FileOutputStream(rankFile);

                Scanner scanner = new Scanner(verticesFile);

                String[] vertices = new String[numVertices];
                int[] ranks = new int[numVertices];
                int[] inRanks = new int[numVertices];
                int[] normalizedRanks = new int[numVertices];
                int[] normalizedInRanks = new int[numVertices];

                int i = 0;
                String line = new String();
                while (scanner.hasNext()){
                        line = scanner.next();
                        Scanner innerScan = new Scanner(line).useDelimiter(",");
                        while (innerScan.hasNext()){
                                vertices[i] = innerScan.next();
                                ranks[i] = innerScan.nextInt();
                                inRanks[i++] = innerScan.nextInt();
                        }
                        if ( i >= numVertices) break;
                        if(i % 1000000 == 0){
                    hour = calendar.get(Calendar.HOUR);
                    minute = calendar.get(Calendar.MINUTE);
                    second = calendar.get(Calendar.SECOND);
                    if(calendar.get(Calendar.AM_PM) == 0)
                      am_pm = "AM";
                    else
                      am_pm = "PM";
                    System.out.println("Current Time : " + hour + ":"
                + minute + ":" + second + " " + am_pm);
                                System.out.println("i = " + i + ", max = " + numVertices);

                        }
                }
                System.out.println("read file complete");
                int sum = 0, inSum = 0;
                int roundVal = 0;
                float avg = 0.0f, inAvg = 0.0f;
                float norm = 0.0f;
                for (int j = 0; j < numVertices; j++){
                        sum += ranks[j];
                        inSum += inRanks[j];
                }
                avg = (float)sum / (float) numVertices;
                inAvg = (float)inSum / (float) numVertices;
                norm = (float) (avgRank * numVertices);

                //System.out.println("avg = " + avg + ",sum = "+ sum + ", norm = " + norm);
                for (int j = 0; j < numVertices; j++){
                        roundVal = Math.round(((float)ranks[j] / (float) sum) * norm) ;
                        normalizedRanks[j] = roundVal;
                        roundVal = Math.round(((float)inRanks[j] / (float) inSum) * norm) ;
                        normalizedInRanks[j] = roundVal;
                }
                byte[] buf;
                System.out.println("Writing to file: " + rankFile.getAbsolutePath());
                System.out.println("");


                for(int k = 0; k < numVertices; k++){
                        entry = vertices[k] + "," + normalizedRanks[k] + "," + normalizedInRanks[k] + "\n";
                        buf = entry.getBytes();
                        out.write(buf);
                        if(k % 1000000 == 0) {
                    hour = calendar.get(Calendar.HOUR);
                    minute = calendar.get(Calendar.MINUTE);
                    second = calendar.get(Calendar.SECOND);
                    if(calendar.get(Calendar.AM_PM) == 0)
                      am_pm = "AM";
                    else
                      am_pm = "PM";
                    System.out.println("Current Time : " + hour + ":"
                + minute + ":" + second + " " + am_pm);
                                System.out.println("k = " + k + ", max = " + numVertices);
                        }
                }
                out.close();
        }

        public static void createEdges() throws IOException {
                System.out.println("Step 3 of 3: Edge generation");
                System.out.println("=============================");

                String entry = new String("");
			    Calendar calendar = new GregorianCalendar();
			    String am_pm;
			    int hour, minute, second;


                if(!edgeFile.exists()){
                        edgeFile.createNewFile();
                }
                else {
                        System.out.println("File " + edgeFile.getAbsolutePath() + " exists, skipping Item Generation step");
                        return;
                }

                Scanner scanner = new Scanner(rankFile);

                int inVal = 0, outVal = 0;
                String line = null;
                String vertex = new String("");
                OutputStream out = new FileOutputStream(edgeFile);
                Random rand=new Random(System.currentTimeMillis());

                int n=0;
                while (scanner.hasNext()){
                        line = scanner.next();
                        Scanner innerScan = new Scanner(line).useDelimiter(",");
                        while (innerScan.hasNext()){
                                vertex = innerScan.next();
                                outVal = innerScan.nextInt();
                                inVal = innerScan.nextInt();

                                HashSet<String> outVertices=new HashSet<String>();
                                while(outVertices.size()!=outVal){
                                        String outVertex= String.format("%010d", rand.nextInt(numVertices));
                                        if(!outVertex.equals(vertex)){
                                                outVertices.add(outVertex);
                                        }
                                }
                                StringBuilder builder=new StringBuilder();
                                for(String v: outVertices){
                                        builder.append(vertex+","+v+"\n");
                                }
                                out.write(builder.toString().getBytes());
                        }
                        n++;
                        if(n % 1000000 == 0){
                        System.out.println(n);
                        }
                }


                out.close();
        }

        public static void main(String[] args) throws IOException {

                System.out.println("CreateAdjListData started");
                // specify path where we want to put the generated data

                for(int i=0; i < args.length; ++i) {
                if ("-size".equals(args[i])) {
                        numVertices = Integer.valueOf(args[++i]);
                } else if ("-skew".equals(args[i])) {
                        skew = Double.valueOf(args[++i]);
                } else if ("-avgod".equals(args[i])) {
                        avgRank = Double.valueOf(args[++i]);
                } else {
                        System.out.println("Invalid argument = " + args[i] + ", ignoring");
                }
          }
                createVertices();
                normalizeRanks();
                createEdges();

                System.out.println("CreateAdjListData finished");
        }
}

