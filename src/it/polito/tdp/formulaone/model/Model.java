package it.polito.tdp.formulaone.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.formulaone.db.FormulaOneDAO;

public class Model {
	
	private List<Season> season;
	//grafo semplice perchè tra due piloti c'è solo un arco che ha come peso le gare in cui ha battuto il pilota
	private SimpleDirectedWeightedGraph<Driver, DefaultWeightedEdge> graph;
	
	//variabili x ricorsione
	private int tassoMin;
	private List<Driver> teamMin;
	
	public List<Season> getSeason(){
		if(this.season == null){
			FormulaOneDAO dao = new FormulaOneDAO();
			season = dao.getAllSeasons();
		}
		return season;
	}
	
	
	public void creaGrafo(Season s){
		
		this.graph = new SimpleDirectedWeightedGraph<Driver, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		FormulaOneDAO dao = new FormulaOneDAO();
		List<Driver> drivers = dao.getDriversForSeason(s);
		//System.out.println(drivers.size());
		Graphs.addAllVertices(this.graph, drivers);
		//System.out.println(graph);
		for(Driver d1: this.graph.vertexSet()){
			for(Driver d2: this.graph.vertexSet()){
				if(!d1.equals(d2)){
					Integer vittorie = dao.contaVittorie(d1, d2, s);
					if(vittorie>0){
						Graphs.addEdgeWithVertices(graph, d1, d2, vittorie);
					}
				}
			}
		}
		
		//System.out.println(graph);
	}
	
	
	public Driver getBestDriver(){
		
		Driver best = null;
		int max = Integer.MIN_VALUE;
		
		for(Driver d : this.graph.vertexSet()){
			int peso =0;
			for(DefaultWeightedEdge arco : graph.outgoingEdgesOf(d)){
				peso += graph.getEdgeWeight(arco);
			}
			for(DefaultWeightedEdge arco : graph.incomingEdgesOf(d)){
				peso -= graph.getEdgeWeight(arco);
			}
			
			if(peso>max){
				max = peso;
				best = d;
			}
			
		}
		return best;
	}

	
	
	
	public List<Driver> getDreamTeam(int k){
		
		Set<Driver> team = new HashSet<>();
		this.tassoMin = Integer.MAX_VALUE;
		this.teamMin = null;
		ricorsiva(0, team, k);
		
		return this.teamMin;
	}
	/**
	 * In ingresso ricevo il team parziale di passo elementi
	 * la variabile passo parte da 0.
	 * Il caso terminale è quando passo = k ed in quel caso va calcolato il tasso di sconfitta.
	 * Altrimenti si procede ricorsivamente ad aggiungere un nuovo vertice( passo +1), scegliendolo 
	 * tra i vertici non ancora presenti nel team
	 * 
	 * @param passo
	 * @param team
	 * @param k
	 */
	private void ricorsiva(int passo, Set<Driver> team, int k){
		
		//CONDIZIONE DI TERMINAZIONE
		if(passo==k){
			
			//calcolare tasso di sconfitta del team, 
			int tasso = this.tassoSconfitta(team);
			//eventualmente aggornare il minimo
			if(tasso< tassoMin){
				tassoMin = tasso;
				//devo per forza creare una nuova lista!!!
				teamMin = new ArrayList<>(team);
				
			//	System.out.println(tassoMin +" "+ team.toString());
			}
		}else{
			
			//caso normale
			Set<Driver> candidati = new HashSet<>(graph.vertexSet());
			candidati.removeAll(team);
			
			for(Driver d : candidati){
				team.add(d);
				
				ricorsiva(passo+1, team, k);
				
				team.remove(d);
			}
			
		}
		
		
		
	}


	private int tassoSconfitta(Set<Driver> team) {
		
		int tasso = 0;
		
		for(DefaultWeightedEdge arco : graph.edgeSet()){
			if(!team.contains(graph.getEdgeSource(arco)) && team.contains(graph.getEdgeTarget(arco))){
				tasso += graph.getEdgeWeight(arco);
			}
		}
		
		return tasso;
	}

}
