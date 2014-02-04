package springies;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jboxGlue.PhysicalObject;
import jboxGlue.PhysicalObjectRect;
import jboxGlue.WorldManager;
import jgame.JGColor;
import jgame.platform.JGEngine;
import nodes.Fixed;
import nodes.Mass;
import nodes.SuperMass;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import forces.CenterOfMass;
import forces.Force;
import forces.Gravity;
import forces.Muscle;
import forces.Spring;
import forces.Viscosity;


@SuppressWarnings("serial")
public class Springies extends JGEngine
{
	static ArrayList<Force> force = new ArrayList<Force>();
	static ArrayList<SuperMass> allSuperMasses = new ArrayList<SuperMass>();

	public Springies ()
	{
		// set the window size
		int height = 700;
		double aspect = 16.0 / 9.0;
		initEngineComponent((int) (height * aspect), height);
	}

	@Override
	public void initCanvas ()
	{
		// I have no idea what tiles do...
		setCanvasSettings(1, // width of the canvas in tiles
				1, // height of the canvas in tiles
				displayWidth(), // width of one tile
				displayHeight(), // height of one tile
				null,// foreground colour -> use default colour white
				null,// background colour -> use default colour black
				null); // standard font -> use default font
	}

	@Override
	public void initGame ()
	{
		setFrameRate(60, 2);
		// NOTE:
		//   world coordinates have y pointing down
		//   game coordinates have y pointing up
		// so gravity is up in world coords and down in game coords
		// so set all directions (e.g., forces, velocities) in world coords
		WorldManager.initWorld(this);
		addWalls();
		parseXML();
	}

	@Override
	public void doFrame ()
	{
		// update game objects
		WorldManager.getWorld().step(1f, 1);

		for(Force f: force){
			f.calculateForce();
		}
		if(getLastKey() == 78){
			clearLastKey();
			parseXML();
		}
		if(getLastKey() == 67){
			clearLastKey();
			clearAllTheDamnAssemblies();
		}

		moveObjects();
		checkCollision(1 + 2, 1);
	}

	@Override
	public void paintFrame ()
	{
		// nothing to do
		// the objects paint themselves
	}

	private void addWalls ()
	{
		// add walls to bounce off of
		// NOTE: immovable objects must have no mass
		final double WALL_MARGIN = 10;
		final double WALL_THICKNESS = 10;
		final double WALL_WIDTH = displayWidth() - WALL_MARGIN * 2 + WALL_THICKNESS;
		final double WALL_HEIGHT = displayHeight() - WALL_MARGIN * 2 + WALL_THICKNESS;
		PhysicalObject wall = new PhysicalObjectRect("wall", 2, JGColor.green,
				WALL_WIDTH, WALL_THICKNESS);
		wall.setPos(displayWidth() / 2, WALL_MARGIN);
		wall = new PhysicalObjectRect("wall", 2, JGColor.green,
				WALL_WIDTH, WALL_THICKNESS);
		wall.setPos(displayWidth() / 2, displayHeight() - WALL_MARGIN);
		wall = new PhysicalObjectRect("wall", 2, JGColor.green,
				WALL_THICKNESS, WALL_HEIGHT);
		wall.setPos(WALL_MARGIN, displayHeight() / 2);
		wall = new PhysicalObjectRect("wall", 2, JGColor.green,
				WALL_THICKNESS, WALL_HEIGHT);
		wall.setPos(displayWidth() - WALL_MARGIN, displayHeight() / 2);
	}

	public static void parseXML() {
		//Environmental variables, will change to read the environment XML file later
		float gravAccel = (float)9;
		float viscosity = (float)0.8;
		float cOmMag = (float)250;
		float cOmExp = (float)2;

		//Uses the FileChooser to let the user grab the XML file
		final FileChooser fc = new FileChooser();
		File file = fc.getFile();

		HashMap<String,SuperMass> obj = new HashMap<String,SuperMass>();

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();

			System.out.println("root of xml file" + doc.getDocumentElement().getNodeName());
			System.out.println("==========================");

			//parse fixed and dynamic masses
			System.out.println("dynamic masses:");
			NodeList nodeNodes = doc.getElementsByTagName("mass");
			for( int j = 0; j < nodeNodes.getLength(); j++){
				Node node = nodeNodes.item(j);
				System.out.println("id: " + getNodeAttr("id", node) + " x: " + getNodeAttr("x", node) + " y: " + getNodeAttr("y", node) +
						" vx: " + getNodeAttr("vx", node) + " vy: " + getNodeAttr("vy", node) + " mass: " + getNodeAttr("mass", node));

				float mass = 1;
				float xv = 0;
				float yv = 0;

				if(!(getNodeAttr("mass", node).equals(""))){
					mass = Float.parseFloat(getNodeAttr("mass", node));
				}
				if(!(getNodeAttr("xv", node).equals(""))){
					xv = Float.parseFloat(getNodeAttr("xv", node));
				}
				if(!(getNodeAttr("yv", node).equals(""))){
					yv = Float.parseFloat(getNodeAttr("yv", node));
				}
				float x = Float.parseFloat(getNodeAttr("x", node));
				float y = Float.parseFloat(getNodeAttr("y", node));
				String id = getNodeAttr("id", node);
				Mass tempMass = new Mass(id, x, y+20, mass, xv, yv);
				allSuperMasses.add(tempMass);
				obj.put(id, tempMass);
			}
			System.out.println();

			System.out.println("fixed masses:");
			nodeNodes = doc.getElementsByTagName("fixed");
			for( int j = 0; j < nodeNodes.getLength(); j++){
				Node node = nodeNodes.item(j);
				System.out.println("id: " + getNodeAttr("id", node) + " x: " + getNodeAttr("x", node) + " y: " + getNodeAttr("y", node));

				String id = getNodeAttr("id", node);
				float x = Float.parseFloat(getNodeAttr("x", node));
				float y = Float.parseFloat(getNodeAttr("y", node));

				Fixed tempFixed = new Fixed(id, x, y);
				allSuperMasses.add(tempFixed);
				obj.put(id, tempFixed);
				System.out.println();
			}


			//parse links
			System.out.println("springs:");
			nodeNodes = doc.getElementsByTagName("spring");
			for( int j = 0; j < nodeNodes.getLength(); j++){
				Node node = nodeNodes.item(j);
				System.out.println("a: " + getNodeAttr("a", node) + " b: " + getNodeAttr("b", node) + " restlength: " + getNodeAttr("restlength", node) +
						" constant: " + getNodeAttr("constant", node));

				float constant = 1;
				if(!(getNodeAttr("constant", node).equals(""))){
					constant = Float.parseFloat(getNodeAttr("constant", node));
				}
				float rl = 50;
				if(!(getNodeAttr("restlength", node).equals(""))){
					rl = Float.parseFloat(getNodeAttr("restlength", node));
				}
				SuperMass a = (SuperMass) obj.get(getNodeAttr("a", node));
				SuperMass b = (SuperMass) obj.get(getNodeAttr("b", node));

				force.add(new Spring(a, b, rl, constant));
			}
			System.out.println();

			System.out.println("muscles:");
			nodeNodes = doc.getElementsByTagName("muscle");
			for( int j = 0; j < nodeNodes.getLength(); j++){
				Node node = nodeNodes.item(j);
				System.out.println("a: " + getNodeAttr("a", node) + " b: " + getNodeAttr("b", node) + " restlength: " + getNodeAttr("restlength", node) +
						" constant: " + getNodeAttr("constant", node) + " amplitude: " + getNodeAttr("amplitude", node));
				
				float constant = 1;
				if(!(getNodeAttr("constant", node).equals(""))){
					constant = Float.parseFloat(getNodeAttr("constant", node));
				}
				float rl = 50;
				if(!(getNodeAttr("restlength", node).equals(""))){
					rl = Float.parseFloat(getNodeAttr("restlength", node));
				}
				float amplitude = 50;
				if(!(getNodeAttr("restlength", node).equals(""))){
					rl = Float.parseFloat(getNodeAttr("restlength", node));
				}
				SuperMass a = (SuperMass) obj.get(getNodeAttr("a", node));
				SuperMass b = (SuperMass) obj.get(getNodeAttr("b", node));

				force.add(new Muscle(a, b, rl, constant, amplitude));
			}
			System.out.println();

			//Pass list of masses to each environmental (non-spring/muscle) Force constructor
			force.add(new Gravity(gravAccel, new ArrayList<SuperMass>(obj.values())));
			force.add(new Viscosity(viscosity, new ArrayList<SuperMass>(obj.values())));
			force.add(new CenterOfMass(cOmMag, cOmExp, new ArrayList<SuperMass>(obj.values())));

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected static String getNodeAttr(String attrName, Node node ) {
		NamedNodeMap attrs = node.getAttributes();
		for (int y = 0; y < attrs.getLength(); y++ ) {
			Node attr = attrs.item(y);
			if (attr.getNodeName().equalsIgnoreCase(attrName)) {
				return attr.getNodeValue();
			}
		}
		return "";
	}

	public void clearAllTheDamnAssemblies(){
		for(SuperMass m: allSuperMasses){
			m.remove();
		}
		for(Force f: force){
			f.remove();
		}
	}
}
