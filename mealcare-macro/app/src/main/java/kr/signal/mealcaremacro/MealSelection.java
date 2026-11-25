package kr.signal.mealcaremacro;
import java.util.regex.Pattern;
final class MealSelection {
 static final String[] MENUS={"샐러드세트","투고서비스","도시락세트","헬시팩"};
 static final String RD="R&D센터", PRODUCTION="통합생산동", DINNER="석식(사내식당)";
 final boolean lunch; final String menu,tab,productPattern,key,display;
 MealSelection(boolean lunch,String menu){this(lunch?RD:DINNER,menu);}
 MealSelection(String location,String menu){
  if(!java.util.Arrays.asList(MENUS).contains(menu))throw new IllegalArgumentException("unknown menu");
  if(!RD.equals(location)&&!PRODUCTION.equals(location)&&!DINNER.equals(location))throw new IllegalArgumentException("unknown location");
  this.lunch=!DINNER.equals(location);this.menu=menu;this.tab=location;
  productPattern=Pattern.quote(menu)+(lunch?Pattern.quote("("+tab+")"):"\\(사내식당-석식(?:용)?\\)");
  key=RD.equals(tab)?"booked_lunch_"+menu:PRODUCTION.equals(tab)?"booked_production_"+menu:menu.equals("헬시팩")?"booked":"booked_dinner_"+menu;
  display=(lunch?"중식 · "+tab:"석식 · 사내식당")+" · "+menu;
 }
 boolean product(String text){return Pattern.compile(productPattern).matcher(PickupParser.normalize(text)).find();}
 boolean otherTabProduct(String text){String s=PickupParser.normalize(text);return (!RD.equals(tab)&&s.contains("(R&D센터)"))||(!PRODUCTION.equals(tab)&&s.contains("(통합생산동)"))||(lunch&&s.contains("(사내식당-석식"));}
 boolean legacy(){return key.equals("booked");}
 static MealSelection defaults(){return new MealSelection(DINNER,"헬시팩");}
}
