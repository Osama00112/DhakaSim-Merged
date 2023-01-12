package thesisfinal;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Network {
	static ArrayList<Demand> demandList = new ArrayList<>();
	static ArrayList<Link> linkList = new ArrayList<>();
	static ArrayList<Path> pathList = new ArrayList<>();
	static ArrayList<Node> nodeList = new ArrayList<>();
	static ArrayList<Node> intersectionList = new ArrayList<>(); //if node is an intersection, it's added to this list ALSO.
	static Point2D midPoint;

	Network () {}

	static void readNetwork () {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("link.txt"))));
			String dataLine = bufferedReader.readLine();
			int numLinks = Integer.parseInt(dataLine);
			for (int i = 0; i < numLinks; i++) {
				dataLine = bufferedReader.readLine();
				StringTokenizer stringTokenizer = new StringTokenizer(dataLine, " ");
				int linkId = Integer.parseInt(stringTokenizer.nextToken());
				int nodeId1 = Integer.parseInt(stringTokenizer.nextToken());
				int nodeId2 = Integer.parseInt(stringTokenizer.nextToken());
				int segmentCount = Integer.parseInt(stringTokenizer.nextToken());
				Link link = new Link(i, linkId, nodeId1, nodeId2);
				for (int j = 0; j < segmentCount; j++) {
					dataLine = bufferedReader.readLine();
					stringTokenizer = new StringTokenizer(dataLine, " ");
					int segmentId = Integer.parseInt(stringTokenizer.nextToken());
					double startX = Double.parseDouble(stringTokenizer.nextToken());
					double startY = Double.parseDouble(stringTokenizer.nextToken());
					double endX = Double.parseDouble(stringTokenizer.nextToken());
					double endY = Double.parseDouble(stringTokenizer.nextToken());
					double segmentWidth = Double.parseDouble(stringTokenizer.nextToken());
					boolean firstSegment = false;
					if (j == 0) {
						firstSegment = true;
					}
					boolean lastSegment = false;
					if (j == segmentCount - 1) {
						lastSegment = true;
					}
					Segment segment = new Segment(i, j, segmentId, startX, startY, endX, endY, segmentWidth, lastSegment, firstSegment, linkId);
					link.addSegment(segment);
				}
				linkList.add(link);
			}
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("node.txt"))));
			dataLine = bufferedReader.readLine();
			int numNodes = Integer.parseInt(dataLine);
			ArrayList<Point2D> boundaryPoints = new ArrayList<>();
			for (int i = 0; i < numNodes; i++) {
				dataLine = bufferedReader.readLine();
				StringTokenizer stringTokenizer = new StringTokenizer(dataLine, " ");
				int nodeId = Integer.parseInt(stringTokenizer.nextToken());
				double centerX = Double.parseDouble(stringTokenizer.nextToken());
				double centerY = Double.parseDouble(stringTokenizer.nextToken());
				if (centerX != 0 && centerY != 0) {
					boundaryPoints.add(new Point2D(centerX, centerY));
				}
				Node node = new Node(i, nodeId, centerX, centerY);
				while (stringTokenizer.hasMoreTokens()) {
					node.addLink(getLinkIndex(Integer.parseInt(stringTokenizer.nextToken())));
				}
				if (node.numberOfLinks() > 1) {
					node.createBundles();
					intersectionList.add(node);
				}
				nodeList.add(node);
				double left = Double.MAX_VALUE;
				double right = 0;
				double top = Double.MAX_VALUE;
				double down = 0;

				for (Point2D boundaryPoint : boundaryPoints) {

					left = Math.min(left, boundaryPoint.x);
					right = Math.max(right, boundaryPoint.x);

					top = Math.min(top, boundaryPoint.y);
					down = Math.max(down, boundaryPoint.y);
				}
				midPoint = new Point2D((left + right) / 2, (top + down) / 2);
			}
			bufferedReader.close();
		} catch (IOException ex) {
			Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	static void readPath () {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("path.txt"))));
			String dataLine = bufferedReader.readLine();
			int numPaths = Integer.parseInt(dataLine);
			for (int i = 0; i < numPaths; i++) {
				dataLine = bufferedReader.readLine();
				StringTokenizer stringTokenizer = new StringTokenizer(dataLine, " ");
				int nodeId1 = Integer.parseInt(stringTokenizer.nextToken());
				int nodeId2 = Integer.parseInt(stringTokenizer.nextToken());
				Path path = new Path(Network.getNodeIndex(nodeId1), Network.getNodeIndex(nodeId2));
				while (stringTokenizer.hasMoreTokens()) {
					int linkId = Integer.parseInt(stringTokenizer.nextToken());
					path.addLink(Network.getLinkIndex(linkId));
				}
				pathList.add(path);
			}
			bufferedReader.close();
		} catch (IOException ex) {
			Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	static void readDemand() {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("demand.txt"))));
			String dataLine = bufferedReader.readLine();
			int numDemands = Integer.parseInt(dataLine);
			for (int i = 0; i < numDemands; i++) {
				dataLine = bufferedReader.readLine();
				StringTokenizer stringTokenizer = new StringTokenizer(dataLine, " ");
				int nodeId1 = Integer.parseInt(stringTokenizer.nextToken());
				int nodeId2 = Integer.parseInt(stringTokenizer.nextToken());
				int demand = Integer.parseInt(stringTokenizer.nextToken());
				demandList.add(new Demand(Network.getNodeIndex(nodeId1), Network.getNodeIndex(nodeId2), demand));
			}
			bufferedReader.close();
		} catch (IOException ex) {
			Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	static void addPathToDemand() {
		for (Path path : pathList) {
			for (Demand demand : demandList) {
				if (demand.getSource() == path.getSource() && demand.getDestination() == path.getDestination()) {
					demand.addPath(path);

				}
			}
		}
	}

	static int getNodeIndex (int nodeId) {
		for (Node node : nodeList) {
			if (node.getId() == nodeId) {
				return node.getIndex();
			}
		}
		return -1;
	}

	static int getLinkIndex (int linkId) {
		for (Link link : linkList) {
			if (link.getId() == linkId) {
				return link.getIndex();
			}
		}
		return -1;
	}
}
