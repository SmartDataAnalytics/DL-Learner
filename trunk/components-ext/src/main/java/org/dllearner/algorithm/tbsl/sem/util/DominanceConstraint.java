package org.dllearner.algorithm.tbsl.sem.util;


public class DominanceConstraint {

	Label m_Super;
	Label m_Sub;
	
	DomType m_Type;
	

	public DominanceConstraint (Label subLabel, Label superLabel)
	{
		m_Super = superLabel;
		m_Sub = subLabel;
		m_Type = DomType.sub;
	}
	public DominanceConstraint (String subLabel, String superLabel)
	{
		m_Super = new Label(superLabel);
		m_Sub = new Label(subLabel);
		m_Type = DomType.sub;
	}
	
	
	public void setSuper(Label superLabel)
	{
		m_Super = superLabel;
	}
	
	public void setSub(Label subLabel)
	{
		m_Sub = subLabel;
	}
	
	public Label getSuper()
	{
		return m_Super;
	}
	
	public Label getSub()
	{
		return m_Sub;	
	}
	
	public void setType(DomType type)
	{
		m_Type = type;
	}
	
	public DomType getType()
	{
		return m_Type;
	}
	
	public String toString()
	{
		if (m_Type.equals(DomType.sub)) {
			return m_Sub + "<" + m_Super;
		}
		else {
			return m_Sub + "=" + m_Super;
		}
	}
	
	public DominanceConstraint clone() {
		DominanceConstraint copy = new DominanceConstraint(m_Sub,m_Super);
		copy.setType(m_Type);
		return copy;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_Sub == null) ? 0 : m_Sub.hashCode());
		result = prime * result + ((m_Super == null) ? 0 : m_Super.hashCode());
		result = prime * result + ((m_Type == null) ? 0 : m_Type.hashCode());
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
		DominanceConstraint other = (DominanceConstraint) obj;
		if (m_Sub == null) {
			if (other.m_Sub != null)
				return false;
		} else if (!m_Sub.equals(other.m_Sub))
			return false;
		if (m_Super == null) {
			if (other.m_Super != null)
				return false;
		} else if (!m_Super.equals(other.m_Super))
			return false;
		if (m_Type == null) {
			if (other.m_Type != null)
				return false;
		} else if (!m_Type.equals(other.m_Type))
			return false;
		return true;
	}
	

	
}
