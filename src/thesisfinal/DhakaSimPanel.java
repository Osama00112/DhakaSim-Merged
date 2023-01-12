package thesisfinal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.round;
import static thesisfinal.Controller.*;
import static thesisfinal.Network.linkList;
import static thesisfinal.Network.nodeList;
import static thesisfinal.Parameters.*;
import static thesisfinal.Utilities.*;

public class DhakaSimPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener {

	private ArrayList<Demand> testing = new ArrayList<>();

	private double translateX;
	private double translateY;
	private int referenceX = -999999999;
	private int referenceY = -999999999;
	private double scale = 0.10 + Constants.DEFAULT_SCALE * 0.05;

	private JFrame jFrame;
	private Timer timer;

	private boolean drawRoads = true;

	public DhakaSimPanel(JFrame jFrame) {
		this.jFrame = jFrame;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		if (CENTERED_VIEW) {
			translateX = -Network.midPoint.x * pixelPerMeter;
			translateY = -Network.midPoint.y * pixelPerMeter;
		} else {
			translateX = DEFAULT_TRANSLATE_X;
			translateY = DEFAULT_TRANSLATE_Y;
		}
		timer = new Timer(simulationSpeed, this);
		timer.start();
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g2d);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Constants.BACKGROUND_COLOR);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		AffineTransform affineTransform = g2d.getTransform();
		affineTransform.translate(getWidth() / 2.0, getHeight() / 2.0);
		affineTransform.scale(scale, scale);
		affineTransform.translate(-getWidth() / 2.0, -getHeight() / 2.0);
		affineTransform.translate(translateX, translateY);
		g2d.setTransform(affineTransform);

		if (drawRoads) {
			drawRoadNetwork(g2d);
		}

		if (TRACE_MODE) {
			Utilities.drawTrace(traceReader, g2d);
		}
		else {
			if (objectMode) {
				try {
					traceWriter.write("Current Objects");
					traceWriter.newLine();
				} catch (IOException ex) {
					Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
				}
				for (Object object : objects) {
					object.drawObject(traceWriter, g2d, pixelPerStrip, pixelPerMeter, pixelPerFootpathStrip);
				}
			}
			try {
				traceWriter.write("Current Vehicles");
				traceWriter.newLine();
			} catch (IOException ex) {
				Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
			for (Vehicle vehicle : vehicleList) {
				vehicle.drawVehicle(traceWriter, g2d, pixelPerStrip, pixelPerMeter, pixelPerFootpathStrip);
			}
			try {
				traceWriter.write("End Step");
				traceWriter.newLine();
				traceWriter.flush();
			} catch (IOException ex) {
				Logger.getLogger(DhakaSimPanel.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@SuppressWarnings("Duplicates")
	private void drawRoadNetwork(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		for (Link link : linkList) {
			link.draw(g2d);
		}
		ArrayList<double[]> lineList = new ArrayList<>();
		for (Node node : nodeList) {
			for (int j = 0; j < node.numberOfLinks(); j++) {
				Link link = linkList.get(node.getLink(j));
				double x1, y1, x2, y2, x3, y3, x4, y4;
				if (link.getUpNode() == node.getId()) {
					x1 = link.getFirstSegment().getStartX() * pixelPerMeter;
					y1 = link.getFirstSegment().getStartY() * pixelPerMeter;
					x2 = link.getFirstSegment().getEndX() * pixelPerMeter;
					y2 = link.getFirstSegment().getEndY() * pixelPerMeter;

					//double width = 2 * pixelPerFootpathStrip + (link.getSegment(0).numberOfStrips() - 2) * pixelPerStrip;
					double width = link.getFirstSegment().getWidth() * pixelPerMeter;

					x3 = returnX3(x1, y1, x2, y2, width);
					y3 = returnY3(x1, y1, x2, y2, width);
					x4 = returnX4(x1, y1, x2, y2, width);
					y4 = returnY4(x1, y1, x2, y2, width);
				} else {
					x1 = link.getLastSegment().getStartX() * pixelPerMeter;
					y1 = link.getLastSegment().getStartY() * pixelPerMeter;
					x2 = link.getLastSegment().getEndX() * pixelPerMeter;
					y2 = link.getLastSegment().getEndY() * pixelPerMeter;

//                    double w = 2 * pixelPerFootpathStrip + (link.getSegment(link.getNumberOfSegments() - 1).numberOfStrips() - 2) * pixelPerStrip;
					double w = link.getLastSegment().getWidth() * pixelPerMeter;

					x3 = returnX3(x1, y1, x2, y2, w);
					y3 = returnY3(x1, y1, x2, y2, w);
					x4 = returnX4(x1, y1, x2, y2, w);
					y4 = returnY4(x1, y1, x2, y2, w);

					x1 = x2;
					y1 = y2;
					x3 = x4;
					y3 = y4;
				}
				for (int k = 0; k < node.numberOfLinks(); k++) {
					if (j != k) {
						Link linkPrime = linkList.get(node.getLink(k));
						double x1Prime, y1Prime, x2Prime, y2Prime, x3Prime, y3Prime, x4Prime, y4Prime;
						if (linkPrime.getUpNode() == node.getId()) {
							x1Prime = linkPrime.getFirstSegment().getStartX() * pixelPerMeter;
							y1Prime = linkPrime.getFirstSegment().getStartY() * pixelPerMeter;
							x2Prime = linkPrime.getFirstSegment().getEndX() * pixelPerMeter;
							y2Prime = linkPrime.getFirstSegment().getEndY() * pixelPerMeter;

							//double w = 2 * pixelPerFootpathStrip + (linkPrime.getSegment(0).numberOfStrips() - 2) * pixelPerStrip;
							double w = linkPrime.getFirstSegment().getWidth() * pixelPerMeter;

							x3Prime = returnX3(x1Prime, y1Prime, x2Prime, y2Prime, w);
							y3Prime = returnY3(x1Prime, y1Prime, x2Prime, y2Prime, w);
							x4Prime = returnX4(x1Prime, y1Prime, x2Prime, y2Prime, w);
							y4Prime = returnY4(x1Prime, y1Prime, x2Prime, y2Prime, w);
						} else {
							x1Prime = linkPrime.getLastSegment().getStartX() * pixelPerMeter;
							y1Prime = linkPrime.getLastSegment().getStartY() * pixelPerMeter;
							x2Prime = linkPrime.getLastSegment().getEndX() * pixelPerMeter;
							y2Prime = linkPrime.getLastSegment().getEndY() * pixelPerMeter;

							//double w = 2 * pixelPerFootpathStrip + (linkPrime.getSegment(linkPrime.getNumberOfSegments() - 1).numberOfStrips() - 2) * pixelPerStrip;
							double w = linkPrime.getLastSegment().getWidth() * pixelPerMeter;


							x3Prime = returnX3(x1Prime, y1Prime, x2Prime, y2Prime, w);
							y3Prime = returnY3(x1Prime, y1Prime, x2Prime, y2Prime, w);
							x4Prime = returnX4(x1Prime, y1Prime, x2Prime, y2Prime, w);
							y4Prime = returnY4(x1Prime, y1Prime, x2Prime, y2Prime, w);

							x1Prime = x2Prime;
							y1Prime = y2Prime;
							x3Prime = x4Prime;
							y3Prime = y4Prime;
						}

						double[] one = new double[]{x1, y1, x1Prime, y1Prime};
						double[] two = new double[]{x1, y1, x3Prime, y3Prime};
						double[] three = new double[]{x3, y3, x1Prime, y1Prime};
						double[] four = new double[]{x3, y3, x3Prime, y3Prime};
						lineList.add(one);
						lineList.add(two);
						lineList.add(three);
						lineList.add(four);
					}
				}
			}
		}
		for (int i = 0; i < lineList.size(); i++) {
			boolean doIntersect = false;
			for (int j = 0; j < lineList.size(); j++) {
				if (i != j) {
					if (doIntersect(lineList.get(i)[0], lineList.get(i)[1],
							lineList.get(i)[2], lineList.get(i)[3],
							lineList.get(j)[0], lineList.get(j)[1],
							lineList.get(j)[2], lineList.get(j)[3])) {
						doIntersect = true;
						break;
					}
				}
			}
			if (!doIntersect) {
				g2d.drawLine((int) round(lineList.get(i)[0]),
						(int) round(lineList.get(i)[1]),
						(int) round(lineList.get(i)[2]),
						(int) round(lineList.get(i)[3]));
			}
		}
		for (Link link : linkList) {
			Segment segment = link.getFirstSegment();
			for (int j = 1; j < link.getNumberOfSegments(); j++) {
				double x1 = segment.getStartX() * pixelPerMeter;
				double y1 = segment.getStartY() * pixelPerMeter;
				double x2 = segment.getEndX() * pixelPerMeter;
				double y2 = segment.getEndY() * pixelPerMeter;

				//double w = 2 * pixelPerFootpathStrip + (segment.numberOfStrips() - 2) * pixelPerStrip;
				double w = segment.getWidth() * pixelPerMeter;

				double x3 = returnX3(x1, y1, x2, y2, w);
				double y3 = returnY3(x1, y1, x2, y2, w);
				double x4 = returnX4(x1, y1, x2, y2, w);
				double y4 = returnY4(x1, y1, x2, y2, w);

				double x1Prime = link.getSegment(j).getStartX() * pixelPerMeter;
				double y1Prime = link.getSegment(j).getStartY() * pixelPerMeter;
				double x2Prime = link.getSegment(j).getEndX() * pixelPerMeter;
				double y2Prime = link.getSegment(j).getEndY() * pixelPerMeter;

				//double wPrime = 2 * pixelPerFootpathStrip + (link.getSegment(j).numberOfStrips() - 2) * pixelPerStrip;
				double wPrime = link.getSegment(j).getWidth() * pixelPerMeter;

				double x3Prime = returnX3(x1Prime, y1Prime, x2Prime, y2Prime, wPrime);
				double y3Prime = returnY3(x1Prime, y1Prime, x2Prime, y2Prime, wPrime);
				double x4Prime = returnX4(x1Prime, y1Prime, x2Prime, y2Prime, wPrime);
				double y4Prime = returnY4(x1Prime, y1Prime, x2Prime, y2Prime, wPrime);

				g2d.drawLine((int) round(x2), (int) round(y2), (int) round(x1Prime), (int) round(y1Prime));
				g2d.drawLine((int) round(x4), (int) round(y4), (int) round(x3Prime), (int) round(y3Prime));

				segment = link.getSegment(j);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (simulationStep < simulationEndTime) {
			if (!TRACE_MODE) {
				try {
					Controller.run();
					DhakaSimFrame.showProgressSlider.setValue(simulationStep);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} else {
				simulationStep++;
			}
			repaint();
		}
		else if (simulationStep == simulationEndTime) {
			simulationStep++;

			Controller.generateStatistics();

			JPanel jPanel = new JPanel();
			JButton replay = new JButton(new ImageIcon("replay.png"));
			replay.setBackground(Color.WHITE);
			JButton pause = new JButton(new ImageIcon("pause.png"));
			pause.setBackground(Color.WHITE);
			JButton rewind = new JButton(new ImageIcon("rewind.png"));
			rewind.setBackground(Color.WHITE);
			JButton fastForward = new JButton(new ImageIcon("fast_forward.png"));
			fastForward.setBackground(Color.WHITE);
			jPanel.add(replay);
			jPanel.add(pause);
			jPanel.add(rewind);
			jPanel.add(fastForward);
			JSlider jSlider = new JSlider(JSlider.VERTICAL, 0, 0, 0);
			jFrame.getContentPane().add(jPanel, BorderLayout.SOUTH);
			jFrame.getContentPane().add(jSlider, BorderLayout.WEST);
			jFrame.repaint();
			jFrame.revalidate();
		}
	}

	@Override
	public void mousePressed(MouseEvent me) {
		referenceX = me.getX();
		referenceY = me.getY();
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		translateX += (me.getX() - referenceX)*.5/scale;
		translateY += (me.getY() - referenceY)*.5/scale;
		referenceX = me.getX();
		referenceY = me.getY();
		if (!TRACE_MODE) {
			repaint();
		}
		if (DEBUG_MODE) {
			System.out.println(translateX + " " + translateY);
		}
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		System.out.println("-> " + me.getX() + ", " + me.getY());
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mouseMoved(MouseEvent me) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent me) {
		int notches = me.getWheelRotation();
		double newScaleValue = scale - notches * 0.01;
		scale = Math.min(1.0, Math.max(0.01, newScaleValue));
	}
}
