package it.polito.tdp.formulaone.model;

import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.formulaone.db.FormulaOneDAO;

public class Model {
	
	private List<Season> season;
	//grafo semplice perchè tra due piloti c'è solo un arco che ha come peso le gare in cui ha battuto il pilota
	private SimpleDirectedWeightedGraph<Driver, DefaultWeightedEdge> graph;
	
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


}
