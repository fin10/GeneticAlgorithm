package com.fin10.ga.hwfinal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public final class AllergyProblem {

	private static final int POPULATION_SIZE = 500;
	private static final int GENERATION_SIZE = 75;
	
	private static final int CROSSOVER_POINTS = 1;
	private static final float CROSSOVER_PROB = 1.f;
	private static final float MUTATION_PROB = 0.2f;
	private static final float TAU = 0.5f;
	
	private final Random mRandom = new Random();
	private final List<Person> people;
	private final int foods;
	
	public static final class Individual {
		
		private boolean[] values;
		private int fitness = 0;
		
		private Individual(int length) {
			values = new boolean[length];
		}
		
		public static Individual createInstance(Random random, int length) {
			Individual individual = new Individual(length);
			for (int i = 0; i < length; ++i) {
				individual.values[i] = random.nextBoolean();
			}
			
			return individual;
		}
	
		public void calculateFitness(List<Person> people) {
			int count = 0;
			boolean[] checkTable = new boolean[people.size()];
			Arrays.fill(checkTable, false);

			for (int i = 0; i < values.length; ++i) {
				if (values[i]) {
					++count;
					for (int j = 0 ; j < people.size(); ++j) {
						if (!checkTable[j] && people.get(j).availableFoods[i]) {
							checkTable[j] = true;
						}
					}
				}
			}

			if (count == 0) {
				fitness = values.length+1;
				return;
			}
			
			for (boolean check : checkTable) {
				if (!check) {
					fitness = values.length+1;
					return;
				}
			}
			
			fitness = count;
		}
		
		public Individual duplicate() {
			Individual individual = new Individual(values.length);
			for (int i = 0; i < values.length; ++i) {
				individual.values[i] = values[i];
			}
			individual.fitness = fitness;
						
			return individual;
		}
	}
	
	public static final class Person {
		
		private final String name;
		private final boolean availableFoods[];
		
		public Person(String name, int foods) {
			this.name = name;
			this.availableFoods = new boolean[foods];
			Arrays.fill(availableFoods, false);
		}

		public void setFood(int index, boolean possible) {
			this.availableFoods[index] = possible;
		}

		@Override
		public String toString() {
			return "Person [name=" + name + ", availableFoods=" + Arrays.toString(availableFoods) + "]";
		}
	}

	private AllergyProblem(String[] names, int foods) {
		this.foods = foods;
		this.people = new ArrayList<>(names.length);
		for (String name : names) {
			this.people.add(new Person(name, foods));
		}
	}
	
	private Person getPerson(String name) {
		for (Person p : people) {
			if (p.name.equals(name)) {
				return p;
			}
		}
		
		return null;
	}
	
	private int getSolution() {
		List<Individual> population = generatePopulation(POPULATION_SIZE);
		
		int best = foods;
		for (int i = 0; i < GENERATION_SIZE; ++i) {
			List<Individual> reserved = splitPopulation(population, TAU);
			doCrossover(population, CROSSOVER_PROB, CROSSOVER_POINTS);
			doMutation(population, MUTATION_PROB);

			population = doTournamentReplacement(reserved, population);
			
			for (Individual individual : population) {
				individual.calculateFitness(people);
			}
			
			System.out.print("#" + i + " ");
			printEvaluation(population);
			
			for (Individual individual : population) {
				if (best > individual.fitness) best = individual.fitness;
			}
			
			if (checkConvergence(population) > 0.55f) {
				break;
			}
		}
		
		return best;
	}

	public float checkConvergence(List<Individual> population) {
		int length = population.get(0).values.length;
		float half = population.size()/2.f;

		float total = 0;
		for (int i = 0; i < length; ++i) {
			int count = 0;
			for (Individual individual : population) {
				if (individual.values[i]) ++count;
			}
			
			if (count < half) count = population.size() - count;
			total += count;
		}
		
		return (total / population.size()) / length;
	}
	
	public void printEvaluation(List<Individual> population) {
		int best = Integer.MAX_VALUE;
		long total = 0;
		for (Individual individual : population) {
			total += individual.fitness;
			if (best > individual.fitness) best = individual.fitness;
		}
		
		System.out.println("avg:" + (float) total / population.size() + ", best:" + best +
				", convergence:" + checkConvergence(population));
	}
	
	
	public List<Individual> generatePopulation(int size) {
		List<Individual> population = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			population.add(Individual.createInstance(mRandom, foods));
		}
		
		for (Individual individual : population) {
			individual.calculateFitness(people);
		}
		
		return population;
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
	
	public List<Individual> doTournamentReplacement(List<Individual> reserved, List<Individual> population) {
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
		
		return individuals;
	}
	
	public List<Individual> doTournamentSelection(List<Individual> population) {
		int size = population.size();
		List<Individual> offsprings = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			Individual opponent = population.get(mRandom.nextInt(size));
			if (population.get(i).fitness > opponent.fitness) {
				offsprings.add(opponent.duplicate());
			} else {
				offsprings.add(population.get(i).duplicate());
			}
		}
		
		return offsprings;
	}
	
	public void doCrossover(List<Individual> population, float prob, int points) {
		Collections.shuffle(population, mRandom);
		
		int size = population.size();
		for (int i = 0; i < size/2; ++i) {
			if (mRandom.nextFloat() <= prob) {
				int startIdx = 1;
				Individual mom = population.get(i);
				Individual papa = population.get(i + size/2);

				for (int j = 0; j < points; ++j) {
					startIdx += mRandom.nextInt(mom.values.length - startIdx - points);
					Individual tmp = mom.duplicate();
					for (int k = startIdx; k < mom.values.length; ++k) {
						mom.values[k] = papa.values[k];
						papa.values[k] = tmp.values[k];
					}
				}
			}
		}
	}
	
	public void doMutation(List<Individual> population, float prob) {
		for (Individual individual : population) {
			for (int i = 0; i < individual.values.length; ++i) {
				if (mRandom.nextFloat() <= prob) {
					individual.values[i] = !individual.values[i];
				}
			}
		}
	}
	
	public static void main(String[] args) {
		FileInputStream in = null;
		Scanner sc = null;

		try {
			in = new FileInputStream("hw_final.txt");
			sc = new Scanner(in);
//			sc = new Scanner(System.in);
			
			int T = sc.nextInt();
			
			for (int i = 0; i < T; ++i) {
				int persons = sc.nextInt();
				int foods = sc.nextInt();
				
				String[] names = new String[persons];
				for (int j = 0; j < persons; ++j) {
					names[j] = sc.next();
				}
				
				AllergyProblem problem = new AllergyProblem(names, foods);
				for (int j = 0; j < foods; ++j) {
					int c = sc.nextInt();
					for (int k = 0; k < c; ++k) {
						Person person = problem.getPerson(sc.next());
						person.setFood(j, true);
					}
				}
				
				int s = problem.getSolution();
				System.out.println(s);
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) in.close();
				if (sc != null) sc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
