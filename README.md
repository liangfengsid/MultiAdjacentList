##MultiAdjacentList Benchmark 
MultiAdjacentList is a graph computing application which generates the heads and tails of lists of various length from the information of edges. In every iteration where a key Li represents a list of length i, we generate a group of new lists of length i+1 by appending each head node and each tail node to it, where each new list acts as the key and its new head nodes and tail nodes act as the value. 

Acknowledge: It is a extension of the "[Adjacency-List](https://engineering.purdue.edu/~puma/pumabenchmarks.htm)" benchmark developed by the Faraz Ahmad in Purdue University. 

##Dataset Description:
Each line of the source dataset is the nodes of two edges, in the format: Vertex1 Vertex2. 
In Iteration i, the program generates the length-i list as the key, accompanied with the head nodes and tail nodes as the value. Each line of the output is: (List, (Heads, Tails)), where the nodes of the List, Heads and Tails are separated by a space, e.g.., "Node1 Node2 Node3".

##Dataset Generation: 
The input dataset for the benchmark can be generated by the Java programs in the "[java](https://github.com/liangfengsid/MultiAdjacentList/tree/master/java)" directory.

##MultiAdjacentList Program: 
The Scala source code is available at [AdjacentList.scala](https://github.com/liangfengsid/MultiAdjacentList/blob/master/AdjacentList.scala). 
The compiled package is available at [AdjacentList.jar](https://github.com/liangfengsid/MultiAdjacentList/tree/master/lib). 

The program can be run in Spark with the following command: 
```
spark-submit [spark-related options] \\
--class AdjancentList AdjancentList.jar <listMaxLength> <inputPath> <outputPath> <isPartitioned> \\
[<numPartitions> <isHead>] 
```

For example:
```
spark-submit --conf 'spark.shuffle.blockTransferService=nio' \\
--conf 'spark.task.executor.binding.enable=true' \\
--master yarn-client --executor-memory 8g --driver-memory 5g --num-executors 16 \\
--class AdjancentList prefixcount_2.10-1.0.0.jar 2 /edges.txt /adjOutTrue true
```
