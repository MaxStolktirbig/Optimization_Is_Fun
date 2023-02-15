import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {

        int stockDataSize = 1000000;
        int maxStockDataVal = 100000;
        int amountOfQueries = 250;


        List<Integer> stockData = new ArrayList<>();




        for(int index = 0; index<stockDataSize; index++){
            stockData.add((int)(Math.random()*maxStockDataVal));
        }
        ;
        List<Integer> queries = new ArrayList<>();
        for(int additionalIndex = 1; additionalIndex<amountOfQueries;additionalIndex++){
            //queries.add((int)(Math.random()*stockDataSize));
            queries.add(stockData.indexOf(Collections.min(stockData)));
        }
        int runcount = 5;
        long[] noStreamTimes = new long[runcount+1];

        long[] noStreamMinTimes = new long[runcount+1];
        long[] noStreamKnownTimes = new long[runcount+1];
        long[] noStreamMinKnownTimes = new long[runcount+1];
        long[] mapLookupMinKnownTimes = new long[runcount+1];
        long[] mapLookupMinTimes = new long[runcount+1];
        long[] streamTimes = new long[runcount+1];

        long noStreamTimesTotal = 0;
        for(int i = 0; i < runcount; i++) {
            long currentTime = System.nanoTime();
            predictAnswerNoStream(stockData, queries);
            noStreamTimes[i] = System.nanoTime()-currentTime;
            noStreamTimesTotal+=noStreamTimes[i];
        }
        noStreamTimes[runcount] = noStreamTimesTotal/runcount;

        long noStreamKnownTimesTotal = 0;
        for(int i = 0; i < runcount; i++) {
            long currentTime = System.nanoTime();
            predictAnswerNoStreamWithCheckingKnownValues(stockData, queries);
            noStreamKnownTimes[i] = System.nanoTime()-currentTime;
            noStreamKnownTimesTotal+=noStreamKnownTimes[i];
        }
        noStreamKnownTimes[runcount] = noStreamKnownTimesTotal/runcount;

        long noStreamMinTimesTotal = 0;
        for(int i = 0; i < runcount; i++) {
            long currentTime = System.nanoTime();
            predictAnswerNoStreamWithCheckingMinimum(stockData, queries);
            noStreamMinTimes[i] = System.nanoTime()-currentTime;
            noStreamMinTimesTotal += noStreamMinTimes[i];
        }
        noStreamMinTimes[runcount] = noStreamMinTimesTotal/runcount;

        long noStreamMinKnownTimesTotal = 0;
        for(int i = 0; i < runcount; i++) {
            long currentTime = System.nanoTime();
            predictAnswerNoStreamWithCheckingMinimumAndKnownValues(stockData, queries);
            noStreamMinKnownTimes[i] = System.nanoTime()-currentTime;
            noStreamMinKnownTimesTotal+=noStreamMinKnownTimes[i];
        }
        noStreamMinKnownTimes[runcount] = noStreamMinKnownTimesTotal/runcount;

        long mapLookupMinKnownTimesTotal = 0;
        for(int i = 0; i < runcount; i++) {
            long currentTime = System.nanoTime();
            predictAnswerMapLookupWithCheckingMinimumAndKnownValues(stockData, queries);
            mapLookupMinKnownTimes[i] = System.nanoTime()-currentTime;
            mapLookupMinKnownTimesTotal+=mapLookupMinKnownTimes[i];
        }
        mapLookupMinKnownTimes[runcount] = mapLookupMinKnownTimesTotal/runcount;

        long mapLookupMinTimesTotal = 0;
        for(int i = 0; i < runcount; i++) {
            long currentTime = System.nanoTime();
            predictAnswerMapLookupWithCheckingMinimumValue(stockData, queries);
            mapLookupMinTimes[i] = System.nanoTime()-currentTime;
            mapLookupMinTimesTotal+=mapLookupMinTimes[i];
        }
        mapLookupMinTimes[runcount] = mapLookupMinTimesTotal/runcount;

        long streamTimesTotal = 0;
        for(int i = 0; i < runcount; i++) {
            long currentTime = System.nanoTime();
            predictAnswerStreaming(stockData, queries);
            streamTimes[i] = System.nanoTime()-currentTime;
            streamTimesTotal+=streamTimes[i];
        }
        streamTimes[runcount] = streamTimesTotal/runcount;

        String prefix = "%5d -20s%s %5d\n";
        System.out.println("Runs with last as average value:");
        System.out.println("noStreamTimes      noStreamMinTimes        noStreamKnownTimes         noStreamMinKnownTimes    mapLookupMinKnownTimes   mapLookupMinTimes          streamTimes");
        for (int i = 0; i<=runcount;i++) {
            System.out.println(noStreamTimes[i] + "                 "+noStreamMinTimes[i]+ "                 "+noStreamKnownTimes[i]+ "                 "+noStreamMinKnownTimes[i]+ "                 "+mapLookupMinKnownTimes[i]+ "                 "+mapLookupMinTimes[i]+ "                 "+ streamTimes[i]);
        }
    }




    public static List<Integer> predictAnswerMapLookupWithCheckingMinimumValue(List<Integer> stockData, List<Integer> queries) {
        ArrayList<Integer> answers = new ArrayList<>();
        int stockDataSize = stockData.size();
        Integer minimumValue = stockData.get(0);
        HashMap<Integer, Integer> stockDataMap = new HashMap<>();
        for (int index = 0; index<stockDataSize; index++) {
            Integer value = stockData.get(index);
            minimumValue = value < minimumValue ? value : minimumValue;
            stockDataMap.put(index, value);
        }
        for(int query : queries){
            int queryIndex = query-1;
            int queryValue = stockData.get(queryIndex);
            if(queryValue==minimumValue){
                answers.add(-1);
                continue;
            }

            int closestIndexUpper = -1;
            int upperDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex < stockDataSize; stockDataIndex++){
                upperDistance++;
                if(stockDataMap.get(stockDataIndex) < queryValue){
                    closestIndexUpper = stockDataIndex;
                    break;
                }
            }
            int closestIndexLower = -1;
            int lowerDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex > 0; stockDataIndex--){
                lowerDistance++;
                if(stockDataMap.get(stockDataIndex) < queryValue){
                    closestIndexLower = stockDataIndex;
                    break;
                }
            }
            if(closestIndexLower == -1 && closestIndexUpper == -1){
                answers.add(-1);
            } else if (closestIndexUpper==-1){
                answers.add(closestIndexLower+1);
            } else if (closestIndexLower==-1){
                answers.add(closestIndexUpper+1);
            } else {
                int finalIndex = lowerDistance > upperDistance ? closestIndexUpper : closestIndexLower;
                if(lowerDistance == upperDistance){
                    finalIndex = closestIndexLower+1;
                }
                answers.add(finalIndex);
            }
        }
        return answers;

    }

    //this function tries to use a map lookup while memorizing the known values and checking minimum value
    public static List<Integer> predictAnswerMapLookupWithCheckingMinimumAndKnownValues(List<Integer> stockData, List<Integer> queries) {
        ArrayList<Integer> answers = new ArrayList<>();
        int stockDataSize = stockData.size();
        Integer minimumValue = stockData.get(0);
        HashMap<Integer, Integer> stockDataMap = new HashMap<>();
        for (int index = 0; index<stockDataSize; index++) {
            Integer value = stockData.get(index);
            minimumValue = value < minimumValue ? value : minimumValue;
            stockDataMap.put(index, value);
        }
        HashMap<Integer, Integer> knownIndexValues = new HashMap<>();
        for(int query : queries){
            int queryIndex = query-1;
            int queryValue = stockData.get(queryIndex);
            if(queryValue==minimumValue){
                answers.add(-1);
                knownIndexValues.put(queryIndex, -1);
                continue;
            }
            if(knownIndexValues.containsKey(queryIndex)){
                answers.add(knownIndexValues.get(queryIndex));
                continue;
            }
            int closestIndexUpper = -1;
            int upperDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex < stockDataSize; stockDataIndex++){
                upperDistance++;
                if(stockDataMap.get(stockDataIndex) < queryValue){
                    closestIndexUpper = stockDataIndex;
                    break;
                }
            }
            int closestIndexLower = -1;
            int lowerDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex > 0; stockDataIndex--){
                lowerDistance++;
                if(stockDataMap.get(stockDataIndex) < queryValue){
                    closestIndexLower = stockDataIndex;
                    break;
                }
            }
            if(closestIndexLower == -1 && closestIndexUpper == -1){
                answers.add(-1);
                knownIndexValues.put(queryIndex, -1);
            } else if (closestIndexUpper==-1){
                answers.add(closestIndexLower+1);
                knownIndexValues.put(queryIndex, closestIndexLower+1);
            } else if (closestIndexLower==-1){
                answers.add(closestIndexUpper+1);
                knownIndexValues.put(queryIndex, closestIndexUpper+1);
            } else {
                int finalIndex = lowerDistance > upperDistance ? closestIndexUpper : closestIndexLower;
                if(lowerDistance == upperDistance){
                    finalIndex = closestIndexLower+1;
                }
                answers.add(finalIndex);
                knownIndexValues.put(queryIndex, finalIndex);
            }
        }
        return answers;

    }

    public static List<Integer> predictAnswerNoStreamWithCheckingMinimumAndKnownValues(List<Integer> stockData, List<Integer> queries) {
        ArrayList<Integer> answers = new ArrayList<>();
        int stockDataSize = stockData.size();
        Integer minimumValue = stockData.get(0);
        for (Integer value : stockData) {
            minimumValue = value < minimumValue ? value : minimumValue;

        }
        HashMap<Integer, Integer> knownIndexValues = new HashMap<>();
        for(int query : queries){
            int queryIndex = query-1;
            int queryValue = stockData.get(queryIndex);
            //check if this is the minimum value so that we can skip the loops if so
            if(queryValue==minimumValue){
                answers.add(-1);
                knownIndexValues.put(queryIndex, -1);
                continue;
            }
            //check if we know this index, look it up if so so that we can skip the loops
            if(knownIndexValues.containsKey(queryIndex)){
                answers.add(knownIndexValues.get(queryIndex));
                continue;
            }
            int closestIndexUpper = -1;
            int upperDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex < stockDataSize; stockDataIndex++){
                upperDistance++;
                if(stockData.get(stockDataIndex) < queryValue){
                    closestIndexUpper = stockDataIndex;
                    break;
                }
            }
            int closestIndexLower = -1;
            int lowerDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex > 0; stockDataIndex--){
                lowerDistance++;
                if(stockData.get(stockDataIndex) < queryValue){
                    closestIndexLower = stockDataIndex;
                    break;
                }
            }
            if(closestIndexLower == -1 && closestIndexUpper == -1){
                answers.add(-1);
                knownIndexValues.put(queryIndex, -1);
            } else if (closestIndexUpper==-1){
                answers.add(closestIndexLower+1);
                knownIndexValues.put(queryIndex, closestIndexLower+1);
            } else if (closestIndexLower==-1){
                answers.add(closestIndexUpper+1);
                knownIndexValues.put(queryIndex, closestIndexUpper+1);
            } else {
                int finalIndex = lowerDistance > upperDistance ? closestIndexUpper : closestIndexLower;
                if(lowerDistance == upperDistance){
                    finalIndex = closestIndexLower+1;
                }
                answers.add(finalIndex);
                knownIndexValues.put(queryIndex, finalIndex);
            }
        }
        return answers;

    }

    public static List<Integer> predictAnswerNoStreamWithCheckingMinimum(List<Integer> stockData, List<Integer> queries) {
        // Write your code hereList<int> answers = new();
        //use arraylist to get an ordered list
        ArrayList<Integer> answers = new ArrayList<>();
        int stockDataSize = stockData.size();
        Integer minimumValue = stockData.get(0);
        for (Integer value: stockData
        ) {
            minimumValue = value < minimumValue ? value : minimumValue;
        }
        for(int query : queries){
            int queryIndex = query-1;
            int queryValue = stockData.get(queryIndex);
            if(queryValue==minimumValue){
                answers.add(-1);
                continue;
            }
            int closestIndexUpper = -1;
            int upperDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex < stockDataSize; stockDataIndex++){
                upperDistance++;
                if(stockData.get(stockDataIndex) < queryValue){
                    closestIndexUpper = stockDataIndex;
                    break;
                }
            }
            int closestIndexLower = -1;
            int lowerDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex > 0; stockDataIndex--){
                lowerDistance++;
                if(stockData.get(stockDataIndex) < queryValue){
                    closestIndexLower = stockDataIndex;
                    break;
                }
            }
            if(closestIndexLower == -1 && closestIndexUpper == -1){
                answers.add(-1);
            } else if (closestIndexUpper==-1){
                answers.add(closestIndexLower+1);
            } else if (closestIndexLower==-1){
                answers.add(closestIndexUpper+1);
            } else {
                int finalIndex = lowerDistance > upperDistance ? closestIndexUpper : closestIndexLower;
                if(lowerDistance == upperDistance){
                    finalIndex = closestIndexLower+1;
                }
                answers.add(finalIndex);
            }
        }
        return answers;
    }

    public static List<Integer> predictAnswerNoStreamWithCheckingKnownValues(List<Integer> stockData, List<Integer> queries) {
        // Write your code hereList<int> answers = new();
        //use arraylist to get an ordered list
        ArrayList<Integer> answers = new ArrayList<>();
        int stockDataSize = stockData.size();
        HashMap<Integer, Integer> knownIndexValues = new HashMap<>();
        for(int query : queries){
            int queryIndex = query-1;
            int queryValue = stockData.get(queryIndex);
            if(knownIndexValues.containsKey(queryIndex)){
                answers.add(knownIndexValues.get(queryIndex));
                continue;
            }
            int closestIndexUpper = -1;
            int upperDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex < stockDataSize; stockDataIndex++){
                upperDistance++;
                if(stockData.get(stockDataIndex) < queryValue){
                    closestIndexUpper = stockDataIndex;
                    break;
                }
            }
            int closestIndexLower = -1;
            int lowerDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex > 0; stockDataIndex--){
                lowerDistance++;
                if(stockData.get(stockDataIndex) < queryValue){
                    closestIndexLower = stockDataIndex;
                    break;
                }
            }
            if(closestIndexLower == -1 && closestIndexUpper == -1){
                answers.add(-1);
                knownIndexValues.put(queryIndex, -1);
            } else if (closestIndexUpper==-1){
                answers.add(closestIndexLower+1);
                knownIndexValues.put(queryIndex, closestIndexLower+1);
            } else if (closestIndexLower==-1){
                answers.add(closestIndexUpper+1);
                knownIndexValues.put(queryIndex, closestIndexUpper+1);
            } else {
                int finalIndex = lowerDistance > upperDistance ? closestIndexUpper : closestIndexLower;
                if(lowerDistance == upperDistance){
                    finalIndex = closestIndexLower+1;
                }
                answers.add(finalIndex);
                knownIndexValues.put(queryIndex, finalIndex);
            }
        }
        return answers;

    }
    public static List<Integer> predictAnswerNoStream(List<Integer> stockData, List<Integer> queries) {
        // Write your code hereList<int> answers = new();
        //use arraylist to get an ordered list
        ArrayList<Integer> answers = new ArrayList<>();
        int stockDataSize = stockData.size();

        for(int query : queries){
            int queryIndex = query-1;
            int queryValue = stockData.get(queryIndex);
            int closestIndexUpper = -1;
            int upperDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex < stockDataSize; stockDataIndex++){
                upperDistance++;
                if(stockData.get(stockDataIndex) < queryValue){
                    closestIndexUpper = stockDataIndex;
                    break;
                }
            }
            int closestIndexLower = -1;
            int lowerDistance = 0;
            for(int stockDataIndex = queryIndex; stockDataIndex > 0; stockDataIndex--){
                lowerDistance++;
                if(stockData.get(stockDataIndex) < queryValue){
                    closestIndexLower = stockDataIndex;
                    break;
                }
            }
            if(closestIndexLower == -1 && closestIndexUpper == -1){
                answers.add(-1);
            } else if (closestIndexUpper==-1){
                answers.add(closestIndexLower+1);
            } else if (closestIndexLower==-1){
                answers.add(closestIndexUpper+1);
            } else {
                int finalIndex = lowerDistance > upperDistance ? closestIndexUpper : closestIndexLower;
                if(lowerDistance == upperDistance){
                    finalIndex = closestIndexLower+1;
                }
                answers.add(finalIndex);
            }
        }
        return answers;
    }

    public static List<Integer> predictAnswerStreaming(List<Integer> stockData, List<Integer> queries) {
        // Write your code hereList<int> answers = new();
        //use arraylist to get an ordered list
        ArrayList<Integer> answers = new ArrayList<>();
        int stockDataSize = stockData.size();
        for(int query : queries){
            int queryIndex = query-1;
            int queryValue = stockData.get(queryIndex);
            List<Integer> upperStockData = stockData.subList(queryIndex, stockDataSize);
            List<Integer> lowerStockData = stockData.subList(0, queryIndex);
            Collections.reverse(lowerStockData);
            int distanceHigher = IntStream.range(0, upperStockData.size()).filter(i -> upperStockData.get(i)<queryValue).findFirst().orElse(-1);
            int distanceLower = IntStream.range(0, lowerStockData.size()).filter(i -> lowerStockData.get(i)<queryValue).findFirst().orElse(-1);

            int finalIndex = -1;
            if(distanceHigher==-1&&distanceLower==-1) {
            } else if(distanceHigher==-1){
                finalIndex = queryIndex-distanceLower;
            } else if(distanceLower==-1){
                finalIndex = queryIndex+distanceHigher;
            } else if(distanceHigher==distanceLower) {
                finalIndex = queryIndex-distanceLower;
            }
            answers.add(finalIndex);
        }
        return answers;

    }


}