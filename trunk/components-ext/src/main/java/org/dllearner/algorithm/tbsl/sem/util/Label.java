package org.dllearner.algorithm.tbsl.sem.util;

import java.util.Set;

public class Label {

	String m_Label;
	Position m_Position;

	public Label(String label) {
		m_Label = label;
	}
	public Label(String l,Position p) {
		m_Label = l;
		m_Position = p;
	}
	
	public String getLabel() {
		return m_Label;
	}
	
	public String toString() {
		
		if (m_Position != null) {
			return m_Position + "(" + m_Label + ")";
		} 
		else {
			return m_Label;
		}
	}

	public boolean occursAsSubIn(Set<DominanceConstraint> constraints,DominanceConstraint constraint) {
		
		for (DominanceConstraint c : constraints) {
			if ( !c.equals(constraint) & c.getSub().equals(this) ) { return true; }
		}
		return false;
	}

	public void setPosition(Position position) {
		m_Position = position;
		
	}
	
	public Position getPosition()
	{
		return m_Position;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_Label == null) ? 0 : m_Label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Label other = (Label) obj;
		if (m_Label == null) {
			if (other.m_Label != null)
				return false;
		} else if (!m_Label.equals(other.m_Label))
			return false;
		return true;
	}


	
	

}
