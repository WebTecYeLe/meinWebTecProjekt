package controllers;

import java.util.List;

import models.ShopList;
import models.ShopListItem;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class ShopListController extends Controller {
	
	/**
	 * Posten der Liste hinzufügen
	 * @return
	 */
	public static Result addItem(){
		ShopList shopList = ShopList.get();
		
		/**
		 * formular auslesen
		 */
		Form<ShopListItem> form = Form.form(ShopListItem.class);
		form = form.bindFromRequest();
		
		if(form.hasErrors() || form.hasGlobalErrors()){
			return badRequest(views.html.shoplist.shoplist.render(shopList.items));
		}
		
		ShopListItem item = form.get();
		shopList.addItem(item);
		Logger.info("Zu Liste hinzugefügt: " +item +" Posten: " +shopList.items.size());
		return redirect(routes.ShopListController.list());
	}

	/**
	 * Posten von Liste löschen
	 */
	public static Result deleteItem(String itemname){
		ShopList shopList = ShopList.get();
		shopList.remove(itemname);
		
		Logger.info("Von Liste gelöscht: " +itemname);
		return redirect("/shoplist/");
	}
	
	/**
	 * Einkausliste anzeigen
	 * @return
	 */
	public static Result list(){
		List<ShopListItem> items = ShopList.get().items;
		return ok(views.html.shoplist.shoplist.render(items));
	}
}
