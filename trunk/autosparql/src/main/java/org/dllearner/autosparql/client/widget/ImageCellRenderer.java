package org.dllearner.autosparql.client.widget;

import org.dllearner.autosparql.client.model.Example;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.user.client.ui.Image;

public class ImageCellRenderer implements GridCellRenderer<Example>{
	
	Image image;

	@Override
	public Object render(Example model, String property, ColumnData config, int rowIndex, int colIndex,
			ListStore<Example> store, Grid<Example> grid) {
		
		String imageURL = model.getImageURL();
		if(imageURL.isEmpty()){
			return null;
		} else {
			image = new Image(imageURL);
			image.addErrorHandler(new ErrorHandler() {
				
				@Override
				public void onError(ErrorEvent event) {
					image = null;
				}
			});
			if(image != null){
				image.setPixelSize(40, 40);
			}
			return image;
		}
	}

}
