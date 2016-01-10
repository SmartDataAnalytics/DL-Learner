package org.dllearner.prolog;



/**
 * 
 * @author Sebastian Bader
 *
 */
public class Clause {
    private Atom head;
    private Body body;
    
    public Clause(Atom head, Body body) {
        this.head = head;
        this.body = body;
        if (body == null)
            this.body = new Body();
    }

    @Override
	public String toString() {
        if (body.isEmpty())
            return head+".";
        return head + " :- " + body +".";
    }

    public String toPLString() {
        if (body.isEmpty())
            return head.toPLString()+".";
        return head.toPLString() + " :- " + body.toPLString() +".";
    }

    public boolean isGround() {
        if (!head.isGround())
            return false;
        
        return body.isGround();
    }

    public Body getBody() {
        return body;
    }

    public Atom getHead() {
        return head;
    }
    
    /**
	 * 
	 * @param variable
	 *            Substitution variable.
	 * @param term
	 *            A term.
	 * @return Returns a new instance of this term, where the variable is
	 *         replaced by the term.
	 */    
    public Clause getInstance(Variable variable, Term term) {
        Atom newhead = head.getInstance(variable, term);
        Body newbody = body.getInstance(variable, term);
        
        return new Clause(newhead, newbody);
    }

}

