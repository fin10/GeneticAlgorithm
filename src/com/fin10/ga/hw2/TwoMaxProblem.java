package com.fin10.ga.hw2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class TwoMaxProblem {

	private static final int POPULATION_SIZE = 100;
	private static final int GENERATION_COUNT = 300;
	
	private static final int CROSSOVER_POINTS = 1;
	private static final float CROSSOVER_PROB = 1.f;
	private static final float MUTATION_PROB = 0.01f;
	private static final float TAU = 0.5f;

	private final Random mRandom = new Random();

	public static final class Individual {
		
		private static final int LENGTH = 50;
		private static final float D = 30.f;
		
		private final int[] values = new int[LENGTH];
		private float fitness = 0;
		private float rawFitness = 0;

		private Individual() {
		}

		public static Individual createInstance(Random random) {
			Individual individual = new Individual();
			for (int i = 0; i < individual.values.length; ++i) {
				individual.values[i] = random.nextInt(2);
			}
			
			return individual;
		}
		
		public Individual duplicate() {
			Individual individual = new Individual();
			for (int i = 0; i < values.length; ++i) {
				individual.values[i] = values[i];
			}
			individual.fitness = fitness;
						
			return individual;
		}
		
		public void calculateFitness(List<Individual> population) {
			int sumOfOne = 0, sumOfZero = 0;
			for (int value : values) {
				if (value == 1) ++sumOfOne;
				else if (value == 0) ++ sumOfZero;
			}
			
			rawFitness = Math.max(sumOfOne, sumOfZero);
			float m = sharingMethod(population);
			fitness = m > 0.f ? rawFitness / m : rawFitness;
		}

		public void invert(int index) {
			values[index] = values[index] == 1 ? 0 : 1;
		}
		
		public static int size() {
			return LENGTH;
		}

		@Override
		public String toString() {
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("'");
			for (int value : values) {
				strBuilder.append(value);
			}
			strBuilder.append("' fitness: ");
			strBuilder.append(rawFitness);
			strBuilder.append(" modified fitness: ");
			strBuilder.append(fitness);
			
			return strBuilder.toString();
		}
		
		private float sharingMethod(List<Individual> population) {
			float sum = 0.f;
			for (Individual individual : population) {
				if (this == individual) continue;
				
				int distance = getHammingDistance(this.values, individual.values);
				if (distance < D) sum += 1.f - distance/D;
			}
			
			return sum;
		}
		
		private static int getHammingDistance(int[] value1, int[] value2) {
			int distance = 0;
			for (int i = 0; i < value1.length; ++i) {
				if (value1[i] != value2[i]) {
					++distance;
				}
			}
			
			return distance;
		}
	}
	
	public static void main(String[] args) {
		TwoMaxProblem s = new TwoMaxProblem();
		
		List<Individual> population = s.generatePopulation(POPULATION_SIZE);
		
		System.out.print(String.format("#%03d ", 0));
		printSummaryOfPopulation(population);
		
		for (int i = 0; i < GENERATION_COUNT; ++i) {
			List<Individual> reserved = splitPopulation(population, TAU);
			s.doCrossover(population, CROSSOVER_PROB, CROSSOVER_POINTS);
			s.doMutation(population, MUTATION_PROB);

			population = s.doTournamentReplacement(reserved, population);
			
			System.out.print(String.format("#%03d ", i+1));
			printSummaryOfPopulation(population);
		}
		
		printBestOfIndividuals(population, 10);
	}

	private static List<Individual> splitPopulation(List<Individual> population, float tau) {
		Collections.sort(population, new Comparator<Individual>() {

			@Override
			public int compare(Individual o1, Individual o2) {
				return Float.compare(o1.fitness, o2.fitness);
			}
		});
		Collections.reverse(population);
		
		int count = (int) (population.size() * tau);
		List<Individual> result = new ArrayList<>(count);
		for (int i = 0; i < count; ++i) {
			result.add(population.get(i).duplicate());
		}
		
		return result;
	}

	// it generates population with random values.
	private List<Individual> generatePopulation(int size) {
		List<Individual> population = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			population.add(Individual.createInstance(mRandom));
		}

		for (Individual individual : population) {
			individual.calculateFitness(population);
		}
		
		return population;
	}
	
	private List<Individual> doTournamentReplacement(List<Individual> reserved, List<Individual> population) {
		List<Individual> individuals = new ArrayList<>(reserved);
		int count = population.size() - reserved.size();
		for (int i = 0; i < count; ++i) {
			Individual individual = population.get(mRandom.nextInt(population.size()));
			if (population.get(i).fitness < individual.fitness) {
				individuals.add(individual.duplicate());
			} else {
				individuals.add(population.get(i).duplicate());
			}
		}
		
		for (Individual individual : individuals) {
			individual.calculateFitness(individuals);
		}
		
		return individuals;
	}
	
	// the crossover will be occurred in the prob probability.	
	private void doCrossover(List<Individual> population, float prob, int points) {
		Collections.shuffle(population, mRandom);
		
		int size = population.size();
		for (int i = 0; i < size/2; ++i) {
			if (mRandom.nextFloat() <= prob) {
				int startIdx = 1;
				Individual mom = population.get(i);
				Individual papa = population.get(i + size/2);
				if (mom.equals(papa)) continue;

				while (points > 0) {
					// calculates points to split individual.
					startIdx += mRandom.nextInt(Individual.size() - startIdx - points);
					for (int j = startIdx; j < Individual.size(); ++j) {
						int tmp = mom.values[j];
						mom.values[j] = papa.values[j];
						papa.values[j] = tmp;
					}
					
					--points;
				}
			}
		}
		
		for (Individual individual : population) {
			individual.calculateFitness(population);
		}
	}

	// the mutation will be occurred in the prob probability.
	private void doMutation(List<Individual> population, float prob) {
		for (Individual individual : population) {
			for (int i = 0; i < Individual.size(); ++i) {
				if (mRandom.nextFloat() <= prob) {
					// inverts value.
					individual.invert(i);
				}
			}
		}
		
		for (Individual individual : population) {
			individual.calculateFitness(population);
		}
	}

	private static void printSummaryOfPopulation(List<Individual> population) {
		float avg = 0.f;
		float best = 0;
		
		for (Individual individual : population) {
			if (best < individual.rawFitness) best = individual.rawFitness;
			avg += individual.rawFitness;
		}
		
		avg /= population.size();
		
		System.out.println(String.format("avg: %.2f, best: %.2f", avg, best));
	}
	
	private static void printBestOfIndividuals(List<Individual> population, int count) {
		Collections.sort(population, new Comparator<Individual>() {

			@Override
			public int compare(Individual o1, Individual o2) {
				return Float.compare(o1.rawFitness, o2.rawFitness);
			}
		});
		Collections.reverse(population);

		List<Individual> result = population.subList(0, count);
		for (int i = 0; i < result.size() && i < count; ++i) {
			System.out.println(String.format("#%03d %s", i+1, result.get(i)));
		}
	}
}
