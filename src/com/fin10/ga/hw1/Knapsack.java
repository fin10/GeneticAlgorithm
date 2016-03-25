package com.fin10.ga.hw1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public final class Knapsack {
	
	private static final int POPULATION_SIZE = 100;
	private static final int GENERATION_SIZE = 100;
	
	private static final int CROSSOVER_POINTS = 3;
	private static final float CROSSOVER_PROB = 0.9f;
	private static final float MUTATION_PROB = 0.01f;
	
	public static final class Item {
		private final int weight;
		private final int profit;
		
		private Item(int weight, int profit) {
			this.weight = weight;
			this.profit = profit;
		}
	}
	
	private final int mCapacity;
	private final List<Item> mItems;
	private final Random mRandom = new Random();
	
	public static void main(String[] args) {
		try {
			Knapsack s = new Knapsack("hw1.txt");
			
			// the roulette wheel selection
			int bestOfBest = 0;
			System.out.println("Roulette Wheel Selection");
			System.out.print("#0 ");
			List<List<Boolean>> chromosomes = s.generatePopulation(POPULATION_SIZE);
			s.printEvaluation(chromosomes);
			
			for (int i = 0; i < GENERATION_SIZE; ++i) {
				chromosomes = s.doRouletteWheelSelection(chromosomes);
				s.doCrossover(chromosomes, CROSSOVER_PROB, CROSSOVER_POINTS);
				s.doMutation(chromosomes, MUTATION_PROB);
				
				System.out.print("#" + (i+1) + " ");
				int best = s.printEvaluation(chromosomes);
				if (best > bestOfBest) bestOfBest = best;
			}

			System.out.println("the best of best:" + bestOfBest);
			
			// the tournament selection
			bestOfBest = 0;
			System.out.println("\nTournament Selection");
			System.out.print("#0 ");
			chromosomes = s.generatePopulation(POPULATION_SIZE);
			s.printEvaluation(chromosomes);
			
			for (int i = 0; i < GENERATION_SIZE; ++i) {
				chromosomes = s.doTournamentSelection(chromosomes);
				s.doCrossover(chromosomes, CROSSOVER_PROB, CROSSOVER_POINTS);
				s.doMutation(chromosomes, MUTATION_PROB);
				
				System.out.print("#" + (i+1) + " ");
				int best = s.printEvaluation(chromosomes);
				if (best > bestOfBest) bestOfBest = best;
			}
			
			System.out.println("the best of best:" + bestOfBest);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Knapsack(String fileName) throws FileNotFoundException {
		FileInputStream in = null;
		Scanner sc = null;
		
		try {
			in = new FileInputStream(fileName);
			sc = new Scanner(in);
			mCapacity = sc.nextInt();
			
			List<Item> items = new ArrayList<>();
			while (sc.hasNext()) {
				int index = sc.nextInt();
				int weight = sc.nextInt();
				int profit = sc.nextInt();
				items.add(new Item(weight, profit));
			}
			
			mItems = Collections.unmodifiableList(items);
			
		} finally {
			try {
				if (in != null) in.close();
				if (sc != null) sc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// it generates chromosomes with random values.
	public List<List<Boolean>> generatePopulation(int size) {
		List<List<Boolean>> chromosomes = new ArrayList<>(size);
		while (chromosomes.size() < size) {
			List<Boolean> chromosome = new ArrayList<>(mItems.size());
			for (int i = 0; i < mItems.size(); ++i) {
				chromosome.add(mRandom.nextBoolean());
			}
			
			chromosomes.add(chromosome);
		}
		
		return chromosomes;
	}

	// the roulette wheel selection
	// it returns new chromosome list selected by the roulette wheel selection.
	public List<List<Boolean>> doRouletteWheelSelection(List<List<Boolean>> chromosomes) {
		int size = chromosomes.size();
		List<Integer> scores = new ArrayList<>(size);
		for (List<Boolean> chromosome : chromosomes) {
			scores.add(getTotalProfit(chromosome));
		}
		
		long total = 0;
		for (int score : scores) {
			total += score;
		}

		List<List<Boolean>> offsprings = new ArrayList<>(size);
		while (offsprings.size() < size) {
			long sum = 0;
			long point = total <= 0 ? 0 : Math.abs(mRandom.nextLong()) % total;
			for (int i = 0; i < size; ++i) {
				sum += scores.get(i);
				if (point <= sum) {
					offsprings.add(new ArrayList<>(chromosomes.get(i)));
					break;
				}
			}
		}
		
		return offsprings;
	}

	// the tournament selection
	// it returns new chromosome list selected by the tournament selection.
	public List<List<Boolean>> doTournamentSelection(List<List<Boolean>> chromosomes) {
		int size = chromosomes.size();
		List<List<Boolean>> offsprings = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			List<Boolean> opponent = chromosomes.get(mRandom.nextInt(size));
			if (getTotalProfit(chromosomes.get(i)) < getTotalProfit(opponent)) {
				offsprings.add(new ArrayList<>(opponent));
			} else {
				offsprings.add(new ArrayList<>(chromosomes.get(i)));
			}
		}
		
		return offsprings;
	}
	
	// the crossover will be occurred in the prob probability.	
	public  void doCrossover(List<List<Boolean>> chromosomes, float prob, int points) {
		Collections.shuffle(chromosomes, mRandom);
		
		int size = chromosomes.size();
		for (int i = 0; i < size/2; ++i) {
			if (mRandom.nextFloat() <= prob) {
				int startIdx = 1;
				List<Boolean> mom = chromosomes.get(i);
				List<Boolean> papa = chromosomes.get(i + size/2);
				if (mom.equals(papa)) continue;

				while (points > 0) {
					// calculates points to split chromosome.
					startIdx += mRandom.nextInt(mom.size() - startIdx - points);
					List<Boolean> tmp = new ArrayList<>(mom);
					for (int j = startIdx; j < mom.size(); ++j) {
						mom.set(j, papa.get(j));
						papa.set(j, tmp.get(j));
					}
					
					--points;
				}
			}
		}
	}
	
	// the mutation will be occurred in the prob probability.
	public void doMutation(List<List<Boolean>> chromosomes, float prob) {
		for (List<Boolean> chromosome : chromosomes) {
			for (int i = 0; i < chromosome.size(); ++i) {
				if (mRandom.nextFloat() <= prob) {
					// inverts value.
					chromosome.set(i, !chromosome.get(i));
				}
			}
		}
	}
	
	// it prints the average of profits and the best of profit in chromosomes.
	// and it returns the best of profit.
	public int printEvaluation(List<List<Boolean>> chromosomes) {
		int best = 0;
		long total = 0;
		for (List<Boolean> chromosome : chromosomes) {
			int weight = getTotalWeight(chromosome);
			if (weight <= mCapacity) {
				int profit = getTotalProfit(chromosome); 
				total += profit;
				
				if (best < profit) best = profit;
			}
		}
		
		System.out.println("avg:" + (float) total / chromosomes.size() + ", best:" + best);
		return best;
	}
	
	private int getTotalWeight(List<Boolean> chromosome) {
		int total = 0;
		int size = chromosome.size();
		for (int i = 0; i < size; ++i) {
			if (chromosome.get(i)) {
				total += mItems.get(i).weight;
			}
		}
		
		return total;
	}

	private int getTotalProfit(List<Boolean> chromosome) {
		int total = 0;
		int weight = 0;
		int size = chromosome.size();
		for (int i = 0; i < size; ++i) {
			if (chromosome.get(i)) {
				total += mItems.get(i).profit;
				weight += mItems.get(i).weight;
				
				if (weight > mCapacity) return 0;
			}
		}
		
		return total;
	}
}
