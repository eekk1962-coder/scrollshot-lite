package kr.signal.mealcaremacro;
import java.util.regex.Pattern;
final class MealSelection {
 static final String[] MENUS={"샐러드세트","투고서비스","도시락세트","헬시팩"};
 final boolean lunch; final String menu,tab,productPattern,key,display;
 MealSelection(boolean lunch,String menu){
  if(!java.util.Arrays.asList(MENUS).contains(menu))throw new IllegalArgumentException("unknown menu");
  this.lunch=lunch;this.menu=menu;
  tab=lunch?"R&D센터":"석식(사내식당)";
  productPattern=Pattern.quote(menu)+(lunch?Pattern.quote("(R&D센터)"):"\\(사내식당-석식(?:용)?\\)");
  key=lunch?"booked_lunch_"+menu:menu.equals("헬시팩")?"booked":"booked_dinner_"+menu;
  display=(lunch?"중식":"석식")+" · "+menu;
 }
 boolean product(String text){return Pattern.compile(productPattern).matcher(PickupParser.normalize(text)).find();}
 boolean otherTabProduct(String text){String s=PickupParser.normalize(text);return lunch?s.contains("(사내식당-석식"):s.contains("(R&D센터)")||s.contains("(통합생산동)");}
 boolean legacy(){return key.equals("booked");}
 static MealSelection defaults(){return new MealSelection(false,"헬시팩");}
}
