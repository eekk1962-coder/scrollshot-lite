package kr.signal.mealcaremacro;
import java.time.*;
import java.util.regex.*;
final class PickupParser {
 static final String PRODUCT="헬시팩(사내식당-석식용)";
 static final Pattern CARD=Pattern.compile("^(CLOSED)?"+Pattern.quote(PRODUCT)+"(\\d{1,2})[./-](\\d{1,2})(?:\\([월화수목금토일]\\))?픽업$",Pattern.CASE_INSENSITIVE);
 static final Pattern DETAIL=Pattern.compile("픽업(?:날짜|일자|일)(?:[:：])?(?:(20\\d{2})[./-])?(\\d{1,2})[./-](\\d{1,2})(?!\\d)(?:\\([월화수목금토일]\\))?");
 static final Pattern QTY=Pattern.compile("수량[:：]?([0-9]+)(?=픽업|$|[+])");
 static String normalize(String s){return s==null?"":s.replaceAll("[\\s\\u00a0\\u200b\\u3000]+","").replace('–','-').replace('−','-').replace('（','(').replace('）',')');}
 static LocalDate resolve(int month,int day,LocalDate today){try{LocalDate d=LocalDate.of(today.getYear(),month,day);if(d.isBefore(today.minusMonths(6)))d=d.plusYears(1);if(d.isAfter(today.plusMonths(6)))d=d.minusYears(1);return d;}catch(Exception e){return null;}}
 static final class Card {final LocalDate date;final boolean closed;Card(LocalDate d,boolean c){date=d;closed=c;}}
 static Card card(String text,LocalDate today){Matcher m=CARD.matcher(normalize(text));if(!m.matches())return null;LocalDate d=resolve(Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)),today);return d==null?null:new Card(d,m.group(1)!=null);}
 static LocalDate detail(String text,LocalDate today){Matcher m=DETAIL.matcher(normalize(text));if(!m.find())return null;try{return m.group(1)==null?resolve(Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)),today):LocalDate.of(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));}catch(Exception e){return null;}}
 static Integer quantity(String text){Matcher m=QTY.matcher(normalize(text));if(!m.find())return null;try{return Integer.valueOf(m.group(1));}catch(Exception e){return null;}}
}
