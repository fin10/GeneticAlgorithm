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
			List<List<Boolean>> chromosomes = s.generatePopulation(POPULATION_SIZE);

			System.out.println("Roulette Wheel Selection");
			System.out.print("#0 ");
			s.printEvaluation(chromosomes);
			
			for (int i = 0; i < GENERATION_SIZE; ++i) {
				List<Boolean> mom = s.doRouletteWheelSelection(chromosomes);
				List<Boolean> papa = s.doRouletteWheelSelection(chromosomes);
				List<Boolean> offspring = s.doCrossover(mom, papa, CROSSOVER_PROB, CROSSOVER_POINTS);
				s.doMutation(offspring, MUTATION_PROB);
				s.replace(chromosomes, offspring);
				
				System.out.print("#" + (i+1) + " ");
				s.printEvaluation(chromosomes);
			}
			
			System.out.println("\nTournament Selection");
			System.out.print("#0 ");
			s.printEvaluation(chromosomes);
			
			for (int i = 0; i < GENERATION_SIZE; ++i) {
				List<Boolean> mom = s.doTournamentSelection(chromosomes);
				List<Boolean> papa = s.doTournamentSelection(chromosomes);
				List<Boolean> offspring = s.doCrossover(mom, papa, CROSSOVER_PROB, CROSSOVER_POINTS);
				s.doMutation(offspring, MUTATION_PROB);
				s.replace(chromosomes, offspring);
				
				System.out.print("#" + (i+1) + " ");
				s.printEvaluation(chromosomes);
			}
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
			List<Item> items = new ArrayList<>();
			mCapacity = sc.nextInt();
			
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
	
	public List<List<Boolean>> generatePopulation(int size) {
		List<List<Boolean>> chromosomes = new ArrayList<>(size);
		long max = (long) Math.pow(2, mItems.size());
		
		while (chromosomes.size() < size && chromosomes.size() < max) {
			List<Boolean> chromosome = new ArrayList<>(mItems.size());
			for (int i = 0; i < mItems.size(); ++i) {
				chromosome.add(mRandom.nextBoolean());
			}
			
			if (!chromosomes.contains(chromosome)) {
				chromosomes.add(chromosome);
			}
		}
		
		return chromosomes;
	}

	public List<Boolean> doRouletteWheelSelection(List<List<Boolean>> chromosomes) {
		List<Integer> scores = new ArrayList<>(chromosomes.size());
		int worst = getTotalProfit(getWorstChromosome(chromosomes));
		int best = getTotalProfit(getBestChromosome(chromosomes));
		int pressure = (int) ((best - worst) / 4.f);

		for (List<Boolean> chromosome : chromosomes) {
			int score = (getTotalProfit(chromosome) - worst) + pressure;
			scores.add(score);
		}
		
		int total = 0;
		for (int score : scores) {
			total += score;
		}
		
		int point = mRandom.nextInt(total);
		int sum = 0;
		int i = 0;
		for (int score : scores) {
			sum += score;
			if (point < sum) return chromosomes.get(i);
			++i;
		}
		
		return chromosomes.get(chromosomes.size() - 1);
	}
	
	public List<Boolean> doTournamentSelection(List<List<Boolean>> chromosomes) {
		return Collections.emptyList();
	}
	
	public List<Boolean> doCrossover(List<Boolean> mom, List<Boolean> papa, float prob, int points) {
		if (mRandom.nextFloat() <= prob) {
			return Collections.emptyList();
		} else {
			return new ArrayList<>(getTotalProfit(mom) < getTotalProfit(papa) ? papa : mom);
		}
	}
	
	public void doMutation(List<Boolean> chromosome, float prob) {
		if (chromosome.isEmpty()) return;
		
		if (mRandom.nextDouble() <= prob) {
			int index = mRandom.nextInt(chromosome.size());
			chromosome.set(index, !chromosome.get(index));
		}
	}
	
	public void replace(List<List<Boolean>> chromosomes, List<Boolean> offspring) {
		List<Boolean> worst = getWorstChromosome(chromosomes);
		Collections.copy(worst, offspring);
	}
	
	public void printEvaluation(List<List<Boolean>> chromosomes) {
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
		
		System.out.println("avg:" + (double) total / chromosomes.size() + ", best: " + best);
	}
	
	private int getTotalWeight(List<Boolean> chromosome) {
		if (mItems.size() != chromosome.size()) return 0;
		
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
		if (mItems.size() != chromosome.size()) return 0;
		
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

	private List<Boolean> getBestChromosome(List<List<Boolean>> chromosomes) {
		if (chromosomes.isEmpty()) return Collections.emptyList();
		
		int best = 0;
		int bestIdx = chromosomes.size()-1;
		
		int i = 0;
		for (List<Boolean> chromosome : chromosomes) {
			int weight = getTotalWeight(chromosome);
			if (weight <= mCapacity) {
				int profit = getTotalProfit(chromosome); 
				if (best < profit) {
					best = profit;
					bestIdx = i;
				}
			}
			
			++i;
		}
		
		return chromosomes.get(bestIdx);
	}
	
	private List<Boolean> getWorstChromosome(List<List<Boolean>> chromosomes) {
		if (chromosomes.isEmpty()) return Collections.emptyList();
		
		int worst = Integer.MAX_VALUE;
		int worstIdx = chromosomes.size()-1;
		
		int i = 0;
		for (List<Boolean> chromosome : chromosomes) {
			int weight = getTotalWeight(chromosome);
			if (weight > mCapacity) {
				return chromosome;
			}
			
			int profit = getTotalProfit(chromosome); 
			if (worst > profit) {
				worst = profit;
				worstIdx = i;
			}
			
			++i;
		}
		
		return chromosomes.get(worstIdx);
	}
}
