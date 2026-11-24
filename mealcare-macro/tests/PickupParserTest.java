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
 System.out.println("PASS: actual log labels, date filtering, CLOSED, other products, combined detail, quantity and rollover");
 }
}
