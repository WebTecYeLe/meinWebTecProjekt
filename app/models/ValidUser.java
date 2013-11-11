package models;

import play.data.validation.Constraints.Required;

/**
 * Beispiel für Validierung
 * @author mike
 *
 */
public class ValidUser {
	@Required
    public String email;
    
	@Required
	public String password;
    public boolean remember;
}
