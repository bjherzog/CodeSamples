/*
 * Traveling Salesman GA
 * CS490 - Evoutionary Computation
 * Dr. Moore
 */

package tspgeneticalgorithm;

import java.util.ArrayList;
import java.util.Random;
import java.util.LinkedList;

/**
 *
 * @author Shawn Aldridge & Britny Herzog
 * raskolnikov42@gmail.com, bjherzog@gmail.com
 */
public class Traveler {

    public static final int POPULATION_SIZE = 1000; //global variable for population size
    public static final int NUMBER_OF_CITIES = 100; //global variable for number of cities
    public static final int NUMBER_OF_GENERATIONS = 1000;

    //global variables for probabilistic control parameters, you must tweak these to account for the
    //elite count, as well as ensuring that the full population is reproduced each time
    public static final float PROB_OF_MUTATION = 0.04f;
    public static final float PROB_OF_CROSSOVER = 0.959f;
    public static final float PROB_OF_REPRODUCTION = 0.0f;
    public static final int ELITE_COUNT = 1;
    public static int bestIndex;
    public ArrayList<int[]> population; //global container to hold current generation
    public ArrayList<int[]> selectionPool; //global container to hold members of next generation as they are generated
    static int[][] costGrid; //global container to hold costs for each edge between cities

    /**
     * Generates a grid of costs for traveling between each city in our graph array
     * with a random cost between 99 and 2000
     * Algorithm: intialize 2d array with a length in both directions equal to the number
     * of cities in our problem. For each cell [ij] in array where (i != j) produce a random
     * number between 0 and 1901 and add 99 to it.
     */
    public void seedCostGrid()
    {
        costGrid = new int[NUMBER_OF_CITIES][NUMBER_OF_CITIES];

        Random randomgenerator = new Random();

        for(int i = 0; i < NUMBER_OF_CITIES; i++)
        {
            for(int j = 0; j < NUMBER_OF_CITIES; j++)
            {
                //seed cost if traveling to a different city
                if(i != j)
                {
                    int randomInt = randomgenerator.nextInt(1902);
                    randomInt += 99;
                    costGrid[i][j] = randomInt;
                }
            }
        }
    }//end method


    /**
     * Generates our initial chromosomes and adds them to the global population arraylist.
     * 
     */
    public void seedPopulation()
    {        
        int[] genome = new int[NUMBER_OF_CITIES];
        Random popgen = new Random();
        int randomInt = popgen.nextInt(NUMBER_OF_CITIES);
        genome[0] = randomInt;

        LinkedList values = new LinkedList();
        for(int i = 0; i < NUMBER_OF_CITIES; i++)
        {
            values.add(i);
        }

        values.remove((Integer)randomInt);

        int index = 1;
        while(values.size() > 0)
        {
            randomInt = popgen.nextInt(NUMBER_OF_CITIES);
            if(values.contains((Integer)randomInt))
            {
                genome[index] = randomInt;
                values.remove((Integer)randomInt);
                //System.out.println(values.size());
                index++;
            }
        }

        /* //prints out population as it is generated
        for(int k = 0; k < genome.length; k++)
        {
           System.out.print(genome[k]);
        }
        System.out.println();
         */
         
        population.add(genome);
    }//end method

    /**
     * computes the cost of a given circuit aka fitness function
     * @param circuit array representing candidate solution
     * @return total cost of individual circuit
     */
    public static int costOfCircuit(int[] circuit)
    {
        int cost = 0;
        for(int i = 0; i < NUMBER_OF_CITIES - 1; i++)
        {
            int thisCost = costGrid[circuit[i]][circuit[i+1]];
            cost += thisCost;
        }
        return cost;
    }

    /*
     * Computes fitness values for each genome in the population, puts the genomes through
     * linear normalization, and returns the resulting values in a sequential float array summing to 1.0
     */
    public float[] selectionViaFit()
    {
        int[] fitSelect = new int[POPULATION_SIZE];
        int[] temp;

        //array with cost of each genome in same position as population
        for(int i = 0; i < POPULATION_SIZE; i++)
        {
            temp = population.get(i);
            fitSelect[i] = costOfCircuit(temp);
        }

        int[] linearValues = new int[POPULATION_SIZE]; //array to hold normalized values
        //places the indices of the population into an int array from worst to best
        for(int i = 0; i < linearValues.length; i++)
        {
            int minCost = 0;
            int index = 0;
            for(int j = 0; j < fitSelect.length; j++)
            {
                if(fitSelect[j] > minCost)
                {
                    minCost = fitSelect[j];
                    index = j;
                }
            }
            if(i == linearValues.length - 1) bestIndex = index;
            fitSelect[index] = 0;
            linearValues[index] = i;
        }

        //computes the ranges of probability of selection for each chromosome in the population
        float[] percentages = new float[POPULATION_SIZE];
        float totalFitness = 0.0f;
        float runningTotal = 0.0f;
        for(int k = 0; k < POPULATION_SIZE; k++) totalFitness += k;

        for(int m = 0; m < POPULATION_SIZE; m++)
        {
            percentages[m] = runningTotal + (float)(linearValues[m] / totalFitness);
            runningTotal += (linearValues[m] / totalFitness);
        }
        return percentages;
    }//end method

    /**
     * boolean method to compare two int arrays of same length
     * @param a
     * @param b
     * @return true if the arrays contian the same values at every index
     */
    public boolean same(int[] a, int[] b)
    {
        for(int i = 0; i < a.length; i++)
        {
            if(a[i] != b[i]) return false;
        }
        return true;
    }

    /**
     * Creates selection pool from total population by applying selection probabilties according to 
     * passed in fitness values
     */
    public void selection(float[] fitSelect)
    {
        selectionPool = new ArrayList<int[]>();
        boolean found = false;
        for(int i = 0; i < POPULATION_SIZE; i++)
        {
            found = false;
            double spinner = Math.random();
            int index = 0;
            while(found == false)
            {
                if(spinner < fitSelect[index])
                {
                    int[] candidate = population.get(index);
                    selectionPool.add(candidate);
                    found = true;
                }else index++;
            }//endwhile
        }//endfor
    }//end method selection

    /**
     * Helper method to return the index of a given value in an array
     * @param genome
     * @param value
     * @return throws -1 if value not found
     */
    public int findIndex(int[]genome, int value)
    {
        for(int j = 0; j < genome.length; j++)
        {
            if(genome[j] == value)
            {
                return j;
            }

        }
        return -1;
    }

    /**
     * Performs partially mapped crossover for permutations
     */
    public void PMXcrossover(int[] genome1, int[] genome2)
    {
        //generate crossover points
        int point1 = (int)(Math.random() * NUMBER_OF_CITIES);
        int point2 = point1;
        while(point2 == point1) point2 = (int)(Math.random() * NUMBER_OF_CITIES);

        if(point2 < point1)
        {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        //System.out.println("PMX Point 1: " + point1 + " PMX Point 2: " + point2);
        
        //create child arrays to be placed into selection pool
        int[] child1 = new int[NUMBER_OF_CITIES];
        int[] child2 = new int[NUMBER_OF_CITIES];

        //intialize children to -1 to show holes in crossover
        for(int k = 0; k < child1.length; k++)
        {
            child1[k] = -1;
            child2[k] = -1;
        }

        LinkedList<Integer> transferred = new LinkedList<Integer>();
        LinkedList<Integer> replaced = new LinkedList<Integer>();
        //copy in values from first parent
        for(int i = point1; i < point2; i++)
        {
            child1[i] = genome2[i];
            replaced.add(genome1[i]);
            transferred.add(genome2[i]);

        }//endfor
        
        //iterate through the values that were replaced in genome 1 and find their indices in genome 2
        for(int i = 0; i < transferred.size(); i++)
        {
            int target = replaced.get(i);
            int targetIndex = 0;
            if(!(transferred.contains((Integer)target)))
            {
                //System.out.println(target + " not found in child1");
                boolean found = false;
                targetIndex = findIndex(genome1, target);
                int nextTarget = genome2[targetIndex];
                targetIndex = findIndex(genome1, nextTarget);
                while(!(found))
                {
                    if((targetIndex < point1) || (targetIndex >= point2))
                    {
                        found = true;
                    }else{
                        nextTarget = genome2[targetIndex];
                        targetIndex = findIndex(genome1, nextTarget);
                    }
                }
                child1[targetIndex] = target;
            }
        }
        
        for(int i = 0; i < genome1.length; i++)
        {
            if(child1[i] == -1)
            {
                child1[i] = genome1[i];
            }
        }

        /* //prints out first child generated by PMX
        System.out.println("PMX Child1 = ");
        for(int j = 0; j < child1.length; j++)
        {
            System.out.print(child1[j] + " ");
        }

        System.out.println();
         */

        transferred.clear();
        replaced.clear();

        //copy in values from second parent
        for(int i = point1; i < point2; i++)
        {
            child2[i] = genome1[i];
            replaced.add(genome2[i]);
            transferred.add(genome1[i]);

        }//endfor

        //iterate through the values that were replaced in genome 2 and find their indices in genome 1
        for(int i = 0; i < transferred.size(); i++)
        {
            int target = replaced.get(i);
            int targetIndex = 0;
            if(!(transferred.contains((Integer)target)))
            {
                //System.out.println(target + " not found in child1");
                boolean found = false;
                targetIndex = findIndex(genome2, target);
                int nextTarget = genome1[targetIndex];
                targetIndex = findIndex(genome2, nextTarget);
                while(!(found))
                {
                    if((targetIndex < point1) || (targetIndex >= point2))
                    {
                        found = true;
                    }else{
                        nextTarget = genome1[targetIndex];
                        targetIndex = findIndex(genome2, nextTarget);
                    }
                }
                child2[targetIndex] = target;
            }
        }

        for(int i = 0; i < genome1.length; i++)
        {
            if(child2[i] == -1)
            {
                child2[i] = genome2[i];
            }
        }

        /* //prints out second child generated by PMX
        System.out.println("PMX Child2 = ");
        for(int j = 0; j < child2.length; j++)
        {
            System.out.print(child2[j] + " ");
        }
         */

        population.add(child1);
        population.add(child2);
    }

    /**
     * Performs order crossover and places the two new genomes into the population pool
     * @param genome1
     * @param genome2
     */
    public void OrderCrossover(int[] genome1, int[] genome2)
    {
        //intialize two crossover points
        int point1 = (int)(Math.random() * NUMBER_OF_CITIES);
        int point2 = point1;
        while(point2 == point1) point2 = (int)(Math.random() * NUMBER_OF_CITIES);

        //make sure point 2 is greater than point 1
        if(point2 < point1)
        {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        //System.out.println("Point 1 order: " + point1 + " Point 2 order: " + point2);

        //create child arrays to be placed into selection pool
        int[] child1 = new int[NUMBER_OF_CITIES];
        int[] child2 = new int[NUMBER_OF_CITIES];

        //intialize children to -1 to show holes in crossover
        for(int k = 0; k < child1.length; k++)
        {
            child1[k] = -1;
            child2[k] = -1;
        }

        //copy in values from first parent
        for(int i = point1; i < point2; i++)
        {
            child1[i] = genome1[i];

        }//endfor

        //start the loop at the position directly after our replaced section and continue for
        //as many places as have not been filled in by our initial copying
        int missed = 0;
        boolean done = false;
        int k = point2;
        while(!(done))
        {//^.^happycode

            if(k >= NUMBER_OF_CITIES) k -= NUMBER_OF_CITIES;
            int num = genome2[k]; //capture value in next open slot of second parent
            boolean notInChild = true;
            //check to see if value is already in child
            for(int j = 0; j < NUMBER_OF_CITIES; j++)
            {
                if(child1[j] == num)
                {
                    notInChild = false;
                    missed++;
                }
            }

            if(notInChild)
            {
                int index = k - missed;
                if(index < 0) index += NUMBER_OF_CITIES;
                child1[index] = num;
            }

            done = true;
            for(int n = 0; n < child1.length; n++)
            {
                if(child1[n] == -1)done = false;
            }

            k++;
        }//endfor

        //same code reversed for second child

        //copy in values from second parent
        for(int i = point1; i < point2; i++)
        {
            child2[i] = genome2[i];

        }//endfor

        missed = 0;
        done = false;
        k = point2;
        while(!(done))
        {//^.^happycode

            if(k >= NUMBER_OF_CITIES) k -= NUMBER_OF_CITIES;
            int num = genome1[k]; //capture value in next open slot of second parent
            boolean notInChild = true;
            //check to see if value is already in child
            for(int j = 0; j < NUMBER_OF_CITIES; j++)
            {
                if(child2[j] == num)
                {
                    notInChild = false;
                    missed++;
                }
            }

            if(notInChild)
            {
                int index = k - missed;
                if(index < 0) index += NUMBER_OF_CITIES;
                child2[index] = num;
            }

            done = true;
            for(int n = 0; n < child2.length; n++)
            {
                if(child2[n] == -1)done = false;
            }

            k++;
        }//endfor


        /* //print statements for debugging purposes
        System.out.print("Child 1 : ");
        for(int i = 0; i < NUMBER_OF_CITIES; i++)
        {
            System.out.print(child1[i] + " ");
        }
        System.out.println();

        System.out.print("Child 2 : ");
        for(int i = 0; i < NUMBER_OF_CITIES; i++)
        {
            System.out.print(child2[i] + " ");
        }
        System.out.println();
         */

        population.add(child1);
        population.add(child2);
    }

    /**
     * Performs cycle crossover and places the two new genomes into the population
     * @param genome1
     * @param genome2
     */
    public void cycleCrossover(int[] genome1, int[] genome2)
    {
        //intialize two new children
        int[] child1 = new int[NUMBER_OF_CITIES];
        int[] child2 = new int[NUMBER_OF_CITIES];

        //fill child arrays with -1's
        for(int i = 0; i < child1.length; i++)
        {
            child1[i] = -1;
            child2[i] = -1;
        }

        //arraylist to hold indices of cycle
        ArrayList<Integer> cycle = new ArrayList<Integer>();

        int index = 0;
        while(!(childDone(child1)))
        {
            if(genome2[index] != genome1[index])
            {
                cycle = cycleHelper(genome1, genome2, index);
            }else cycle.add(index);

            //System.out.println("First cycle: ");
            for(int i = 0; i < cycle.size(); i++)
            {
                int cycleNum = cycle.get(i);
                //System.out.println("Cyclenum = " + cycleNum);
                child1[cycleNum] = genome1[cycleNum];
                child2[cycleNum] = genome2[cycleNum];
            }

            cycle.clear();
            if(!(childDone(child1)))
            {
                //I need to find the first number in genome1 that is not in child1, and retrieve its index
                int finder = 1;
                while(findIndex(child1, genome1[finder]) != -1) finder++;

                if(genome2[finder] != genome1[finder])
                {
                    cycle = cycleHelper(genome1, genome2, finder);
                }else cycle.add(finder);

                //System.out.println("Second cycle: ");
                for(int i = 0; i < cycle.size(); i++)
                {
                    int cycleNum = cycle.get(i);
                    //System.out.println("Cyclenum = " + cycleNum);
                    child2[cycleNum] = genome1[cycleNum];
                    child1[cycleNum] = genome2[cycleNum];
                }

                index = 1;
                while(((findIndex(child1, genome1[index]) != -1) && (findIndex(child2, genome1[index]) != -1))
                    && (!(childDone(child1)))) index++;
            }//endif

            cycle.clear();
            }

            /* //print statements for debugging
            System.out.print("Cycle Child 1 : ");
            for(int i = 0; i < NUMBER_OF_CITIES; i++)
            {
                System.out.print(child1[i] + " ");
            }
            System.out.println();

            System.out.print("Cycle Child 2 : ");
            for(int i = 0; i < NUMBER_OF_CITIES; i++)
            {
                System.out.print(child2[i] + " ");
            }
            System.out.println();
             */

        population.add(child1);
        population.add(child2);
    }

    /**
     * Helper method for cycle crossover, returns an arraylist of indices marking out a cycle
     * @param input1
     * @param input2
     * @return
     */
    public ArrayList<Integer> cycleHelper(int[] input1, int[] input2, int inputNum)
    {
        int index = inputNum;
        ArrayList<Integer> output = new ArrayList<Integer>();

        int target = input1[index];
        int current = -1;

        while(current != target)
        {
            //System.out.println("Inside helper");
            current = input1[index];
            output.add(index);
            for(int i = 0; i < input1.length; i++) if(input2[i] == current) index = i;
            current = input1[index];
        }
        return output;
    }

    /**
     * helper method for cycle crossover, returns true if there are no -1's in an array
     * @param child
     * @return
     */
    public boolean childDone(int[] child)
    {
        for(int i = 0; i < child.length; i++)
        {
            if(child[i] == -1) return false;
        }
        return true;
    }

    /**
     * Permutation-specific swap mutation
     */
    public int[] mutation(int[] incomingGenome)
    {
        int[] genome = incomingGenome;
        int point1 = (int)(Math.random() * NUMBER_OF_CITIES);
        int point2 = point1;
        while(point2 == point1) point2 = (int)(Math.random() * NUMBER_OF_CITIES);

        //make sure point 2 is greater than point 1
        if(point2 < point1)
        {
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        int temp = genome[point1];
        genome[point1] = genome[point2];
        genome[point2] = temp;
        return genome;
    }

    /**
     * Returns the index of the candidate in the population with the highest fitness ergo lowest cost
     * @return
     */
    public int findBestFitness(float[] fitness)
    {
        float best = 99999;
        int index = 0;
        for(int i = 0; i < fitness.length; i++)
        {
            if(fitness[i] < best)
            {
                best = fitness[i];
                index = i;
            }
        }
        return index;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

     Traveler traveler = new Traveler();

     traveler.seedCostGrid();
     traveler.population = new ArrayList<int[]>();


     //System.out.println("Current Population: ");
     

     //System.out.println("Initial cost of each circuit: ");
        /*for(int i = 0; i < traveler.population.size(); i++)
        {
        int[] chrome = traveler.population.get(i);
        System.out.print(costOfCircuit(chrome) + " ");
        }*/

     for(int z = 0; z < 3; z++)
     {

         for(int i = 0; i < traveler.POPULATION_SIZE; i++)
         {
            traveler.seedPopulation();
         }

     //main loop to find best genome
     //compute fitness
     //get selection pool
     //perform: crossover, mutation, reproduction, elitecount
     //for elitecount find best X individuals where x is elitecount
     //make sure population is replaced
     //repeat
     for(int i = 0; i < traveler.NUMBER_OF_GENERATIONS; i++)
     {
          float[] fitnessCost = traveler.selectionViaFit(); //compute fitness for population
          int[] best = traveler.population.get(traveler.bestIndex);//fetches best individual in population

          traveler.selection(fitnessCost); //create selection pool from computed fitness values

          /*
          System.out.println("Initial cost of each circuit: ");
          for(int j = 0; j < traveler.population.size(); j++)
          {
          int[] chrome = traveler.population.get(j);
          System.out.print(costOfCircuit(chrome) + " ");
          }
           */
          //System.out.println(" Population Size: " + traveler.population.size());
          //System.out.println("Generation " + (i + 1));

          //need to create new population
          traveler.population.clear();
          traveler.population.add(best);

          //System.out.println("Best fitness is " + costOfCircuit(best));

          int numCrossover = (int)(traveler.POPULATION_SIZE * traveler.PROB_OF_CROSSOVER);
          //System.out.println("Crossover candidates: " + numCrossover);
          for(int j = 0; j < numCrossover; j += 2)
          {
          traveler.cycleCrossover(traveler.selectionPool.get(j), traveler.selectionPool.get(j + 1));
          }

          int numMutate = (int)(traveler.POPULATION_SIZE * traveler.PROB_OF_MUTATION);
          //System.out.println("Mutation candidates: " + numMutate);
          for(int k = numCrossover; k < numMutate + numCrossover; k++)
          {
              int[] temp = traveler.mutation(traveler.selectionPool.get(k));
              traveler.population.add(temp);
          }

          int numRepro = (int)(traveler.POPULATION_SIZE * traveler.PROB_OF_REPRODUCTION);
          //System.out.println("Reproduction candidates: " + numRepro);
          for(int n = numCrossover + numMutate; n < numCrossover + numMutate+ numRepro; n++)
          {
              int[] temp = traveler.selectionPool.get(n);
              traveler.population.add(temp);
          }

          //System.out.println("EliteCount = " + traveler.ELITE_COUNT);

          //System.out.println();

    }//end generational for

     float[] fitnessCost = traveler.selectionViaFit(); //compute fitness for population
     int[] best = traveler.population.get(traveler.bestIndex);//fetches best individual in population
     System.out.println("Best of run = " + costOfCircuit(best));
     for(int k = 0; k < best.length; k++) System.out.print(best[k] + " ");
     System.out.println();

     }//end cycleFor

     /****************************************************************************************************/


     traveler.population.clear();
     for(int z = 0; z < 3; z++)
     {

         for(int i = 0; i < traveler.POPULATION_SIZE; i++)
         {
            traveler.seedPopulation();
         }

     //main loop to find best genome
     //compute fitness
     //get selection pool
     //perform: crossover, mutation, reproduction, elitecount
     //for elitecount find best X individuals where x is elitecount
     //make sure population is replaced
     //repeat
     for(int i = 0; i < traveler.NUMBER_OF_GENERATIONS; i++)
     {
          float[] fitnessCost = traveler.selectionViaFit(); //compute fitness for population
          int[] best = traveler.population.get(traveler.bestIndex);//fetches best individual in population

          traveler.selection(fitnessCost); //create selection pool from computed fitness values

          /*
          System.out.println("Initial cost of each circuit: ");
          for(int j = 0; j < traveler.population.size(); j++)
          {
          int[] chrome = traveler.population.get(j);
          System.out.print(costOfCircuit(chrome) + " ");
          }
           */
          //System.out.println(" Population Size: " + traveler.population.size());
          //System.out.println("Generation " + (i + 1));

          //need to create new population
          traveler.population.clear();
          traveler.population.add(best);

          //System.out.println("Best fitness is " + costOfCircuit(best));

          int numCrossover = (int)(traveler.POPULATION_SIZE * traveler.PROB_OF_CROSSOVER);
          //System.out.println("Crossover candidates: " + numCrossover);
          for(int j = 0; j < numCrossover; j += 2)
          {
          traveler.PMXcrossover(traveler.selectionPool.get(j), traveler.selectionPool.get(j + 1));
          }

          int numMutate = (int)(traveler.POPULATION_SIZE * traveler.PROB_OF_MUTATION);
          //System.out.println("Mutation candidates: " + numMutate);
          for(int k = numCrossover; k < numMutate + numCrossover; k++)
          {
              int[] temp = traveler.mutation(traveler.selectionPool.get(k));
              traveler.population.add(temp);
          }

          int numRepro = (int)(traveler.POPULATION_SIZE * traveler.PROB_OF_REPRODUCTION);
          //System.out.println("Reproduction candidates: " + numRepro);
          for(int n = numCrossover + numMutate; n < numCrossover + numMutate+ numRepro; n++)
          {
              int[] temp = traveler.selectionPool.get(n);
              traveler.population.add(temp);
          }

          //System.out.println("EliteCount = " + traveler.ELITE_COUNT);

          //System.out.println();

    }//end generational for

     float[] fitnessCost = traveler.selectionViaFit(); //compute fitness for population
     int[] best = traveler.population.get(traveler.bestIndex);//fetches best individual in population
     System.out.println("Best of run = " + costOfCircuit(best));
     for(int k = 0; k < best.length; k++) System.out.print(best[k] + " ");
     System.out.println();

     }//end PMXFor


     /************************************************************************************************/


     traveler.population.clear();
     for(int z = 0; z < 3; z++)
     {

         for(int i = 0; i < traveler.POPULATION_SIZE; i++)
         {
            traveler.seedPopulation();
         }

     //main loop to find best genome
     //compute fitness
     //get selection pool
     //perform: crossover, mutation, reproduction, elitecount
     //for elitecount find best X individuals where x is elitecount
     //make sure population is replaced
     //repeat
     for(int i = 0; i < traveler.NUMBER_OF_GENERATIONS; i++)
     {
          float[] fitnessCost = traveler.selectionViaFit(); //compute fitness for population
          int[] best = traveler.population.get(traveler.bestIndex);//fetches best individual in population

          traveler.selection(fitnessCost); //create selection pool from computed fitness values

          /*
          System.out.println("Initial cost of each circuit: ");
          for(int j = 0; j < traveler.population.size(); j++)
          {
          int[] chrome = traveler.population.get(j);
          System.out.print(costOfCircuit(chrome) + " ");
          }
           */
          //System.out.println(" Population Size: " + traveler.population.size());
          //System.out.println("Generation " + (i + 1));

          //need to create new population
          traveler.population.clear();
          traveler.population.add(best);

          //System.out.println("Best fitness is " + costOfCircuit(best));

          int numCrossover = (int)(traveler.POPULATION_SIZE * traveler.PROB_OF_CROSSOVER);
          //System.out.println("Crossover candidates: " + numCrossover);
          for(int j = 0; j < numCrossover; j += 2)
          {
          traveler.OrderCrossover(traveler.selectionPool.get(j), traveler.selectionPool.get(j + 1));
          }

          int numMutate = (int)(traveler.POPULATION_SIZE * traveler.PROB_OF_MUTATION);
          //System.out.println("Mutation candidates: " + numMutate);
          for(int k = numCrossover; k < numMutate + numCrossover; k++)
          {
              int[] temp = traveler.mutation(traveler.selectionPool.get(k));
              traveler.population.add(temp);
          }

          int numRepro = (int)(traveler.POPULATION_SIZE * traveler.PROB_OF_REPRODUCTION);
          //System.out.println("Reproduction candidates: " + numRepro);
          for(int n = numCrossover + numMutate; n < numCrossover + numMutate+ numRepro; n++)
          {
              int[] temp = traveler.selectionPool.get(n);
              traveler.population.add(temp);
          }

          //System.out.println("EliteCount = " + traveler.ELITE_COUNT);

          //System.out.println();

    }//end generational for

     float[] fitnessCost = traveler.selectionViaFit(); //compute fitness for population
     int[] best = traveler.population.get(traveler.bestIndex);//fetches best individual in population
     System.out.println("Best of run = " + costOfCircuit(best));
     for(int k = 0; k < best.length; k++) System.out.print(best[k] + " ");
     System.out.println();

     }//end OrderFor

      

    }//end main method
}// end class
