package org.dllearner.utilities.experiments;

import java.util.List;

import com.jamonapi.MonKey;
import com.jamonapi.MonKeyItem;
@SuppressWarnings("all")
public class MyMonKey  implements MonKey{
			
	
	
	      private final String summaryLabel; // pageHits for example
	      private Object details; // The actual page name for the detail buffer.  pageHits for example
	      private final String units; // ms. for example
//	      private boolean initializeDetail=true;
	      private Object param;
//	      
	      public MyMonKey(String summaryLabel, String units) {
	    	  this(summaryLabel, summaryLabel, units);
	      }
	   
//	      /** Object details can be an Object[], a Collection, or a Java Object.  */
	      public MyMonKey(String summaryLabel, Object details, String units) {
	          this.summaryLabel = (summaryLabel==null) ? "" : summaryLabel;
	          this.details = details;
	          this.units= (units==null) ? "" : units;
	    }
	      
	      public MyMonKey(MonKeyItem keyItem, String units) {
	    	  super();
	          this.summaryLabel = (keyItem==null) ? "" : keyItem.toString();;
	          this.units= (units==null) ? "" : units;
	          this.details=keyItem.getDetails();
	      }


	      
	      /** Returns the label for the monitor */
	      public String getLabel() {
	          return summaryLabel;
	      }
	        
	      /** Returns the units for the monitor */
	      public String getUnits() {
	          return units;
	      }
	      
	  	public Object getDetails() {
	        return details;
	 //       return details;
//	  		if (initializeDetail) {
//	  			initializeDetail=false;
//	  			detailLabel+=", "+units;
//	            (detailLabel==null) ? "" : detailLabel
//	  		}
//	  		
//		     return detailLabel;
		}
	    
	//    
//	    public List getDetails(List list) {
//	        Misc.addTo(list, details);
//	        return list;
//	    }
	  	
		public void setDetails(Object details) {
			this.details=details;
		    
	    }
	      
	      /** Returns any object that has a named key.  In this keys case
	       * 'label' and 'units' makes sense, but any values are acceptible.
	       */
	      public Object getValue(String key) {
	          if (LABEL_HEADER.equalsIgnoreCase(key))
	             return getLabel();
	          else if (UNITS_HEADER.equalsIgnoreCase(key))
	             return getUnits();
	          else if ("param".equalsIgnoreCase(key))
	             return getParam();
	          else if ("details".equalsIgnoreCase(key))
	             return getDetails();
	          else
	             return null;
	              
	      }
	      
	      /** Used to get any arbitrary Object into the key.  It will not be used as part of the key, however it can be retrieved later for example
	       * in the JAMonBufferListener.
	       * @return
	       */
	      public Object getParam() {
	          return param;
	      }
	 
	      
	      /** Used to set any arbitrary Object into the key.  It will not be used as part of the key, however it can be retrieved later for example
	       * in the JAMonBufferListener.
	       * @return
	       */
	      public void setParam(Object param) {
	          this.param=param;
	      }
	        
	/**
	This method is called automatically by a HashMap when this class is used as a HashMap key.  A Coordinate is
	considered equal if its x and y variables have the same value.
	*/

	  @Override
	public boolean equals(Object compareKey) {

	     return (
	         compareKey instanceof MyMonKey && 
	         summaryLabel.equals(((MyMonKey) compareKey).summaryLabel) &&
	         units.equals(((MyMonKey) compareKey).units)
	         );

	  }

	  /** Used when key is put into a Map to look up the monitor */
	  @Override
	  public int hashCode() {
	     return (summaryLabel.hashCode() + units.hashCode());
	   }

	    public List getBasicHeader(List header) { 
	        header.add(LABEL_HEADER);
	        return header;
	    }   
	        
	  
	    public List getDisplayHeader(List header) {
	        return getHeader(header);
	    }
	    
	    public List getHeader(List header) {
	        header.add(LABEL_HEADER);
	        header.add(UNITS_HEADER);
	        return header;
	    }   
	    
	    public List getBasicRowData(List rowData) {
	      rowData.add(getLabel()+", "+getUnits());
	      return rowData;
	    }
	    

	    
	    public List getRowData(List rowData) {
	      rowData.add(getLabel());
	      rowData.add(getUnits());
	      
	      return rowData;
	    }
	    
	    public List getRowDisplayData(List rowData) {
	        return getRowData(rowData);
	    }


	    
	    public String toString() {
	        return new StringBuffer().append("JAMon Label=").append(getLabel()).append(", Units=").append(getUnits()).toString();
	        
	    }
	    
	    public String getRangeKey() {
	        return getUnits();
	    }

	    
	
}
