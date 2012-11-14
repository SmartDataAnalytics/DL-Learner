package org.dllearner.algorithm.tbsl.learning;

import java.io.Serializable;
import java.util.Arrays;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** @author konrad
 * TODO: the class is a bit of an ugly mix between enum and non-enum now, when I have more time I need to clean this **/
public class LearnStatus implements Serializable
{
	public enum Type {OK, TIMEOUT, NO_TEMPLATE_FOUND,QUERY_RESULT_EMPTY,NO_QUERY_LEARNED,EXCEPTION}

	public final Type type;

	protected static final long	serialVersionUID	= 1L;
	protected static final LearnStatus OK = new LearnStatus(Type.OK,null);
	protected static final LearnStatus TIMEOUT = new LearnStatus(Type.TIMEOUT,null);
	protected static final LearnStatus NO_TEMPLATE_FOUND = new LearnStatus(Type.NO_TEMPLATE_FOUND,null);
	protected static final LearnStatus QUERY_RESULT_EMPTY = new LearnStatus(Type.QUERY_RESULT_EMPTY,null);
	protected static final LearnStatus NO_QUERY_LEARNED = new LearnStatus(Type.NO_QUERY_LEARNED,null);

	public final Exception exception;

	public Long timeMs = null;

	protected LearnStatus(Type type, Exception exception ,Long timeMs) {this.type=type;this.exception = exception;this.timeMs=timeMs;}
	protected LearnStatus(Type type, Exception exception) {this(type,exception,null);}
	protected LearnStatus(Exception exception, long timeMs) {this(Type.EXCEPTION,exception,timeMs);}
	
	public LearnStatus(Type type, long timeMs) {this.type=type;this.exception = null;this.timeMs=timeMs;}

	public static LearnStatus exceptionStatus(Exception cause, Long timeMs)
	{
		if (cause == null) throw new NullPointerException();
		return new LearnStatus(Type.EXCEPTION,cause);
	}

//	public static LearnStatus exceptionStatus(Exception cause)	{return exceptionStatus(cause,null);}

	@Override public String toString()
	{
		switch(type)
		{
			case OK:				return "OK";
			case TIMEOUT:			return "timeout";
			case NO_TEMPLATE_FOUND:	return "no template found";
			case QUERY_RESULT_EMPTY:return "query result empty";
			case NO_QUERY_LEARNED:	return "no query learned";
			case EXCEPTION:			return "<summary>Exception: <details>"+Arrays.toString(exception.getStackTrace())+"</details></summary>";
			default: throw new RuntimeException("switch type not handled");
		}			
	}
	
	public Element element(Document doc,String name)
	{
		Element statusElement = doc.createElement(name);
		{
			Element typeElement = doc.createElement("type");
			statusElement.appendChild(typeElement);
			typeElement.setTextContent(this.type.toString());
		}
		if (this.type==Type.EXCEPTION)
		{	
			Element exceptionElement = doc.createElement("exception");
			statusElement.appendChild(exceptionElement);
			exceptionElement.setTextContent(this.exception.getMessage());
		}
		if(timeMs!=null)
		{
			Element timeMsElement = doc.createElement("timeMs");
			statusElement.appendChild(timeMsElement);
			timeMsElement.setTextContent(this.timeMs.toString());
		}
		return statusElement; 
	}
}