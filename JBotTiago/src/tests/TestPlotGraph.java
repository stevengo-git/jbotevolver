package tests;

import java.awt.BorderLayout;

import gui.util.Graph;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestPlotGraph {

	private JFrame window;
	private JPanel graphPanel;
	private Graph fitnessGraph;
	
	public TestPlotGraph() {
		window = new JFrame("Test Graph Plotter");
		
		fitnessGraph = new Graph();
		fitnessGraph.setxLabel("Generations");
		fitnessGraph.setyLabel("Fitness");
		
		graphPanel = new JPanel(new BorderLayout());
		graphPanel.setBorder(BorderFactory.createTitledBorder("Fitness Graph"));
		graphPanel.add(fitnessGraph, BorderLayout.CENTER);
		
		fitnessGraph.setShowLast(3);
		
		fitnessGraph.addData(1.0);
		fitnessGraph.addData(-1.0);
		fitnessGraph.addData(1.0);
		
		window.add(graphPanel, BorderLayout.CENTER);
		
		window.setSize(800, 600);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
	
	
	public static void main(String[] args) {
		new TestPlotGraph();
	}
	
}
