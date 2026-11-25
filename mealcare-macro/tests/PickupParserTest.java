package kr.signal.mealcaremacro;
import java.time.*;
public class PickupParserTest {
 static void check(boolean condition,String msg){if(!condition)throw new AssertionError(msg);}
 public static void main(String[] args){
 LocalDate today=LocalDate.of(2026,11,24),start=LocalDate.of(2026,11,27);
 String[] labels={"CLOSED 헬시팩(사내식당-석식용) 11.25(수) 픽업","헬시팩(사내식당-석식용) 11.26(목) 픽업","헬시팩(사내식당-석식용) 11.27(금) 픽업","헬시팩(사내식당-석식용) 11.30(월) 픽업","헬시팩(사내식당-석식용) 12.01(화) 픽업","샐러드세트(사내식당-석식용) 11.27(금) 픽업","헬시팩(R&D센터) 11.27(금) 픽업"};
 String[] expected={"2026-11-25","2026-11-26","2026-11-27","2026-11-30","2026-12-01",null,null};
 int eligible=0;
 for(int i=0;i<labels.length;i++){PickupParser.Card c=PickupParser.card(labels[i],today);check(expected[i]==null?c==null:c!=null&&c.date.toString().equals(expected[i]),labels[i]);if(c!=null){check(c.closed==(i==0),"closed");if(!c.closed&&!c.date.isBefore(start))eligible++;}}
 check(eligible==3,"eligible count");
 String detail="헬시팩(사내식당-석식용) 11/23,25,27(월,수,금) -직화닭가슴살스테이크(오리지널),고구마,삶은계란,오초띵바,어메이징오트(오리지널)   11/24,26(화,목):닭가슴살네모나(블랙페퍼),고구마,하루견과청포도요거트,비타에너지바스낵24,연세두유고단백pro  수량 1 픽업날짜 2026.11.27(금)";
 check(start.equals(PickupParser.detail(detail,today)),"actual detail date");
 check(Integer.valueOf(1).equals(PickupParser.quantity(detail)),"actual detail qty");
 check(Integer.valueOf(10).equals(PickupParser.quantity(detail.replace("수량 1","수량 10"))),"reject qty 10");
 check(PickupParser.quantity("상품 설명 1")==null,"unknown qty");
 check(LocalDate.of(2027,1,1).equals(PickupParser.card("헬시팩(사내식당-석식용) 01.01(금) 픽업",LocalDate.of(2026,12,30)).date),"year rollover");
 java.util.Set<String> done=new java.util.HashSet<>();
 LocalDate cursor=PickupParser.nextEligible(start,done);
 for(String day:new String[]{"2026-11-27","2026-11-30","2026-12-01","2026-12-02"}){check(cursor.toString().equals(day),"weekday sequence "+day+" got "+cursor);done.add(day);cursor=PickupParser.nextEligible(cursor.plusDays(1),done);}
 check(PickupParser.nextEligible(start,done).equals(LocalDate.of(2026,12,3)),"restart skips booked");
 check(PickupParser.nextEligible(LocalDate.of(2026,11,28),java.util.Collections.emptySet()).equals(LocalDate.of(2026,11,30)),"weekend start");
 java.util.Set<String> keys=new java.util.HashSet<>();
 for(boolean lunch:new boolean[]{true,false})for(String menu:MealSelection.MENUS){
  MealSelection sel=new MealSelection(lunch,menu);check(keys.add(sel.key),"separate history key");
  String suffix=lunch?"(R&D센터)":menu.equals("투고서비스")?"(사내식당-석식)":"(사내식당-석식용)";
  String label=menu+suffix+" 11.27(금) 픽업";
  PickupParser.Card c=PickupParser.card(label,today,sel);check(c!=null&&c.date.equals(start)&&!c.closed,"selected "+label);
  check(PickupParser.card("CLOSED "+label,today,sel).closed,"closed "+label);
  check(sel.product(menu+suffix+" 수량 1 픽업날짜 2026.11.27(금)"),"detail match "+label);
  check(PickupParser.card(label,today,new MealSelection(!lunch,menu))==null,"reject other meal "+label);
  for(String other:MealSelection.MENUS)if(!other.equals(menu))check(PickupParser.card(label,today,new MealSelection(lunch,other))==null,"reject other menu "+label);
 }
 check(MealSelection.defaults().key.equals("booked"),"preserve legacy dinner healthy pack bookings");
 check(new MealSelection(false,"도시락세트").product("도시락세트(사내식당-석식)"),"dinner alternative suffix");
 System.out.println("PASS: all eight selections, wrong meal/menu rejection, closed, legacy booking keys");
 System.out.println("PASS: weekday target sequence, restart, weekend start");
 System.out.println("PASS: actual log labels, date filtering, CLOSED, other products, combined detail, quantity and rollover");
 }
}
