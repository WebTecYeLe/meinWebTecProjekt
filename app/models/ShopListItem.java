package models;

import play.data.validation.Constraints;


public class ShopListItem {
	@Constraints.Required(message="Menge ist erforderlich")
	public int amount;
	
	@Constraints.Required(message="Name ist erforderlich")
	public String name;
	
	@Override
	public String toString() {
		return "" +amount +" " +name;
	}
}
