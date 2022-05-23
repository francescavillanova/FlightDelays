package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	private Graph<Airport,DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer,Airport> idMap;  //in questo caso conterrà tutti gli aeroporti
	
	public Model() {
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap<Integer,Airport>();
		dao.loadAllAirports(idMap);  //riempio la mappa all'inizio del programma 
	}
	
	public void creaGrafo(int x) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);  
		//aggiungere i vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(x, idMap));
		
		//aggiungere gli archi
		for (Rotta r : dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1()) 
					&& this.grafo.containsVertex(r.getA2())) {  //prima di aggiungere l'arco controllo se il grafo contiene i vertici
				DefaultWeightedEdge edge = this.grafo.getEdge(r.getA1(),r.getA2());
				if(edge == null) {  //se non c'è ancora un arco tra i due aeroporti lo aggiungo
					Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getnVoli());
				} else {  //se l'arco c'è già aggiorno il peso
					double pesoVecchio = this.grafo.getEdgeWeight(edge);
					double pesoNuovo = pesoVecchio + r.getnVoli();
					this.grafo.setEdgeWeight(edge, pesoNuovo);
				}
			}
		}
		
	}
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<Airport> getVertici(){
		//meglio se controllo che il grafo sia stato creato 
		List<Airport> vertici = new ArrayList<>(this.grafo.vertexSet());
		Collections.sort(vertici);
		return vertici;
	}
	
	public List<Airport> getPercorso (Airport a1, Airport a2){
		 List<Airport> percorso = new ArrayList<>();
		 	BreadthFirstIterator<Airport,DefaultWeightedEdge> it =
				 new BreadthFirstIterator<>(this.grafo,a1);
		 
		 Boolean trovato = false;  //creo un flag 
		 //visito il grafo
		 while(it.hasNext()) {
			 Airport visitato = it.next();  //it.next() ritorna il nodo che visita
			 if(visitato.equals(a2))
				 trovato = true;
		 }
		 
		 
		 //ottengo il percorso
		 if(trovato) { //cerco il percorso solo se nella componente connessa si trova il vertice di destinazione
			 percorso.add(a2); //inserisco la destinazione
			 Airport step = it.getParent(a2);  //risalgo l'albero di visita andando a prendere il padre della destinazione
			 while (!step.equals(a1)) {  //vado avanti finchè il padre non è la mia parteznza 
				 percorso.add(0,step); //ogni volta che trovo un padre lo aggiungo alla lista in testa 
				 step = it.getParent(step); //continuo a risalire nell'albero 
			 }
			 
			 percorso.add(0,a1); //se lo trovo non entra nel ciclo while quindi devo aggiungerlo
			 return percorso;
		 } else {
			 return null;
		 }
	}
}



















