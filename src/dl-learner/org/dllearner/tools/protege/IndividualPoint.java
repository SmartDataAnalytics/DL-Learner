package org.dllearner.tools.protege;

public class IndividualPoint {
	
	private String point;
	private int xAxis;
	private int yAxis;
	private String individual;
	
	public IndividualPoint(String p, int x, int y, String ind) {
		this.point = p;
		this.xAxis = x;
		this.yAxis = y;
		this.individual = ind;
	}

	/**
	 * @param point the point to set
	 */
	public void setPoint(String point) {
		this.point = point;
	}

	/**
	 * @return the point
	 */
	public String getPoint() {
		return point;
	}

	/**
	 * @param xAxis the xAxis to set
	 */
	public void setXAxis(int xAxis) {
		this.xAxis = xAxis;
	}

	/**
	 * @return the xAxis
	 */
	public int getXAxis() {
		return xAxis;
	}

	/**
	 * @param yAxis the yAxis to set
	 */
	public void setYAxis(int yAxis) {
		this.yAxis = yAxis;
	}

	/**
	 * @return the yAxis
	 */
	public int getYAxis() {
		return yAxis;
	}
	
	public String getIndividualName() {
		return individual;
	}

}
