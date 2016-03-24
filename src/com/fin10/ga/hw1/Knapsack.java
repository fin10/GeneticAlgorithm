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
				s.doRouletteWheelSelection(chromosomes);
				s.doCrossover(chromosomes, CROSSOVER_PROB, CROSSOVER_POINTS);
				s.doMutation(chromosomes, MUTATION_PROB);
				
				System.out.print("#" + (i+1) + " ");
				s.printEvaluation(chromosomes);
			}
			
			System.out.println("\nTournament Selection");
			System.out.print("#0 ");
			s.printEvaluation(chromosomes);
			
			for (int i = 0; i < GENERATION_SIZE; ++i) {
				s.doTournamentSelection(chromosomes);
				s.doCrossover(chromosomes, CROSSOVER_PROB, CROSSOVER_POINTS);
				s.doMutation(chromosomes, MUTATION_PROB);
				
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

	public void doRouletteWheelSelection(List<List<Boolean>> chromosomes) {
		
	}
	
	public void doTournamentSelection(List<List<Boolean>> chromosomes) {
		
	}
	
	public void doCrossover(List<List<Boolean>> chromosomes, float prob, int points) {
		if (mRandom.nextDouble() > prob) return;
		
	}
	
	public void doMutation(List<List<Boolean>> chromosomes, float prob) {
		for (List<Boolean> chromosome : chromosomes) {
			if (mRandom.nextDouble() <= prob) {
				int index = mRandom.nextInt(chromosome.size());
				chromosome.set(index, !chromosome.get(index));
			}
		}
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
		int size = chromosome.size();
		for (int i = 0; i < size; ++i) {
			if (chromosome.get(i)) {
				total += mItems.get(i).profit;
			}
		}
		
		return total;
	}
}
